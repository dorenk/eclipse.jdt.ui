package org.eclipse.jdt.internal.corext.refactoring.concurrency;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IFile;

import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.refactoring.descriptors.FJTaskRefactoringDescriptor;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;

import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.jdt.internal.corext.dom.ModifierRewrite;
import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.JDTRefactoringDescriptorComment;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringDescriptorUtil;
import org.eclipse.jdt.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
import org.eclipse.jdt.internal.corext.refactoring.changes.TextChangeCompatibility;
import org.eclipse.jdt.internal.corext.refactoring.util.CompilationUnitSourceContext;
import org.eclipse.jdt.internal.corext.refactoring.util.MessageUtil;
import org.eclipse.jdt.internal.corext.refactoring.util.ResourceUtil;
import org.eclipse.jdt.internal.corext.refactoring.util.TextChangeManager;
import org.eclipse.jdt.internal.corext.util.Messages;

import org.eclipse.jdt.ui.CodeStyleConfiguration;
import org.eclipse.jdt.ui.JavaElementLabels;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;

public class ConvertToFJTaskRefactoring extends Refactoring {

	private static final String NO_NAME= ConcurrencyRefactorings.ConcurrencyRefactorings_empty_string;
	private IMethod fMethod;
	private CompilationUnit fRoot;
	private MethodDeclaration fMethodDeclaration;
	private ASTRewrite fRewriter;
	private TextChangeManager fChangeManager;
	private ImportRewrite fImportRewrite;
	private String nameForFJTaskSubtype= ConcurrencyRefactorings.ConcurrencyRefactorings_empty_string;
	private String sequentialThreshold= ConcurrencyRefactorings.ConcurrencyRefactorings_empty_string;
	private boolean fInfixExpressionFlag= false;
	private boolean fMethodInvocationFlag= false;
	private Statement fSingleElseStatement;

	

	public ConvertToFJTaskRefactoring(IMethod method){
		fChangeManager= new TextChangeManager();
		this.fMethod= method;
		nameForFJTaskSubtype= suggestTaskName();
	}
	
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		
		RefactoringStatus result= new RefactoringStatus();
		fChangeManager.clear();
		pm.beginTask(ConcurrencyRefactorings.ConcurrencyRefactorings_empty_string, 12);
		pm.setTaskName(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_check_preconditions);
		pm.worked(1);
			
		pm.setTaskName(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_task_name);
		
		List<TextEditGroup> ownerDescriptions= new ArrayList<TextEditGroup>();
		ICompilationUnit owner= fMethod.getCompilationUnit();
		
		fImportRewrite= CodeStyleConfiguration.createImportRewrite(fRoot, true);
		
		checkCompileErrors(result, fRoot, owner);
		
		ownerDescriptions.addAll(addCreateTaskClass(fRoot, result));
		if (result.hasFatalError()) {
			return result;
		}
		
		ownerDescriptions.addAll(reimplementOriginalRecursiveFunction());
		
		addImports(fImportRewrite);
		
		
		createEdits(owner, fRewriter, ownerDescriptions, fImportRewrite);
		
		IFile[] filesToBeModified= ResourceUtil.getFiles(fChangeManager.getAllCompilationUnits());
		result.merge(Checks.validateModifiesFiles(filesToBeModified, getValidationContext()));
		if (result.hasFatalError()) {
			return result;
		}
		ResourceChangeChecker.checkFilesToBeChanged(filesToBeModified, new SubProgressMonitor(pm, 1));
		return result;
		
	}
	
	private Collection<TextEditGroup> reimplementOriginalRecursiveFunction() {
		
		TextEditGroup gd= new TextEditGroup(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_recursive_method);
		
		AST ast= fRoot.getAST();
		
		Block originalBody= fMethodDeclaration.getBody();
		Block newMethodBody= ast.newBlock();
		List<ASTNode> newStatements= newMethodBody.statements();
		
		String declareNumOfAvailableResource= "int processorCount = Runtime.getRuntime().availableProcessors();"; //$NON-NLS-1$
		ASTNode declNumOfAvailableResources= fRewriter.createStringPlaceholder(declareNumOfAvailableResource, ASTNode.EXPRESSION_STATEMENT);
		newStatements.add(declNumOfAvailableResources);
		
		String pool= new String("ForkJoinPool pool = new ForkJoinPool(processorCount);"); //$NON-NLS-1$
		ASTNode poolNode= fRewriter.createStringPlaceholder(pool, ASTNode.EXPRESSION_STATEMENT);
		newStatements.add(poolNode);
		
		VariableDeclarationFragment newTaskDeclFragment= ast.newVariableDeclarationFragment();
		String taskInstanceName= "a" + nameForFJTaskSubtype; //$NON-NLS-1$
		newTaskDeclFragment.setName(ast.newSimpleName(taskInstanceName));
		ClassInstanceCreation createTaskInstance= ast.newClassInstanceCreation();
		newTaskDeclFragment.setInitializer(createTaskInstance);
		createTaskInstance.setType(ast.newSimpleType(ast.newSimpleName(nameForFJTaskSubtype)));
		List<SimpleName> argumentsForTaskCreation= createTaskInstance.arguments();
		
		List<ASTNode> recursiveMethodParameters= fMethodDeclaration.parameters();
		for (Object par : recursiveMethodParameters) {
			SingleVariableDeclaration parameter= (SingleVariableDeclaration) par;
			argumentsForTaskCreation.add(ast.newSimpleName(parameter.getName().getIdentifier()));
		}
		
		VariableDeclarationStatement declTask= ast.newVariableDeclarationStatement(newTaskDeclFragment);
		declTask.setType(ast.newSimpleType(ast.newSimpleName(nameForFJTaskSubtype)));
		newStatements.add(declTask);
		
		String poolInvoke= "pool.invoke(" + taskInstanceName +");"; //$NON-NLS-1$ //$NON-NLS-2$
		ASTNode poolInvokeNode= fRewriter.createStringPlaceholder(poolInvoke, ASTNode.EXPRESSION_STATEMENT);
		newStatements.add(poolInvokeNode);
		
		if (!recursiveMethodReturnsVoid()) {
			String returnSt= "return " + taskInstanceName + ".result;"; //$NON-NLS-1$ //$NON-NLS-2$
			ASTNode returnNode= fRewriter.createStringPlaceholder(returnSt, ASTNode.EXPRESSION_STATEMENT);
			newStatements.add(returnNode);
		}
		
		fRewriter.replace(originalBody, newMethodBody, gd);
		
		ArrayList<TextEditGroup> group= new ArrayList<TextEditGroup>();
		group.add(gd);
		return group;
	}

	private void addImports(ImportRewrite importRewrite) {
		
		importRewrite.addImport("java.util.concurrent.ForkJoinPool"); //$NON-NLS-1$
		importRewrite.addImport("java.util.concurrent.RecursiveAction"); //$NON-NLS-1$
	}

	private Collection<TextEditGroup> addCreateTaskClass(CompilationUnit root, RefactoringStatus result) {
		
		TextEditGroup gd= new TextEditGroup(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_recursive_action);
		
		AST ast= root.getAST();
		TypeDeclaration recursiveActionSubtype= ast.newTypeDeclaration();
		recursiveActionSubtype.setName(ast.newSimpleName(nameForFJTaskSubtype));
		recursiveActionSubtype.setSuperclassType(ast.newSimpleType(ast.newSimpleName("RecursiveAction")));	//$NON-NLS-1$	
		ModifierRewrite.create(fRewriter, recursiveActionSubtype).copyAllModifiers(fMethodDeclaration, gd);
		
		createFields(recursiveActionSubtype, ast);

		createConstructor(recursiveActionSubtype, ast);
		
		createComputeMethod(recursiveActionSubtype, ast, result);
		
		copyRecursiveMethod(recursiveActionSubtype, ast, result);
		
		ChildListPropertyDescriptor descriptor= getBodyDeclarationsProperty(fMethodDeclaration.getParent());
		fRewriter.getListRewrite(fMethodDeclaration.getParent(), descriptor).insertAfter(recursiveActionSubtype, fMethodDeclaration, gd);
		
		
		ArrayList<TextEditGroup> group= new ArrayList<TextEditGroup>();
		group.add(gd);
		return group;
	}

	private void copyRecursiveMethod(TypeDeclaration recursiveActionSubtype, AST ast, RefactoringStatus result) {
		
		ASTNode copyRecursiveMethod= ASTNode.copySubtree(ast, fMethodDeclaration);
		recursiveActionSubtype.bodyDeclarations().add(copyRecursiveMethod);
		if(fMethodDeclaration.getBody() != null) {
			checkIfCommentWarning(result);
		}
	}

	private void checkIfCommentWarning(RefactoringStatus result) {
		int start= fMethodDeclaration.getBody().getStartPosition();
		int end= fMethodDeclaration.getBody().getLength() - start;
		List<Comment> commentList= fRoot.getCommentList();
		if (commentList.size() != 0) {
			for (int i=0; i < commentList.size(); i++) {
				int tempStart= commentList.get(i).getStartPosition();
				if (tempStart > start && tempStart < end) {
					RefactoringStatus warning= new RefactoringStatus();
					warning.addWarning(Messages.format(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_comment_warning, new String[] {fMethod.getElementName()}));
					result.merge(warning);
					return;
				}
			}
		}
	}

	private void createComputeMethod(TypeDeclaration recursiveActionSubtype, final AST ast, RefactoringStatus result) {
		
		MethodDeclaration computeMethod= ast.newMethodDeclaration();
		computeMethod.setName(ast.newSimpleName("compute")); //$NON-NLS-1$
		computeMethod.modifiers().add(ast.newModifier(ModifierKeyword.PROTECTED_KEYWORD));
		
		final TextEditGroup editGroup= new TextEditGroup(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_generate_compute);
		
		if (fMethodDeclaration.getBody() == null) {
			createFatalError(result, Messages.format(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_method_body_error, new String[] {fMethod.getElementName()}));
			return;
		}
		Statement recursionBaseCaseBranch= identifyRecursionBaseCaseBranch(fMethodDeclaration.getBody());
		if (recursionBaseCaseBranch == null) {
			createFatalError(result, Messages.format(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_recursion_error, new String[] {fMethod.getElementName()}));
			return;
		}

		final ASTRewrite scratchRewriter= ASTRewrite.create(fRoot.getAST());
		ASTNode sequentialThresholdCheck= scratchRewriter.createStringPlaceholder(sequentialThreshold, ASTNode.PARENTHESIZED_EXPRESSION); 
		
		IfStatement enclosingIf= (IfStatement) recursionBaseCaseBranch.getParent();
		scratchRewriter.replace(enclosingIf.getExpression(), sequentialThresholdCheck, editGroup);
		
		if (recursionBaseCaseBranch instanceof Block) {
			doRecursionBaseCaseBlock(ast, editGroup, recursionBaseCaseBranch, scratchRewriter);
		} else if (recursionBaseCaseBranch instanceof ReturnStatement) {
			doRecursionBaseCaseReturn(ast, editGroup, recursionBaseCaseBranch, scratchRewriter);
		}
		
		final List<Statement> statementsWithRecursiveMethodInvocation= new ArrayList<Statement>();
		final int[] taskNumber= new int[] {0};
		final List<String> partialComputationsNames= new ArrayList<String>();
		final List<String> typesOfComputations= new ArrayList<String>();
		final boolean[] switchStatementFound= new boolean[] {false};
		fSingleElseStatement = null;  //TODO Remove
		final Map<Integer, Block> tasksToBlock= new HashMap<Integer, Block>();  //Can determine which task belongs to which block
		final Map<Block, Integer> numTasksPerBlock= new HashMap<Block, Integer>();  //Can determine how many tasks belong to this block easily
		final Map<ASTNode, Block> locationOfNewBlocks= new HashMap<ASTNode, Block>();  //Can determine where the new block was created so as to see if already has been created (don't create a new one at "same" place)
		final Map<Block, Statement> blockWithoutBraces= new HashMap<Block, Statement>();  //Can determine if a block does not have braces so as to use when inserting things to it
		fMethodDeclaration.accept(new ASTVisitor() {
			@Override
			public boolean visit(MethodInvocation methodCall) {
				
				IMethodBinding bindingForMethodCall= methodCall.resolveMethodBinding();
				IMethodBinding bindingForMethodDeclaration= fMethodDeclaration.resolveBinding();
				if (bindingForMethodCall.isEqualTo(bindingForMethodDeclaration)) {
					String codeForTaskDecl= nameForFJTaskSubtype + " task" + ++taskNumber[0] +  //$NON-NLS-1$
				    " = new " + nameForFJTaskSubtype + "("; //$NON-NLS-1$ //$NON-NLS-2$
					String methodArguments= ConcurrencyRefactorings.ConcurrencyRefactorings_empty_string;
					List<Expression> arguments= methodCall.arguments();
					for (Iterator<Expression> iterator= arguments.iterator(); iterator
							.hasNext();) {
						ASTNode argument= iterator.next();
						methodArguments += argument.toString();
						if (iterator.hasNext()) {
							methodArguments += ", "; //$NON-NLS-1$
						}
					}
					codeForTaskDecl += methodArguments + ");"; //$NON-NLS-1$
					VariableDeclarationStatement taskDeclStatement= (VariableDeclarationStatement) scratchRewriter.createStringPlaceholder(codeForTaskDecl , ASTNode.VARIABLE_DECLARATION_STATEMENT);
					
					Block myBlock= null;
					Statement parentOfMethodCall= findParentStatement(methodCall);
					if (parentOfMethodCall == null) {
						return false;
					} else if (SwitchStatement.class.isInstance(parentOfMethodCall.getParent())) {
						switchStatementFound[0]= true;
						return false;
					} else if (recursiveMethodReturnsVoid()) {
						scratchRewriter.replace(parentOfMethodCall, taskDeclStatement, editGroup);
					} else {
						if (parentOfMethodCall instanceof VariableDeclarationStatement) {
							VariableDeclarationStatement varDeclaration= (VariableDeclarationStatement) parentOfMethodCall;
							VariableDeclarationFragment varFragment= (VariableDeclarationFragment) varDeclaration.fragments().get(0);
							partialComputationsNames.add(varFragment.getName().getIdentifier());
							typesOfComputations.add(varDeclaration.getType().toString());
							scratchRewriter.replace(parentOfMethodCall, taskDeclStatement, editGroup);
						} else if (parentOfMethodCall instanceof ExpressionStatement) {
							ExpressionStatement exprStatement= (ExpressionStatement) parentOfMethodCall;
							Expression expressionContainer= exprStatement.getExpression();
							if (expressionContainer instanceof Assignment) {
								Assignment assignment= (Assignment) expressionContainer;
								Expression leftHandSide= assignment.getLeftHandSide();
								partialComputationsNames.add(leftHandSide.toString());
								typesOfComputations.add(leftHandSide.resolveTypeBinding().getName());
								scratchRewriter.replace(parentOfMethodCall, taskDeclStatement, editGroup);
							} else {
								System.err.println(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_scenario_error + parentOfMethodCall.toString() );
								return false;
							}
						} else if (parentOfMethodCall instanceof ReturnStatement) {
							ASTNode tempNode= parentOfMethodCall.getParent();
							if (tempNode instanceof Block) {
								Block blockWithReturn= (Block) tempNode;
								ListRewrite listRewriteForBlock= scratchRewriter.getListRewrite(blockWithReturn, Block.STATEMENTS_PROPERTY);
								List<ASTNode> statementsInBlockWithReturn= blockWithReturn.statements();
								Statement lastStatementInBlock= (Statement) statementsInBlockWithReturn.get(statementsInBlockWithReturn.size() - 1);
								if (lastStatementInBlock instanceof ReturnStatement) {
									listRewriteForBlock.insertBefore(taskDeclStatement, lastStatementInBlock, editGroup);
								}
							} else if (tempNode instanceof IfStatement) {  //TODO Move this outside of return statement?
								IfStatement ifStatement= (IfStatement) tempNode;
								Statement elseStatement= ifStatement.getElseStatement();
								if (elseStatement != null && ifStatement.getThenStatement() != null && !ifStatement.getThenStatement().equals(parentOfMethodCall)) {
									if (locationOfNewBlocks.containsKey(elseStatement)) {
										myBlock= locationOfNewBlocks.get(elseStatement);
									} else {
										myBlock= ast.newBlock();
										locationOfNewBlocks.put(elseStatement, myBlock);
										blockWithoutBraces.put(myBlock, elseStatement);
									}
									fSingleElseStatement= elseStatement;
									ListRewrite listRewriteForBlock= scratchRewriter.getListRewrite(myBlock, Block.STATEMENTS_PROPERTY);
									scratchRewriter.replace(elseStatement, myBlock, editGroup);
									listRewriteForBlock.insertLast(taskDeclStatement, editGroup);									
								} else {
									Statement thenStatement= ifStatement.getThenStatement();
									if (thenStatement != null && thenStatement.equals(parentOfMethodCall)) {
										if (locationOfNewBlocks.containsKey(thenStatement)) {
											myBlock= locationOfNewBlocks.get(thenStatement);
										} else {
											myBlock= ast.newBlock();
											locationOfNewBlocks.put(thenStatement, myBlock);
											blockWithoutBraces.put(myBlock, thenStatement);
										}
										fSingleElseStatement= thenStatement;
										ListRewrite listRewriteForBlock= scratchRewriter.getListRewrite(myBlock, Block.STATEMENTS_PROPERTY);
										scratchRewriter.replace(thenStatement, myBlock, editGroup);
										listRewriteForBlock.insertLast(taskDeclStatement, editGroup);
									} else {
										return false;
									}
								}
							}  //TODO Add another case for when block is higher up?
							Expression exprTemp= ((ReturnStatement) parentOfMethodCall).getExpression();
							if (exprTemp instanceof InfixExpression) {
								fInfixExpressionFlag= true;
							} else if (exprTemp instanceof MethodInvocation) {
								fMethodInvocationFlag= true;
							} else {
								System.err.println(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_scenario_error + parentOfMethodCall.toString() );
								return false;
							}
						} else {
							System.err.println(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_scenario_error + parentOfMethodCall.toString() );
							return false;
						}
					}
					if (myBlock == null) {
						ASTNode tempNode= parentOfMethodCall;
						do {
							tempNode= tempNode.getParent();
						} while (tempNode != null && !Block.class.isInstance(tempNode) && !SwitchStatement.class.isInstance(tempNode));
						if (tempNode == null) {
							return false;
						} else if (tempNode instanceof SwitchStatement) {
							switchStatementFound[0]= true;
							return false;
						} else {
							myBlock= (Block) tempNode;
						}
					}
					tasksToBlock.put(new Integer(taskNumber[0]), myBlock);
					if (numTasksPerBlock.containsKey(myBlock)) {
						Integer newValue= new Integer(numTasksPerBlock.get(myBlock) + 1);
						numTasksPerBlock.put(myBlock, newValue);
					} else {
						numTasksPerBlock.put(myBlock, new Integer(1));
					}
					statementsWithRecursiveMethodInvocation.add(parentOfMethodCall);
				}
				return true;
			}
		});
		try {
			if (statementsWithRecursiveMethodInvocation.size() <= 1) {
				createFatalError(result, Messages.format(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_statement_error, new String[] {fMethod.getElementName()}));
				return;
			} else if (switchStatementFound[0]) {
				createFatalError(result, Messages.format(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_switch_statement_error, new String[] {fMethod.getElementName()}));
				return;
			}
			Collection<Integer> numBlockTasks= numTasksPerBlock.values();
			Iterator<Integer> taskIter= numBlockTasks.iterator();
			while (taskIter.hasNext()) {
				if (taskIter.next().equals(Integer.valueOf(1))) {
					createFatalError(result, ConcurrencyRefactorings.ConvertToFJTaskRefactoring_multiple_block_error);
					return;
				}
			}
			//TODO Make a separate fSingleElseStatement thing for each block (potentially)
			//start loop
			ListRewrite listRewriteForBlock= scratchRewriter.getListRewrite(tasksToBlock.get(Integer.valueOf(1)), Block.STATEMENTS_PROPERTY);
			
			MethodInvocation forkJoinInvocation= ast.newMethodInvocation();
			forkJoinInvocation.setName(ast.newSimpleName("invokeAll")); //$NON-NLS-1$
			List<Expression> argumentsForkJoin= forkJoinInvocation.arguments();
			for (int i= 1; i <= taskNumber[0]; i++) {
				argumentsForkJoin.add(ast.newSimpleName("task" + i)); //$NON-NLS-1$
			}
			Statement lastStatementWithRecursiveCall;
			if (fSingleElseStatement == null) {
				lastStatementWithRecursiveCall= statementsWithRecursiveMethodInvocation.get(statementsWithRecursiveMethodInvocation.size() - 1);
			} else {
				lastStatementWithRecursiveCall= fSingleElseStatement;
			}
			
			if (!recursiveMethodReturnsVoid()) {
				if (partialComputationsNames.size() >= 1) {
					createPartialComputations(editGroup, scratchRewriter, partialComputationsNames, typesOfComputations, listRewriteForBlock, lastStatementWithRecursiveCall);
				}
				
				Statement lastStatementInBlock;
				if (fSingleElseStatement == null) {
					List<ASTNode> statementsInBlockWithTaskDecl= tasksToBlock.get(Integer.valueOf(1)).statements();
					lastStatementInBlock= (Statement) statementsInBlockWithTaskDecl.get(statementsInBlockWithTaskDecl.size() - 1);
				} else {
					lastStatementInBlock= fSingleElseStatement;
				}
				if (lastStatementInBlock instanceof ReturnStatement) {
					int errorFlag= createLastStatement(ast, result, editGroup, scratchRewriter, listRewriteForBlock, lastStatementInBlock);
					if(errorFlag == -1) {
						return;
					}
				}
			}
			if (fSingleElseStatement == null) {
				if (fInfixExpressionFlag || fMethodInvocationFlag) {
					listRewriteForBlock.insertBefore(ast.newExpressionStatement(forkJoinInvocation), lastStatementWithRecursiveCall, editGroup);
				} else {
					listRewriteForBlock.insertAfter(ast.newExpressionStatement(forkJoinInvocation), lastStatementWithRecursiveCall, editGroup);
				}
			} else {
					listRewriteForBlock.insertAt(ast.newExpressionStatement(forkJoinInvocation), tasksToBlock.size(), editGroup);
			}
			//end loop
			tryApplyEdits(ast, computeMethod, scratchRewriter);
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		recursiveActionSubtype.bodyDeclarations().add(computeMethod);
	}

	private void tryApplyEdits(AST ast, MethodDeclaration computeMethod, final ASTRewrite scratchRewriter) throws JavaModelException {
		TextEdit edits= scratchRewriter.rewriteAST();
		IDocument scratchDocument= new Document(((ICompilationUnit)fRoot.getJavaElement()).getSource());
		try {
			edits.apply(scratchDocument);
			
			ASTParser parser= ASTParser.newParser(AST.JLS4);
			parser.setSource(scratchDocument.get().toCharArray());
			CompilationUnit scratchCU= (CompilationUnit)parser.createAST(null);
			final TypeDeclaration[] declaringClass= new TypeDeclaration[1];
			scratchCU.accept(new ASTVisitor() {
				@Override
				public boolean visit(TypeDeclaration typedecl){
					if (typedecl.getName().getIdentifier().equals(fMethod.getDeclaringType().getElementName())) {
						declaringClass[0]= typedecl;
					}
					return true;
				}
			});
			MethodDeclaration[] methodsInRefactoredClass= declaringClass[0].getMethods();
			for (MethodDeclaration methodDeclaration : methodsInRefactoredClass) {
				if (methodDeclaration.getName().getIdentifier().equals(fMethodDeclaration.getName().getIdentifier())
						&& methodsHaveSameSignature(methodDeclaration,fMethodDeclaration)) {
					Block block= methodDeclaration.getBody();
					Block copySubtree= (Block) ASTNode.copySubtree(ast, block);
					computeMethod.setBody(copySubtree);
					break;
				}
			}
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	private int createLastStatement(AST ast, RefactoringStatus result, final TextEditGroup editGroup, final ASTRewrite scratchRewriter, ListRewrite listRewriteForBlock, Statement lastStatementInBlock) {
		if (fInfixExpressionFlag) {
			Assignment assignToResult= ast.newAssignment();
			assignToResult.setLeftHandSide(ast.newSimpleName("result")); //$NON-NLS-1$
			InfixExpression infixExpression= ((InfixExpression)(ASTNode.copySubtree(ast, ((ReturnStatement)lastStatementInBlock).getExpression())));
			int taskNum= 1;
			infixExpression.setLeftOperand(ast.newQualifiedName(ast.newSimpleName("task" + taskNum++), ast.newSimpleName("result"))); //$NON-NLS-1$ //$NON-NLS-2$
			infixExpression.setRightOperand(ast.newQualifiedName(ast.newSimpleName("task" + taskNum++), ast.newSimpleName("result"))); //$NON-NLS-1$ //$NON-NLS-2$
			List<Expression> extendedOperands = infixExpression.extendedOperands();
			for (int i= 0; i < extendedOperands.size(); ) {
				extendedOperands.set(i, ast.newQualifiedName(ast.newSimpleName("task" + taskNum++), ast.newSimpleName("result"))); //$NON-NLS-1$ //$NON-NLS-2$
				i++;
			}
			assignToResult.setRightHandSide(infixExpression);
			if (fSingleElseStatement == null) {
				scratchRewriter.replace(lastStatementInBlock, ast.newExpressionStatement(assignToResult), editGroup);
			} else {
				listRewriteForBlock.insertLast(ast.newExpressionStatement(assignToResult), editGroup);
			}
		}
		else if (fMethodInvocationFlag) {
			Assignment assignToResult= ast.newAssignment();
			assignToResult.setLeftHandSide(ast.newSimpleName("result")); //$NON-NLS-1$
			ASTNode tempAST= ASTNode.copySubtree(ast, ((ReturnStatement)lastStatementInBlock).getExpression());
			if (!(tempAST instanceof MethodInvocation)) {
				createFatalError(result, Messages.format(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_analyze_error, new String[] {fMethod.getElementName()}));
				return -1;
			}
			MethodInvocation methodInvocation= ((MethodInvocation) tempAST);
			int taskNum= 1;
			List<Expression> methodArguments= methodInvocation.arguments();
			for (int index= 0; index < methodArguments.size(); ) {
				methodArguments.set(index++, ast.newQualifiedName(ast.newSimpleName("task" + taskNum++), ast.newSimpleName("result"))); //$NON-NLS-1$ //$NON-NLS-2$
			}
			assignToResult.setRightHandSide(methodInvocation);
			if (fSingleElseStatement == null) {
				scratchRewriter.replace(lastStatementInBlock, ast.newExpressionStatement(assignToResult), editGroup);
			} else {
				listRewriteForBlock.insertLast(ast.newExpressionStatement(assignToResult), editGroup);
			}
		}
		else {
			Assignment assignToResult= ast.newAssignment();
			assignToResult.setLeftHandSide(ast.newSimpleName("result")); //$NON-NLS-1$
			assignToResult.setRightHandSide((Expression) ASTNode.copySubtree(ast, ((ReturnStatement) lastStatementInBlock).getExpression()));
			if (fSingleElseStatement == null) {
				scratchRewriter.replace(lastStatementInBlock, ast.newExpressionStatement(assignToResult), editGroup);
			} else {
				listRewriteForBlock.insertLast(ast.newExpressionStatement(assignToResult), editGroup);
			}
		}
		return 0;
	}

	private void createPartialComputations(final TextEditGroup editGroup, final ASTRewrite scratchRewriter, final List<String> partialComputationsNames, final List<String> typesOfComputations,
			ListRewrite listRewriteForBlock, Statement lastStatementWithRecursiveCall) {
		if (lastStatementWithRecursiveCall instanceof VariableDeclarationStatement) {
			for (int i= partialComputationsNames.size() - 1; i >= 0 ; ) {
				String varStatement= typesOfComputations.get(i) + " " + partialComputationsNames.get(i) + " = task" + (i + 1) + ".result;"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				VariableDeclarationStatement variableStatement= (VariableDeclarationStatement) scratchRewriter.createStringPlaceholder(varStatement, ASTNode.VARIABLE_DECLARATION_STATEMENT);
				if (fSingleElseStatement == null) {
					listRewriteForBlock.insertAfter(variableStatement, lastStatementWithRecursiveCall, editGroup);
				} else {
					listRewriteForBlock.insertLast(variableStatement, editGroup);
				}
				i--;
			}
		} else if (lastStatementWithRecursiveCall instanceof ExpressionStatement) {
			for (int i= partialComputationsNames.size() - 1; i >= 0 ; ) {
				String varStatement= partialComputationsNames.get(i) + " = task" + (i + 1) + ".result;"; //$NON-NLS-1$ //$NON-NLS-2$
				ExpressionStatement exprStatement= (ExpressionStatement) scratchRewriter.createStringPlaceholder(varStatement, ASTNode.EXPRESSION_STATEMENT);
				if (fSingleElseStatement == null) {
					listRewriteForBlock.insertAfter(exprStatement, lastStatementWithRecursiveCall, editGroup);
				} else {
					listRewriteForBlock.insertLast(exprStatement, editGroup);
				}
				i--;
			}
		}
	}

	private void doRecursionBaseCaseReturn(AST ast, final TextEditGroup editGroup, Statement recursionBaseCaseBranch, final ASTRewrite scratchRewriter) {
		Block basecaseBlock= ast.newBlock();
		List<ASTNode> basecaseStatements= basecaseBlock.statements();
		if (recursiveMethodReturnsVoid()) {
			ExpressionStatement sequentialMethodInvocation= ast.newExpressionStatement(createSequentialMethodInvocation(ast));
			basecaseStatements.add(sequentialMethodInvocation);
		} else {
			Assignment assignmentToResult= ast.newAssignment();
			assignmentToResult.setLeftHandSide(ast.newSimpleName("result")); //$NON-NLS-1$
			assignmentToResult.setRightHandSide(createSequentialMethodInvocation(ast));
			ExpressionStatement newExpressionStatement= ast.newExpressionStatement(assignmentToResult);
			basecaseStatements.add(newExpressionStatement);
		}
		basecaseStatements.add(ast.newReturnStatement());
		scratchRewriter.replace(recursionBaseCaseBranch, basecaseBlock, editGroup);
	}

	private void doRecursionBaseCaseBlock(AST ast, final TextEditGroup editGroup, Statement recursionBaseCaseBranch, final ASTRewrite scratchRewriter) {
		Block baseCaseBlock= (Block) recursionBaseCaseBranch;
		List<ASTNode> statementsInBaseCase= baseCaseBlock.statements();
		ASTNode lastStatementInBaseCase= statementsInBaseCase.get(statementsInBaseCase.size() - 1 );
		if (recursiveMethodReturnsVoid()) {
			ExpressionStatement sequentialMethodInvocation= ast.newExpressionStatement(createSequentialMethodInvocation(ast));
			ListRewrite listRewriteForBaseBlock= scratchRewriter.getListRewrite(baseCaseBlock, Block.STATEMENTS_PROPERTY);
			listRewriteForBaseBlock.insertBefore(sequentialMethodInvocation, lastStatementInBaseCase, editGroup);
		} else {
			Assignment assignmentToResult= ast.newAssignment();
			assignmentToResult.setLeftHandSide(ast.newSimpleName("result")); //$NON-NLS-1$
			assignmentToResult.setRightHandSide(createSequentialMethodInvocation(ast));
			ExpressionStatement newExpressionStatement= ast.newExpressionStatement(assignmentToResult);
			
			ListRewrite listRewriteForBaseBlock= scratchRewriter.getListRewrite(baseCaseBlock, Block.STATEMENTS_PROPERTY);
			listRewriteForBaseBlock.insertBefore(newExpressionStatement, lastStatementInBaseCase, editGroup);
			ReturnStatement newReturnResult= ast.newReturnStatement();
			scratchRewriter.replace(lastStatementInBaseCase, newReturnResult, editGroup);
		}
	}

	private void createFatalError(RefactoringStatus result, String message) {
		RefactoringStatus fatalError= new RefactoringStatus();
		fatalError.addFatalError(message);
		result.merge(fatalError);
	}

	Statement findParentStatement(MethodInvocation methodCall) {
		
		Statement  parentOfMethodCall= null;
		ASTNode tempNode= methodCall;
		do {
			tempNode= tempNode.getParent();
		} while (tempNode != null && !VariableDeclarationStatement.class.isInstance(tempNode));
		if (tempNode != null) {
			parentOfMethodCall= (VariableDeclarationStatement) tempNode;
		} else {
			tempNode= methodCall;
			do {
				tempNode= tempNode.getParent();
			} while (tempNode != null && !ExpressionStatement.class.isInstance(tempNode));
			if (tempNode != null) {
				parentOfMethodCall= (ExpressionStatement) tempNode;
			} else {
				tempNode= methodCall;
				do {
					tempNode= tempNode.getParent();
				} while (tempNode != null && !ReturnStatement.class.isInstance(tempNode));
				if (tempNode != null) {
					parentOfMethodCall= (ReturnStatement) tempNode;
				}
			}
		}
		return parentOfMethodCall;	
	}

	private boolean methodsHaveSameSignature(
			MethodDeclaration methodDeclaration,
			MethodDeclaration methodDeclaration2) {
		
		String methodArguments= ConcurrencyRefactorings.ConcurrencyRefactorings_empty_string;
		List<ASTNode> arguments= methodDeclaration.parameters();
		for (Iterator<ASTNode> iterator= arguments.iterator(); iterator
				.hasNext();) {
			ASTNode argument= iterator.next();
			methodArguments += argument.toString();
			if (iterator.hasNext()) {
				methodArguments += ", "; //$NON-NLS-1$
			}
		}
		
		String methodArguments2= ConcurrencyRefactorings.ConcurrencyRefactorings_empty_string;
		arguments= methodDeclaration2.parameters();
		for (Iterator<ASTNode> iterator= arguments.iterator(); iterator
				.hasNext();) {
			ASTNode argument= iterator.next();
			methodArguments2 += argument.toString();
			if (iterator.hasNext()) {
				methodArguments2 += ", "; //$NON-NLS-1$
			}
		}
		return methodArguments.equals(methodArguments2);
	}


	private MethodInvocation createSequentialMethodInvocation(AST ast) {
		
		MethodInvocation invokeSequentialMethod= ast.newMethodInvocation();
		invokeSequentialMethod.setName(ast.newSimpleName(fMethod.getElementName()));
		List<Expression> argumentsForInvokingSeqMethod= invokeSequentialMethod.arguments();
		List<ASTNode> recursiveMethodParameters= fMethodDeclaration.parameters();
		for (Object par : recursiveMethodParameters) {
			SingleVariableDeclaration parameter= (SingleVariableDeclaration) par;
			argumentsForInvokingSeqMethod.add(ast.newSimpleName(parameter.getName().getIdentifier()));
		}
		return invokeSequentialMethod;
	}

	private Statement identifyRecursionBaseCaseBranch(Block computeBodyBlock) {
		
		final Statement[] baseCase= new Statement[] {null};
		final int[] counter= new int[] {0};
		final boolean[] isFirst= new boolean[] {true};
		computeBodyBlock.accept(new ASTVisitor() {
			@Override
			public boolean visit(IfStatement ifStatement) {
				Statement thenStatement= ifStatement.getThenStatement();
//				Statement elseStatement= ifStatement.getElseStatement();
				if (statementIsBaseCase(thenStatement)) {
					baseCase[0]= thenStatement;
					counter[0]++;
//				} else if ((elseStatement != null) && (statementIsBaseCase(elseStatement))) {
//					baseCase[0]= elseStatement;
//					counter[0]++;
				} else {
					if (isFirst[0] && counter[0] != 1) {
						isFirst[0]= false;
						counter[0]++;
					}
				}
				return false;
			}

			private boolean statementIsBaseCase(Statement statement) {
				
				return statementEndsWithReturn(statement) && !statementContainsRecursiveCall(statement);
			}

			private boolean statementEndsWithReturn(Statement statement) {
				
				if (statement instanceof Block) {
					Block blockStatement= (Block) statement;
					List<ASTNode> statements= blockStatement.statements();
					if (statements.size() == 0) {
						return false;
					}
					ASTNode lastStatement= statements.get(statements.size() - 1);
					if ((lastStatement instanceof ReturnStatement)) {
						return true;
					}
				} else if (statement instanceof ReturnStatement) {
					return true;
				}
				return false;
			}
		});
		if (counter[0] == 1 && isFirst[0]) {
			return baseCase[0];
		} else {
			return null;
		}
	}

	private boolean statementContainsRecursiveCall(Statement statement) {
		
		final boolean[] result= new boolean[] {false};
		statement.accept(new ASTVisitor() {
			@Override
			public boolean visit(MethodInvocation methodCall) {
				IMethodBinding bindingForMethodCall= methodCall.resolveMethodBinding();
				IMethodBinding bindingForMethodDeclaration= fMethodDeclaration.resolveBinding();
				if (bindingForMethodCall.isEqualTo(bindingForMethodDeclaration)) {
					result[0]= true;
				}
				return true;
			}
		});
		return result[0];
	}
	
	private void createConstructor(TypeDeclaration recursiveActionSubtype, AST ast) {
		
		MethodDeclaration newConstructor= ast.newMethodDeclaration();
		newConstructor.setConstructor(true);
		newConstructor.setName(ast.newSimpleName(nameForFJTaskSubtype));
		List<ASTNode> constructorParameters= newConstructor.parameters();
		List<ASTNode> recursiveMethodParameters= fMethodDeclaration.parameters();
		for (Object par : recursiveMethodParameters) {
			SingleVariableDeclaration parameter= (SingleVariableDeclaration) par;
			constructorParameters.add(ASTNode.copySubtree(ast, parameter));
		}
		
		newConstructor.modifiers().add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
		
		Block newConstructorBlock= ast.newBlock();
		newConstructor.setBody(newConstructorBlock);
		
		List<ASTNode> newConstructorStatements= newConstructorBlock.statements();
		for (Object par : recursiveMethodParameters) {
			SingleVariableDeclaration parameter= (SingleVariableDeclaration) par;
			Assignment newAssignment= ast.newAssignment();
			FieldAccess newFieldAccess= ast.newFieldAccess();
			newFieldAccess.setExpression(ast.newThisExpression());
			newFieldAccess.setName(ast.newSimpleName(parameter.getName().getIdentifier()));
			newAssignment.setLeftHandSide(newFieldAccess);
			
			newAssignment.setRightHandSide(ast.newSimpleName(parameter.getName().getIdentifier()));
			
			ExpressionStatement newExpressionStatement= ast.newExpressionStatement(newAssignment);
			newConstructorStatements.add(newExpressionStatement);
		}	
		recursiveActionSubtype.bodyDeclarations().add(newConstructor);
	}

	private void createFields(TypeDeclaration recursiveActionSubtype, AST ast) {
		
		List<ASTNode> recursiveMethodParameters= fMethodDeclaration.parameters();
		for (Object par : recursiveMethodParameters) {
			SingleVariableDeclaration parameter= (SingleVariableDeclaration) par;
			
			VariableDeclarationFragment newDeclarationFragment= ast.newVariableDeclarationFragment();
			newDeclarationFragment.setName(ast.newSimpleName(parameter.getName().getIdentifier()));
			
			FieldDeclaration newFieldDeclaration= ast.newFieldDeclaration(newDeclarationFragment);
			newFieldDeclaration.setType((Type) ASTNode.copySubtree(ast, parameter.getType()));
			List<Modifier> modifiers= newFieldDeclaration.modifiers();
			modifiers.add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
			
			recursiveActionSubtype.bodyDeclarations().add(newFieldDeclaration);
		}
		
		if (!recursiveMethodReturnsVoid()) {
			VariableDeclarationFragment newDeclarationFragment= ast.newVariableDeclarationFragment();
			newDeclarationFragment.setName(ast.newSimpleName("result")); //$NON-NLS-1$
			
			FieldDeclaration newFieldDeclaration= ast.newFieldDeclaration(newDeclarationFragment);
			newFieldDeclaration.setType((Type) ASTNode.copySubtree(ast, fMethodDeclaration.getReturnType2()));
			List<Modifier> modifiers= newFieldDeclaration.modifiers();
			modifiers.add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
			
			recursiveActionSubtype.bodyDeclarations().add(newFieldDeclaration);
		}
	}
	
	boolean recursiveMethodReturnsVoid() {
		
		Type returnType= fMethodDeclaration.getReturnType2();
		if (returnType == null) {
			return true;
		}
		return (returnType.isPrimitiveType() && ((PrimitiveType)returnType).getPrimitiveTypeCode().equals(PrimitiveType.VOID));
	}

	private ChildListPropertyDescriptor getBodyDeclarationsProperty(ASTNode declaration) {
		
		if (declaration instanceof AnonymousClassDeclaration) {
			return AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY;
		} else if (declaration instanceof AbstractTypeDeclaration) {
			return ((AbstractTypeDeclaration) declaration).getBodyDeclarationsProperty();
		}
		Assert.isTrue(false);
		return null;
	}
	
	private void createEdits(ICompilationUnit unit, ASTRewrite rewriter, List<TextEditGroup> groups, ImportRewrite importRewrite) throws CoreException {
		
		TextChange change= fChangeManager.get(unit);
		MultiTextEdit root= new MultiTextEdit();
		change.setEdit(root);
		
		TextEdit importEdit= importRewrite.rewriteImports(null);
		TextChangeCompatibility.addTextEdit(fChangeManager.get(unit), ConcurrencyRefactorings.ConcurrencyRefactorings_update_imports, importEdit);
		
		root.addChild(rewriter.rewriteAST());
		for (Iterator<TextEditGroup> iter= groups.iterator(); iter.hasNext();) {
			change.addTextEditGroup(iter.next());
		}
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		
		RefactoringStatus result=  new RefactoringStatus();
		result.merge(Checks.checkAvailability(fMethod));
		
		if (result.hasFatalError()) {
			return result;
		}
		
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(false);
		parser.setBindingsRecovery(false);
		parser.setSource(fMethod.getCompilationUnit());
		parser.setCompilerOptions(getCompilerOptions(fMethod.getCompilationUnit()));
		fRoot= (CompilationUnit) parser.createAST(pm);
		ISourceRange sourceRange= fMethod.getNameRange();
		ASTNode node= NodeFinder.perform(fRoot, sourceRange.getOffset(), sourceRange.getLength());
		if (node == null) {
			return mappingErrorFound(result, node);
		}
		do {
			node= node.getParent();
		} while (node != null && !MethodDeclaration.class.isInstance(node));
		fMethodDeclaration= (MethodDeclaration) node;
		if (fMethodDeclaration == null) {
			return mappingErrorFound(result, node);
		}
		if (fMethodDeclaration.resolveBinding() == null) {
			if (!processCompilerError(result, node)) {
				result.addFatalError(ConcurrencyRefactorings.ConcurrencyRefactorings_type_error);
			}
			return result;
		}
		fRewriter= ASTRewrite.create(fRoot.getAST());
		return result;
	}
	
	private RefactoringStatus mappingErrorFound(RefactoringStatus result, ASTNode node) {
		
		if (node != null && (node.getFlags() & ASTNode.MALFORMED) != 0 && processCompilerError(result, node)) {
			return result;
		}
		result.addFatalError(getMappingErrorMessage());
		return result;
	}
	
	private String getMappingErrorMessage() {
		
		return MessageFormat.format(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_analyze_error,
				((Object[]) new String[] {fMethod.getElementName()}));
	}

	private boolean processCompilerError(RefactoringStatus result, ASTNode node) {
		
		Message[] messages= MessageUtil.getMessages(node, MessageUtil.INCLUDE_ALL_PARENTS);
		if (messages.length == 0) {
			return false;
		}
		createFatalError(result, Messages.format(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_compile_error,
				(new String[] { fMethod.getElementName(), messages[0].getMessage()})));
		return true;
	}
	
	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		
		String project= null;
		IJavaProject javaProject= fMethod.getJavaProject();
		if (javaProject != null) {
			project= javaProject.getElementName();
		}
		int flags= JavaRefactoringDescriptor.JAR_MIGRATION | JavaRefactoringDescriptor.JAR_REFACTORING | RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE;
		final IType declaring= fMethod.getDeclaringType();
		try {
			if (declaring.isAnonymous() || declaring.isLocal()) {
				flags|= JavaRefactoringDescriptor.JAR_SOURCE_ATTACHMENT;
			}
		} catch (JavaModelException exception) {
			JavaPlugin.log(exception);
		}

		final Map<String, String> arguments= new HashMap<String, String>();
		String description= ConcurrencyRefactorings.ConvertToFJTaskRefactoring_name_user;
		final JDTRefactoringDescriptorComment comment= new JDTRefactoringDescriptorComment(project, this, Messages.format(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_descriptor_description, new String[] { JavaElementLabels.getTextLabel(fMethod, JavaElementLabels.ALL_FULLY_QUALIFIED), JavaElementLabels.getTextLabel(declaring, JavaElementLabels.ALL_FULLY_QUALIFIED)}));
		comment.addSetting(Messages.format(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_method_pattern, BasicElementLabels.getJavaElementName(fMethod.getElementName())));
		
		final FJTaskRefactoringDescriptor descriptor= RefactoringSignatureDescriptorFactory.createFJTaskRefactoringDescriptor(project, description, comment.asString(), arguments, flags);
		
		arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_INPUT, JavaRefactoringDescriptorUtil.elementToHandle(project, fMethod));
		arguments.put(JavaRefactoringDescriptorUtil.ATTRIBUTE_NAME, fMethod.getElementName());
		
		final DynamicValidationRefactoringChange result= new DynamicValidationRefactoringChange(descriptor, getName());
		TextChange[] changes= fChangeManager.getAllChanges();
		pm.beginTask(NO_NAME, changes.length);
		pm.setTaskName(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_create_changes);
		for (int i= 0; i < changes.length; i++) {
			result.add(changes[i]);
			pm.worked(1);
		}
		pm.done();
		return result;
	}

	@Override
	public String getName() {
		
		return ConcurrencyRefactorings.ConvertToFJTaskRefactoring_name_official;
	}

	public void setMethod(IMethod method) {
		
		this.fMethod= method;
	}

	public RefactoringStatus setFieldName(String text) {
		
		// TODO Auto-generated method stub
		return null;
	}
	

	public IMethod getMethod() {
		
		return fMethod;
	}

	public String getMethodName() {
		
		return fMethod.getElementName();
	}

	public String getNameForFJTaskSubtype() {
		
		return nameForFJTaskSubtype;
	}

	public RefactoringStatus setNameForFJTaskSubtype(String nameForFJTaskSubtype) {
		
		this.nameForFJTaskSubtype= nameForFJTaskSubtype;
		return new RefactoringStatus();
	}
	
	private boolean isIgnorableProblem(IProblem problem) {
		
		if (problem.getID() == IProblem.NotVisibleField) {
			return true;
		}
		return false;
	}
	
	private void checkCompileErrors(RefactoringStatus result, CompilationUnit root, ICompilationUnit element) {
		
		IProblem[] messages= root.getProblems();
		for (int i= 0; i < messages.length; i++) {
			IProblem problem= messages[i];
			if (!isIgnorableProblem(problem)) {
				result.addError(MessageFormat.format(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_compile_error_update,
						(new Object[] { element.getElementName()})), new CompilationUnitSourceContext(element, null));
				return;
			}
		}
	}

	public String suggestTaskName() {
		
		String methodName= fMethod.getElementName();
		return methodName.substring(0, 1).toUpperCase() + methodName.substring(1, methodName.length()) + "Impl"; //$NON-NLS-1$
	}
	
	public String suggestSequentialThreshold() {
		
		List<SingleVariableDeclaration> params= fMethodDeclaration.parameters();
		String sugSeqThreshold= ConcurrencyRefactorings.ConcurrencyRefactorings_empty_string;
		if (params != null && params.size() > 0) {
			SingleVariableDeclaration parameter = params.get(0);
			Type type = parameter.getType();
			if (type.isPrimitiveType()) {
				sugSeqThreshold= parameter.getName().getIdentifier() + " < 10"; //$NON-NLS-1$
			} else if (type.isArrayType()) {
				sugSeqThreshold= parameter.getName().getIdentifier() + ".length < 10"; //$NON-NLS-1$
			} else if (type.isParameterizedType()) {
				sugSeqThreshold= parameter.getName().getIdentifier() + ".size() < 10"; //$NON-NLS-1$
			} else if (type.isSimpleType()) {
				sugSeqThreshold= parameter.getName().getIdentifier() + ".length() < 10"; //$NON-NLS-1$
			} else {
				System.err.println(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_parameter_error);
			}
		}
		
		return sugSeqThreshold;
	}

	public RefactoringStatus setSequentialThreshold(String text) {
		
		if (text == null || ConcurrencyRefactorings.ConcurrencyRefactorings_empty_string.equals(text)) {
			return RefactoringStatus.createErrorStatus(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_sequential_req);
		}
		sequentialThreshold= text;
		return new RefactoringStatus();
	}

	public String getMethodNameAndSignature() {
		
		String nameAndSignature= fMethodDeclaration.getName().getIdentifier() + "("; //$NON-NLS-1$
		List<SingleVariableDeclaration> recursiveMethodParameters= castList(SingleVariableDeclaration.class, fMethodDeclaration.parameters());
		for (Iterator<SingleVariableDeclaration> iterator= recursiveMethodParameters.iterator(); iterator
				.hasNext();) {
			SingleVariableDeclaration parameter= iterator.next();
			nameAndSignature += parameter.getType() + " " + parameter.getName().getIdentifier(); //$NON-NLS-1$
			if (iterator.hasNext()) {
				nameAndSignature +=", "; //$NON-NLS-1$
			}
		}
		nameAndSignature += ")"; //$NON-NLS-1$
		return nameAndSignature;
	}	
	
	private <T> List<T> castList(Class<? extends T> toCastTo, List<?> c) {
		
		if (c == null || c.size() == 0) {
			return (List<T>) c;
		} else {
			List<T> tempList = new ArrayList<T>(c.size());
			for (Object objCast: c) {
				tempList.add(toCastTo.cast(objCast));
			}
			return tempList;
		}
	}
	
	private Map<String, String> getCompilerOptions(IJavaElement element) {
		
		IJavaProject project= element.getJavaProject();
		Map<String, String> options= project.getOptions(true);
		for (Iterator<String> iter= options.keySet().iterator(); iter.hasNext();) {
			String key= iter.next();
			String value= options.get(key);
			if (JavaCore.ERROR.equals(value) || JavaCore.WARNING.equals(value)) {
				// System.out.println("Ignoring - " + key);
				options.put(key, JavaCore.IGNORE);
			}
		}
		options.put(JavaCore.COMPILER_PB_MAX_PER_UNIT, "0"); //$NON-NLS-1$
		options.put(JavaCore.COMPILER_TASK_TAGS, ConcurrencyRefactorings.ConcurrencyRefactorings_empty_string);
		return options;
	}
}