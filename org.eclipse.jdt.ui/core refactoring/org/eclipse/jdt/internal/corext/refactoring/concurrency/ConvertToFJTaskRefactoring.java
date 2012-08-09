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
import org.eclipse.jdt.core.dom.ArrayType;
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
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.ParameterizedType;
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
		
		createProcessorCount(newStatements);
		
		createForkJoinPool(newStatements);
		
		String FJTaskSubtypeName= createTaskSubtype(newStatements, ast);
		
		createInvocationOfPool(newStatements, FJTaskSubtypeName);
		
		fRewriter.replace(originalBody, newMethodBody, gd);
		
		ArrayList<TextEditGroup> group= new ArrayList<TextEditGroup>();
		group.add(gd);
		return group;
	}

	private void createInvocationOfPool(List<ASTNode> newStatements, String taskInstanceName) {
		String poolInvoke= "pool.invoke(" + taskInstanceName +");"; //$NON-NLS-1$ //$NON-NLS-2$
		if (!recursiveMethodReturnsVoid()) {
			poolInvoke= "return " + poolInvoke; //$NON-NLS-1$
		}
		ASTNode poolInvokeNode= fRewriter.createStringPlaceholder(poolInvoke, ASTNode.EXPRESSION_STATEMENT);
		newStatements.add(poolInvokeNode);
	}

	private String createTaskSubtype(List<ASTNode> newStatements, AST ast) {
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
		return taskInstanceName;
	}

	private void createForkJoinPool(List<ASTNode> newStatements) {
		String pool= new String("ForkJoinPool pool = new ForkJoinPool(processorCount);"); //$NON-NLS-1$
		ASTNode poolNode= fRewriter.createStringPlaceholder(pool, ASTNode.EXPRESSION_STATEMENT);
		newStatements.add(poolNode);
	}

	private void createProcessorCount(List<ASTNode> newStatements) {
		String declareNumOfAvailableResource= "int processorCount = Runtime.getRuntime().availableProcessors();"; //$NON-NLS-1$
		ASTNode declNumOfAvailableResources= fRewriter.createStringPlaceholder(declareNumOfAvailableResource, ASTNode.EXPRESSION_STATEMENT);
		newStatements.add(declNumOfAvailableResources);
	}

	private void addImports(ImportRewrite importRewrite) {
		
		importRewrite.addImport("java.util.concurrent.ForkJoinPool"); //$NON-NLS-1$
		if (recursiveMethodReturnsVoid()) {
			importRewrite.addImport("java.util.concurrent.RecursiveAction"); //$NON-NLS-1$
		} else {
			importRewrite.addImport("java.util.concurrent.RecursiveTask"); //$NON-NLS-1$
		}
	}

	private Collection<TextEditGroup> addCreateTaskClass(CompilationUnit root, RefactoringStatus result) {
		
		TextEditGroup gd= new TextEditGroup(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_recursive_subtype);
		
		AST ast= root.getAST();
		TypeDeclaration recursiveActionSubtype= ast.newTypeDeclaration();
		
		recursiveActionSubtype.setName(ast.newSimpleName(nameForFJTaskSubtype));
		setSuperClass(ast, recursiveActionSubtype);
		
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

	private void setSuperClass(AST ast, TypeDeclaration recursiveActionSubtype) {
		if (recursiveMethodReturnsVoid()) {
			recursiveActionSubtype.setSuperclassType(ast.newSimpleType(ast.newSimpleName("RecursiveAction")));	//$NON-NLS-1$
		} else {
			ParameterizedType superClass= ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("RecursiveTask"))); //$NON-NLS-1$
			superClass.typeArguments().add(getReturnType(ast));
			recursiveActionSubtype.setSuperclassType(superClass);
		}
	}

	private Type getReturnType(AST ast) {
		Type returnType= fMethodDeclaration.getReturnType2();
		String returnTypeName= returnType.resolveBinding().getName();
		if (returnType.isPrimitiveType()) {
			return ast.newSimpleType(ast.newSimpleName(primitiveTypeToWrapper(returnTypeName)));
		} else if (returnType.isArrayType()) {
			Type tempComponent= (Type) ASTNode.copySubtree(ast, ((ArrayType) returnType).getComponentType());
			return ast.newArrayType(tempComponent);
		} else if (returnType.isParameterizedType()) {
			return ast.newParameterizedType(returnType);
		} else if (returnType.isQualifiedType()) {
			return ast.newQualifiedType(returnType, ast.newSimpleName(returnTypeName));  //TODO check these below
		} else if (returnType.isSimpleType()) {
			return ast.newSimpleType(ast.newName(returnTypeName));
		} else if (returnType.isUnionType()) {
			return ast.newUnionType(); 
		} else if (returnType.isWildcardType()) {
			return ast.newWildcardType();
		} else {
			return null;
		}
	}

	private String primitiveTypeToWrapper(String typeName) {
		if (typeName.equals("int")) { //$NON-NLS-1$
			return "Integer"; //$NON-NLS-1$
		} else if (typeName.equals("boolean")) { //$NON-NLS-1$
			return "Boolean";  //$NON-NLS-1$
		} else if (typeName.equals("long")) { //$NON-NLS-1$
			return "Long";  //$NON-NLS-1$
		} else if (typeName.equals("double")) { //$NON-NLS-1$
			return "Double";  //$NON-NLS-1$
		} else if (typeName.equals("char")) { //$NON-NLS-1$
			return "Char";  //$NON-NLS-1$
		} else if (typeName.equals("float")) { //$NON-NLS-1$
			return "Float";  //$NON-NLS-1$
		} else if (typeName.equals("short")) { //$NON-NLS-1$
			return "Short";  //$NON-NLS-1$
		} else if (typeName.equals("byte")) { //$NON-NLS-1$
			return "Byte";  //$NON-NLS-1$
		} else {
			return null;
		}
	}

	private void copyRecursiveMethod(TypeDeclaration recursiveActionSubtype, AST ast, RefactoringStatus result) {
		
		if (fMethodDeclaration.getBody() != null) {
			checkIfCommentWarning(result);
		}
		
		ASTNode copyRecursiveMethod= ASTNode.copySubtree(ast, fMethodDeclaration);
		changeMethodCallsToSequential(copyRecursiveMethod);
		recursiveActionSubtype.bodyDeclarations().add(copyRecursiveMethod);
		
	}

	private void changeMethodCallsToSequential(ASTNode copyRecursiveMethod) {
		final SimpleName methodName= ((MethodDeclaration) copyRecursiveMethod).getName();
		final String newIdentifier= methodName.getIdentifier() + "_sequential"; //$NON-NLS-1$
		copyRecursiveMethod.accept(new ASTVisitor() {
			@Override
			public boolean visit(MethodInvocation methodCall) {
				SimpleName methodCallName= methodCall.getName();
				if (methodName.getIdentifier().equals(methodCallName.getIdentifier())) {
					methodCallName.setIdentifier(newIdentifier);
				}
				return true;
			}
		});
		methodName.setIdentifier(newIdentifier);
	}

	private void checkIfCommentWarning(RefactoringStatus result) {
		int start= fMethodDeclaration.getBody().getStartPosition();
		int end= fMethodDeclaration.getBody().getLength() + start;
		List<Comment> commentList= fRoot.getCommentList();
		if (commentList.size() != 0) {
			for (Comment comment : commentList) {
				int tempStart= comment.getStartPosition();
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
		
		if(methodThrowsExceptions(result) || methodHasNoBody(result)) {
			return;
		}
		
		MethodDeclaration computeMethod= ast.newMethodDeclaration();
		initializeComputeMethod(ast, computeMethod);
		
		final TextEditGroup editGroup= new TextEditGroup(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_generate_compute);
		
		Statement recursionBaseCaseBranch= identifyRecursionBaseCaseBranch(fMethodDeclaration.getBody());
		if (recursionBaseCaseBranch == null) {
			createFatalError(result, Messages.format(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_recursion_error, new String[] {fMethod.getElementName()}));
			return;
		}

		final ASTRewrite scratchRewriter= ASTRewrite.create(fRoot.getAST());
		ASTNode sequentialThresholdCheck= scratchRewriter.createStringPlaceholder(sequentialThreshold, ASTNode.PARENTHESIZED_EXPRESSION); 
		
		replaceBaseCaseCheckWithSequentialThreshold(editGroup, recursionBaseCaseBranch, scratchRewriter, sequentialThresholdCheck);
		
		computeBaseCaseStatements(ast, editGroup, recursionBaseCaseBranch, scratchRewriter);
		
		final Map<Integer, VariableDeclarationStatement> allTaskDeclStatements= new HashMap<Integer, VariableDeclarationStatement>();
		final Map<Statement, List<Integer> > statementsToTasks= new HashMap<Statement, List<Integer> >();
		final Map<Block, List<Statement> > allStatementsWithRecursiveMethodInvocation= new HashMap<Block, List<Statement> >();
		final Map<Block, Integer> numTasksPerBlock= new HashMap<Block, Integer>();  //Can determine how many tasks belong to this block easily
		final Map<Block, Statement> blockWithoutBraces= new HashMap<Block, Statement>();  //Can determine if a block does not have braces so as to use when inserting things to it
		final List<Block> allTheBlocks= new ArrayList<Block>();
		final boolean[] switchStatementsFound= new boolean[] {false};
		fMethodDeclaration.accept(new MethodVisitor(allTaskDeclStatements, statementsToTasks, scratchRewriter, numTasksPerBlock, blockWithoutBraces, allStatementsWithRecursiveMethodInvocation,
				allTheBlocks, switchStatementsFound, ast));
		try {
			if (switchStatementError(result, switchStatementsFound) || tooFewStatementsToRefactor(result, allStatementsWithRecursiveMethodInvocation)) {
				return;
			}
			boolean atLeastOneBlockChanged= false;
			Iterator<Block> blockIter= allTheBlocks.iterator();
			while (blockIter.hasNext()) {
				Block currBlock= blockIter.next();
				ListRewrite listRewriteForBlock= scratchRewriter.getListRewrite(currBlock, Block.STATEMENTS_PROPERTY);
				atLeastOneBlockChanged= (attemptRefactoringOnBlock(ast, editGroup, scratchRewriter, allTaskDeclStatements, statementsToTasks,
						allStatementsWithRecursiveMethodInvocation, numTasksPerBlock, blockWithoutBraces, atLeastOneBlockChanged, currBlock, listRewriteForBlock) || atLeastOneBlockChanged);
			}
			if (!atLeastOneBlockChanged) {
				createFatalError(result, Messages.format(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_no_change_error, new String[] {fMethod.getElementName()}));
				return;
			}
			tryApplyEdits(ast, computeMethod, scratchRewriter);
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		recursiveActionSubtype.bodyDeclarations().add(computeMethod);
	}

	private boolean tooFewStatementsToRefactor(RefactoringStatus result, final Map<Block, List<Statement>> allStatementsWithRecursiveMethodInvocation) {
		if (allStatementsWithRecursiveMethodInvocation.size() == 0) {
			createFatalError(result, Messages.format(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_statement_error, new String[] {fMethod.getElementName()}));
			return true;
		}
		return false;
	}

	private boolean switchStatementError(RefactoringStatus result, final boolean[] switchStatementsFound) {
		if (switchStatementsFound[0]) {
			createFatalError(result, Messages.format(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_switch_statement_error, new String[] {fMethod.getElementName()}));
			return true;
		}
		return false;
	}

	private void replaceBaseCaseCheckWithSequentialThreshold(final TextEditGroup editGroup, Statement recursionBaseCaseBranch, final ASTRewrite scratchRewriter, ASTNode sequentialThresholdCheck) {
		IfStatement enclosingIf= (IfStatement) recursionBaseCaseBranch.getParent();
		scratchRewriter.replace(enclosingIf.getExpression(), sequentialThresholdCheck, editGroup);
	}

	private boolean methodHasNoBody(RefactoringStatus result) {
		if (fMethodDeclaration.getBody() == null) {
			createFatalError(result, Messages.format(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_method_body_error, new String[] {fMethod.getElementName()}));
			return true;
		}
		return false;
	}

	private void initializeComputeMethod(final AST ast, MethodDeclaration computeMethod) {
		computeMethod.setName(ast.newSimpleName("compute")); //$NON-NLS-1$
		computeMethod.modifiers().add(ast.newModifier(ModifierKeyword.PROTECTED_KEYWORD));
		if (!recursiveMethodReturnsVoid()) {
			computeMethod.setReturnType2(getReturnType(ast));
		}
	}

	private boolean methodThrowsExceptions(RefactoringStatus result) {
		List<Name> exceptionList= fMethodDeclaration.thrownExceptions();
		if (exceptionList.size() > 0) {
			createFatalError(result, Messages.format(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_thrown_exception_error, new String[] {fMethod.getElementName()}));
			return true;
		}
		return false;
	}

	private boolean attemptRefactoringOnBlock(final AST ast, final TextEditGroup editGroup, final ASTRewrite scratchRewriter, Map<Integer, VariableDeclarationStatement> allTaskDeclStatements,
			Map<Statement, List<Integer>> statementsToTasks, final Map<Block, List<Statement>> allStatementsWithRecursiveMethodInvocation, final Map<Block, Integer> numTasksPerBlock,
			final Map<Block, Statement> blockWithoutBraces, boolean atLeastOneBlockChanged, Block currBlock, ListRewrite listRewriteForBlock) {

		boolean isNewBlock= blockWithoutBraces.containsKey(currBlock);
		
		if (allStatementsWithRecursiveMethodInvocation.get(currBlock).size() >= 1 && !numTasksPerBlock.get(currBlock).equals(Integer.valueOf(1))) {
			atLeastOneBlockChanged=  true;
			
			if (isNewBlock) {
				scratchRewriter.replace(blockWithoutBraces.get(currBlock), currBlock, editGroup);
			}
			
			MethodInvocation forkJoinInvocation= ast.newMethodInvocation();
			forkJoinInvocation.setName(ast.newSimpleName("invokeAll")); //$NON-NLS-1$
			List<Expression> argumentsForkJoin= forkJoinInvocation.arguments();
			
			List<Statement> recursiveList= allStatementsWithRecursiveMethodInvocation.get(currBlock);
			boolean isNotNewBlock= !isNewBlock;
			Statement lastStatementWithRecursiveCall= recursiveList.get(recursiveList.size() - 1);
			Statement firstStatementWithRecursiveCall= recursiveList.get(0);
			
			processRefactoringForAllStatementsInBlock(ast, editGroup, scratchRewriter, allTaskDeclStatements, statementsToTasks, listRewriteForBlock, argumentsForkJoin, recursiveList, isNotNewBlock,
					firstStatementWithRecursiveCall);
			
			writeForkJoinInvocationInBlock(ast, editGroup, numTasksPerBlock, currBlock, listRewriteForBlock, forkJoinInvocation, isNotNewBlock, firstStatementWithRecursiveCall);
			
			if (!recursiveMethodReturnsVoid() && (lastStatementWithRecursiveCall instanceof ReturnStatement)) {
				refactorReturnStatement(ast, editGroup, scratchRewriter, listRewriteForBlock, lastStatementWithRecursiveCall, isNotNewBlock, statementsToTasks.get(lastStatementWithRecursiveCall));
			}
			if (statementsToAdd.size() > 0) {
				addSavedStatementsAfterLastStatement(editGroup, listRewriteForBlock, lastStatementWithRecursiveCall, statementsToAdd);
			}
			writeForkJoinInvocationInBlock(ast, editGroup, numTasksPerBlock, currBlock, listRewriteForBlock, forkJoinInvocation, isNotNewBlock, lastStatementWithRecursiveCall);
			if (statementsToAdd.size() > 0) { //TODO is this going to run twice or something?
				addSavedStatementsBeforeLastStatement(editGroup, listRewriteForBlock, lastStatementWithRecursiveCall, statementsToAdd);
			}
		}
		else if (!recursiveMethodReturnsVoid()) {
			renameRecursiveCallsToSequentialInNonRefactoredBlock(ast, editGroup, scratchRewriter, blockWithoutBraces, currBlock, isNewBlock);
		}
		return atLeastOneBlockChanged;
	}

	private void processRefactoringForAllStatementsInBlock(final AST ast, final TextEditGroup editGroup, final ASTRewrite scratchRewriter,
			Map<Integer, VariableDeclarationStatement> allTaskDeclStatements, Map<Statement, List<Integer>> statementsToTasks, ListRewrite listRewriteForBlock, List<Expression> argumentsForkJoin,
			List<Statement> recursiveList, boolean isNotNewBlock, Statement firstStatementWithRecursiveCall) {
		Statement currStatement;
		for (int listIndex= 0; listIndex < recursiveList.size(); listIndex++) {
			currStatement= recursiveList.get(listIndex);
			List<Integer> taskList= statementsToTasks.get(currStatement);
			writeTaskDeclStatementsAndAddTaskToForkJoinArguments(ast, editGroup, allTaskDeclStatements, listRewriteForBlock, argumentsForkJoin, isNotNewBlock, taskList, firstStatementWithRecursiveCall);
			if (!(currStatement instanceof ReturnStatement) && !recursiveMethodReturnsVoid()) {
				refactorStatement(ast, editGroup, scratchRewriter, listRewriteForBlock, currStatement, isNotNewBlock, taskList);
			} else if (recursiveMethodReturnsVoid()) {
				scratchRewriter.remove(currStatement, editGroup);
			}
		}
	}
	
	private void renameRecursiveCallsToSequentialInNonRefactoredBlock(final AST ast, final TextEditGroup editGroup, final ASTRewrite scratchRewriter, final Map<Block, Statement> blockWithoutBraces,
			Block currBlock, boolean isNewBlock) {
		if (!isNewBlock) {
			Block newBlock= (Block) ASTNode.copySubtree(ast, currBlock);
			newBlock.accept(new RecursiveToSequentialMethodCallVisitor(ast));
			scratchRewriter.replace(currBlock, newBlock, editGroup);
		} else {
			Statement newStatement= (Statement) ASTNode.copySubtree(ast, blockWithoutBraces.get(currBlock));
			newStatement.accept(new RecursiveToSequentialMethodCallVisitor(ast));
			scratchRewriter.replace(blockWithoutBraces.get(currBlock), newStatement, editGroup);
		}
	}

	private void addSavedStatementsBeforeLastStatement(final TextEditGroup editGroup, ListRewrite listRewriteForBlock, Statement lastStatementWithRecursiveCall, List<ASTNode> statementsToAdd) {
		for (int i= 0; i < statementsToAdd.size(); i++) {
			if (lastStatementWithRecursiveCall instanceof ReturnStatement) {
				listRewriteForBlock.insertBefore(statementsToAdd.get(i), lastStatementWithRecursiveCall, editGroup);
			}
		}
	}

	private void writeForkJoinInvocationInBlock(final AST ast, final TextEditGroup editGroup, final Map<Block, Integer> numTasksPerBlock, Block currBlock, ListRewrite listRewriteForBlock,
			MethodInvocation forkJoinInvocation, boolean isNotNewBlock, Statement firstStatementWithRecursiveCall) {
		if (isNotNewBlock) {
			listRewriteForBlock.insertBefore(ast.newExpressionStatement(forkJoinInvocation), firstStatementWithRecursiveCall, editGroup);
		} else {
			listRewriteForBlock.insertAt(ast.newExpressionStatement(forkJoinInvocation), numTasksPerBlock.get(currBlock).intValue(), editGroup);
		}
	}

	private void addSavedStatementsAfterLastStatement(final TextEditGroup editGroup, ListRewrite listRewriteForBlock, Statement lastStatementWithRecursiveCall, List<ASTNode> statementsToAdd) {
		for (int i= 0; i < statementsToAdd.size(); i++) {
			if (!(lastStatementWithRecursiveCall instanceof ReturnStatement)) {
				listRewriteForBlock.insertAfter(statementsToAdd.get(i), lastStatementWithRecursiveCall, editGroup);
			}
		}
	}


	private void writeTaskDeclStatementsAndAddTaskToForkJoinArguments(final AST ast, final TextEditGroup editGroup, final ASTRewrite scratchRewriter,
			Map<Integer, VariableDeclarationStatement> allTaskDeclStatements, ListRewrite listRewriteForBlock, List<Expression> argumentsForkJoin, boolean isNotNewBlock, Statement currStatement,
			List<Integer> taskList) {
		for (int i= 0; i < taskList.size(); i++) {
			Integer taskNum= taskList.get(i);
			argumentsForkJoin.add(ast.newSimpleName("task" + taskNum)); //$NON-NLS-1$
			replaceWithTaskDeclStatement(allTaskDeclStatements.get(taskNum), currStatement, scratchRewriter, editGroup, listRewriteForBlock, isNotNewBlock, i == taskList.size() - 1);
		}
	}

	private void replaceWithTaskDeclStatement(VariableDeclarationStatement taskDeclStatement, ASTNode node, ASTRewrite scratchRewriter, TextEditGroup editGroup, ListRewrite listRewriteForBlock,
			boolean isNotNewBlock, boolean isLast) {
		if(isNotNewBlock) {
			if(!isLast || node instanceof ReturnStatement) {
				listRewriteForBlock.insertBefore(taskDeclStatement, node, editGroup);
			} else {
				scratchRewriter.replace(node, taskDeclStatement, editGroup);
			}
		} else {
			listRewriteForBlock.insertLast(taskDeclStatement, editGroup);
		}
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
			copyBodyFromMethodDeclarationToComputeMethod(ast, computeMethod, scratchCU, declaringClass);
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	private void copyBodyFromMethodDeclarationToComputeMethod(AST ast, MethodDeclaration computeMethod, CompilationUnit scratchCU, final TypeDeclaration[] declaringClass) {  //TODO throw exception?
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
	}

	private void refactorReturnStatement(final AST ast, final TextEditGroup editGroup, final ASTRewrite scratchRewriter, ListRewrite listRewriteForBlock, Statement lastStatementInBlock, boolean isNotNewBlock, final List<Integer> taskList) {
		ReturnStatement returnStatement= ((ReturnStatement)(ASTNode.copySubtree(ast, lastStatementInBlock)));
		final int[] taskNum= {0};
		returnStatement.accept(new ConvertMethodCallToTask(taskList, ast, taskNum, scratchRewriter, editGroup));
			
		if (isNotNewBlock) {
			scratchRewriter.replace(lastStatementInBlock, returnStatement, editGroup);
		} else {
			listRewriteForBlock.insertLast(returnStatement, editGroup);
		}
	}

	private void refactorStatement(final AST ast, final TextEditGroup editGroup, final ASTRewrite scratchRewriter, ListRewrite listRewriteForBlock, Statement currStatement,
			boolean isNotNewBlock, final List<Integer> taskList) {
		if (currStatement instanceof VariableDeclarationStatement) {	
			VariableDeclarationFragment varFragment= ((VariableDeclarationFragment)(ASTNode.copySubtree(ast, ((VariableDeclarationFragment)(((VariableDeclarationStatement) currStatement).fragments().get(0))))));
			final int[] taskNum= {0};
			varFragment.accept(new ConvertMethodCallToTask(taskList, ast, taskNum, scratchRewriter, editGroup));
				
			addVariableDeclarationStatement(ast, editGroup, listRewriteForBlock, isNotNewBlock, varFragment, scratchRewriter, currStatement);
		} else if (currStatement instanceof ExpressionStatement) {
			Expression expr= ((Expression) (ASTNode.copySubtree(ast, ((ExpressionStatement) currStatement).getExpression())));
			final int[] taskNum= {0};
			expr.accept(new ConvertMethodCallToTask(taskList, ast, taskNum, scratchRewriter, editGroup));
				
			addExpressionStatement(ast, editGroup, listRewriteForBlock, isNotNewBlock, expr, scratchRewriter, currStatement);
		}
	}

	private void addVariableDeclarationStatement(final AST ast, final TextEditGroup editGroup, ListRewrite listRewriteForBlock, boolean isNotNewBlock, VariableDeclarationFragment varFragment,
			ASTRewrite scratchRewriter, Statement currStatement) {
		VariableDeclarationStatement assignToResult= ast.newVariableDeclarationStatement(varFragment);
		if (isNotNewBlock) {
			scratchRewriter.replace(currStatement, assignToResult, editGroup);
		} else {
			listRewriteForBlock.insertLast(assignToResult, editGroup);
		}
	}

	private void addExpressionStatement(final AST ast, final TextEditGroup editGroup, ListRewrite listRewriteForBlock, boolean isNotNewBlock, Expression expr, ASTRewrite scratchRewriter, Statement currStatement) {
		ExpressionStatement assignToResult= ast.newExpressionStatement(expr);
		if (isNotNewBlock) {
			scratchRewriter.replace(currStatement, assignToResult, editGroup);
		} else {
			listRewriteForBlock.insertLast(assignToResult, editGroup);
		}
	}

	private void computeBaseCaseStatements(AST ast, TextEditGroup editGroup, Statement recursionBaseCaseBranch, ASTRewrite scratchRewriter) {
		Block baseCaseBlock;
		boolean isBlock= false;
		if (recursionBaseCaseBranch instanceof Block) {
			baseCaseBlock= (Block) recursionBaseCaseBranch;
			isBlock= true;
		} else {
			baseCaseBlock= ast.newBlock();
		}
		List<Statement> baseCaseStatements= baseCaseBlock.statements();
		if (recursiveMethodReturnsVoid()) {
			ExpressionStatement sequentialMethodInvocation= ast.newExpressionStatement(createSequentialMethodInvocation(ast));
			if (isBlock) { //TODO do i clear the whole block?
				ListRewrite listRewriteForBaseBlock= scratchRewriter.getListRewrite(baseCaseBlock, Block.STATEMENTS_PROPERTY);
				listRewriteForBaseBlock.insertBefore(sequentialMethodInvocation, baseCaseStatements.get(baseCaseStatements.size() - 1), editGroup);
			} else {
				baseCaseStatements.add(sequentialMethodInvocation);
				baseCaseStatements.add(ast.newReturnStatement());
			}
		} else {
			ReturnStatement newReturnResult= ast.newReturnStatement();
			newReturnResult.setExpression(createSequentialMethodInvocation(ast));
			if (isBlock) {
				scratchRewriter.replace(baseCaseStatements.get(baseCaseStatements.size() - 1), newReturnResult, editGroup);
			} else {
				baseCaseStatements.add(newReturnResult);
			}
		}
		if (!isBlock) {
			scratchRewriter.replace(recursionBaseCaseBranch, baseCaseBlock, editGroup);
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
		} while (tempNode != null && !VariableDeclarationStatement.class.isInstance(tempNode) && !ExpressionStatement.class.isInstance(tempNode) && !ReturnStatement.class.isInstance(tempNode));
		if (tempNode != null) {
			if (tempNode instanceof VariableDeclarationStatement) {
				parentOfMethodCall= (VariableDeclarationStatement) tempNode;
			} else if (tempNode instanceof ExpressionStatement) {
				parentOfMethodCall= (ExpressionStatement) tempNode;
			} else {
				parentOfMethodCall= (ReturnStatement) tempNode;
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
			methodArguments+= argument.toString();
			if (iterator.hasNext()) {
				methodArguments+= ", "; //$NON-NLS-1$
			}
		}
		
		String methodArguments2= ConcurrencyRefactorings.ConcurrencyRefactorings_empty_string;
		arguments= methodDeclaration2.parameters();
		for (Iterator<ASTNode> iterator= arguments.iterator(); iterator
				.hasNext();) {
			ASTNode argument= iterator.next();
			methodArguments2+= argument.toString();
			if (iterator.hasNext()) {
				methodArguments2+= ", "; //$NON-NLS-1$
			}
		}
		return methodArguments.equals(methodArguments2);
	}


	private MethodInvocation createSequentialMethodInvocation(AST ast) {
		
		MethodInvocation invokeSequentialMethod= ast.newMethodInvocation();
		invokeSequentialMethod.setName(ast.newSimpleName(fMethod.getElementName() + "_sequential")); //$NON-NLS-1$
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
		computeBodyBlock.accept(new FindBaseCaseVisitor(baseCase, counter, isFirst));
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
		List<ASTNode> recursiveMethodParameters= initializeConstructorMethodSignature(ast, newConstructor);
		
		newConstructor.modifiers().add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
		
		Block newConstructorBlock= ast.newBlock();
		newConstructor.setBody(newConstructorBlock);
		
		createInitializationOfFields(ast, recursiveMethodParameters, newConstructorBlock);	
		
		recursiveActionSubtype.bodyDeclarations().add(newConstructor);
	}

	private void createInitializationOfFields(AST ast, List<ASTNode> recursiveMethodParameters, Block newConstructorBlock) {
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
	}

	private List<ASTNode> initializeConstructorMethodSignature(AST ast, MethodDeclaration newConstructor) {
		newConstructor.setConstructor(true);
		newConstructor.setName(ast.newSimpleName(nameForFJTaskSubtype));
		List<ASTNode> constructorParameters= newConstructor.parameters();
		List<ASTNode> recursiveMethodParameters= fMethodDeclaration.parameters();
		for (Object par : recursiveMethodParameters) {
			SingleVariableDeclaration parameter= (SingleVariableDeclaration) par;
			constructorParameters.add(ASTNode.copySubtree(ast, parameter));
		}
		return recursiveMethodParameters;
	}

	private void createFields(TypeDeclaration recursiveActionSubtype, AST ast) {
		
		List<SingleVariableDeclaration> recursiveMethodParameters= fMethodDeclaration.parameters();
		for (SingleVariableDeclaration parameter : recursiveMethodParameters) {
			createNewFieldDeclaration(recursiveActionSubtype, ast, parameter);
		}
	}

	private void createNewFieldDeclaration(TypeDeclaration recursiveActionSubtype, AST ast, SingleVariableDeclaration parameter) {
		VariableDeclarationFragment newDeclarationFragment= ast.newVariableDeclarationFragment();
		newDeclarationFragment.setName(ast.newSimpleName(parameter.getName().getIdentifier()));
		
		FieldDeclaration newFieldDeclaration= ast.newFieldDeclaration(newDeclarationFragment);
		newFieldDeclaration.setType((Type) ASTNode.copySubtree(ast, parameter.getType()));
		List<Modifier> modifiers= newFieldDeclaration.modifiers();
		modifiers.add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
		
		recursiveActionSubtype.bodyDeclarations().add(newFieldDeclaration);
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
		
		ASTParser parser= ASTParser.newParser(AST.JLS4);
		setPropertiesForParser(parser);
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

	private void setPropertiesForParser(ASTParser parser) {
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(false);
		parser.setBindingsRecovery(false);
		parser.setSource(fMethod.getCompilationUnit());
		parser.setCompilerOptions(getCompilerOptions(fMethod.getCompilationUnit()));
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
			SingleVariableDeclaration parameter= params.get(0);
			Type type= parameter.getType();
			if (type.isPrimitiveType()) {
				sugSeqThreshold= parameter.getName().getIdentifier() + " < 1000"; //$NON-NLS-1$
			} else if (type.isArrayType()) {
				sugSeqThreshold= parameter.getName().getIdentifier() + ".length < 1000"; //$NON-NLS-1$
			} else if (type.isParameterizedType()) {
				sugSeqThreshold= parameter.getName().getIdentifier() + ".size() < 1000"; //$NON-NLS-1$
			} else if (type.isSimpleType()) {
				sugSeqThreshold= parameter.getName().getIdentifier() + ".length() < 1000"; //$NON-NLS-1$
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
		nameAndSignature= addParametersToSignature(nameAndSignature, recursiveMethodParameters);
		nameAndSignature+= ")"; //$NON-NLS-1$
		return nameAndSignature;
	}

	private String addParametersToSignature(String nameAndSignature, List<SingleVariableDeclaration> recursiveMethodParameters) {
		for (Iterator<SingleVariableDeclaration> iterator= recursiveMethodParameters.iterator(); iterator
				.hasNext();) {
			SingleVariableDeclaration parameter= iterator.next();
			nameAndSignature+= parameter.getType() + " " + parameter.getName().getIdentifier(); //$NON-NLS-1$
			if (iterator.hasNext()) {
				nameAndSignature+= ", "; //$NON-NLS-1$
			}
		}
		return nameAndSignature;
	}	
	
	private <T> List<T> castList(Class<? extends T> toCastTo, List<?> c) {
		
		if (c == null || c.size() == 0) {
			return (List<T>) c;
		} else {
			List<T> tempList= new ArrayList<T>(c.size());
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
	
	private boolean isMethodDeclarationEqualTo(MethodInvocation methodCall) {
		boolean namesMatch= methodCall.getName().getFullyQualifiedName().equals(fMethodDeclaration.getName().getFullyQualifiedName());
		List<Expression> methodArgs= methodCall.arguments();
		List<SingleVariableDeclaration> declArgs= fMethodDeclaration.parameters();
		boolean paramSizesMatch= methodArgs.size() == declArgs.size();
		return namesMatch && paramSizesMatch;  //TODO Add more logic to better check - make so overloaded methods don't get chosen
	}
	
	private final class FindBaseCaseVisitor extends ASTVisitor {
		private final Statement[] fBaseCase;
		private final int[] fCounter;
		private final boolean[] fIsFirst;

		private FindBaseCaseVisitor(Statement[] baseCase, int[] counter, boolean[] isFirst) {
			fBaseCase= baseCase;
			fCounter= counter;
			fIsFirst= isFirst;
		}

		@Override
		public boolean visit(IfStatement ifStatement) {
			Statement thenStatement= ifStatement.getThenStatement();
			if (statementIsBaseCase(thenStatement)) {
				if (fCounter[0] == 0) {
					fBaseCase[0]= thenStatement;
					fCounter[0]= 1;
				}
			} else {
				if (fIsFirst[0] && fCounter[0] != 1) {
					fIsFirst[0]= false;
					fCounter[0]= 2;
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
	}

	private final class RecursiveToSequentialMethodCallVisitor extends ASTVisitor {
		private final AST fAst;

		private RecursiveToSequentialMethodCallVisitor(AST ast) {
			fAst= ast;
		}

		@Override
		public boolean visit(MethodInvocation methodCall){
			if (isMethodDeclarationEqualTo(methodCall)) {
				methodCall.setName(fAst.newSimpleName(methodCall.getName().getIdentifier() + "_sequential")); //$NON-NLS-1$
				return false;
			}
			return true;
		}
	}

	private final class ConvertMethodCallToTask extends ASTVisitor {
		private final List<Integer> fTaskList;
		private final AST fAst;
		private final int[] fTaskNum;
		private final ASTRewrite fScratchRewriter;
		private final TextEditGroup fEditGroup;

		private ConvertMethodCallToTask(List<Integer> taskList, AST ast, int[] taskNum, ASTRewrite scratchRewriter, TextEditGroup editGroup) {
			fTaskList= taskList;
			fAst= ast;
			fTaskNum= taskNum;
			fScratchRewriter= scratchRewriter;
			fEditGroup= editGroup;
		}

		@Override
		public boolean visit(MethodInvocation methodCall) {
		if	(isMethodDeclarationEqualTo(methodCall)) {
			MethodInvocation replacement= fAst.newMethodInvocation();
			replacement.setExpression(fAst.newSimpleName("task" + fTaskList.get(fTaskNum[0]))); //$NON-NLS-1$
			replacement.setName(fAst.newSimpleName("getRawResult"));  //$NON-NLS-1$
			fScratchRewriter.replace(methodCall, replacement, fEditGroup);
			fTaskNum[0]++;
		}
		if (fTaskNum[0] == fTaskList.size()) {
			return false;
		}
		return true;
		}
	}

	private final class MethodVisitor extends ASTVisitor {
		private final Map<Integer, VariableDeclarationStatement> fAllTaskDeclStatements;
		private final Map<Statement, List<Integer> > fStatementsToTasks;
		private final Map<ASTNode, Block> fLocationOfNewBlocks;
		private final ASTRewrite fScratchRewriter;
		private final Map<Block, Integer> fNumTasksPerBlock;
		private final int[] fTaskNumber;
		private final Map<Block, Statement> fBlockWithoutBraces;
		private final Map<Block, List<Statement>> fAllStatementsWithRecursiveMethodInvocation;
		private final List<Block> fAllTheBlocks;
		private final boolean[] fSwitchStatementsFound;
		private final AST fAst;

		private MethodVisitor(Map<Integer, VariableDeclarationStatement> allTaskDeclStatements, Map<Statement, List<Integer>> statementsToTasks, ASTRewrite scratchRewriter,
				Map<Block, Integer> numTasksPerBlock, Map<Block, Statement> blockWithoutBraces, Map<Block, List<Statement>> allStatementsWithRecursiveMethodInvocation, List<Block> allTheBlocks,
				boolean[] switchStatementsFound, AST ast) {
			fAllTaskDeclStatements= allTaskDeclStatements;
			fStatementsToTasks= statementsToTasks;
			fScratchRewriter= scratchRewriter;
			fNumTasksPerBlock= numTasksPerBlock;
			fBlockWithoutBraces= blockWithoutBraces;
			fAllStatementsWithRecursiveMethodInvocation= allStatementsWithRecursiveMethodInvocation;
			fAllTheBlocks= allTheBlocks;
			fSwitchStatementsFound= switchStatementsFound;
			fAst= ast;
			fLocationOfNewBlocks= new HashMap<ASTNode, Block>();
			fTaskNumber= new int[] {0};
		}

		@Override
		public boolean visit(MethodInvocation methodCall) {
			
			if (isRecursiveMethod(methodCall)) {
				VariableDeclarationStatement taskDeclStatement= createTaskDeclaration(methodCall);
				
				Block myBlock= null;
				Statement parentOfMethodCall= findParentStatement(methodCall);
				
				if (parentOfMethodCall == null) {
					return false;
				} else if (SwitchStatement.class.isInstance(parentOfMethodCall.getParent())) {
					fSwitchStatementsFound[0]= true;
					return false;
				} else if (!recursiveMethodReturnsVoid()) {
					if (parentOfMethodCall instanceof VariableDeclarationStatement) {
						ASTNode tempNode= parentOfMethodCall.getParent();
						if (tempNode instanceof IfStatement) {
							myBlock= createBlockAtIfStatement(myBlock, parentOfMethodCall, tempNode);
							if (myBlock == null) {
								return false;
							}
						} else if (tempNode instanceof ForStatement) {
							myBlock= createBlockAtForStatement(myBlock, tempNode);
							if (myBlock == null) {
								return false;
							}
						}
					} else if (parentOfMethodCall instanceof ExpressionStatement) {
						ExpressionStatement exprStatement= (ExpressionStatement) parentOfMethodCall;
						Expression expressionContainer= exprStatement.getExpression();
						if (expressionContainer instanceof Assignment) {
							ASTNode tempNode= parentOfMethodCall.getParent();
							if (tempNode instanceof IfStatement) {
								myBlock= createBlockAtIfStatement(myBlock, parentOfMethodCall, tempNode);
								if (myBlock == null) {
									return false;
								}
							} else if (tempNode instanceof ForStatement) {
								myBlock= createBlockAtForStatement(myBlock, tempNode);
								if (myBlock == null) {
									return false;
								}
							}
						} else {
							System.err.println(ConcurrencyRefactorings.ConvertToFJTaskRefactoring_scenario_error + parentOfMethodCall.toString() );
							return false;
						}
					} else if (parentOfMethodCall instanceof ReturnStatement) {
						ASTNode tempNode= parentOfMethodCall.getParent();
						if (tempNode instanceof IfStatement) {
							myBlock= createBlockAtIfStatement(myBlock, parentOfMethodCall, tempNode);
							if (myBlock == null) {
								return false;
							}
						} else if (tempNode instanceof ForStatement) {
							myBlock= createBlockAtForStatement(myBlock, tempNode);
							if (myBlock == null) {
								return false;
							}
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
						fSwitchStatementsFound[0]= true;
						return false;
					} else {
						myBlock= (Block) tempNode;
					}
				}
				populateAllMaps(myBlock, parentOfMethodCall, taskDeclStatement);
			}
			return true;
		}

		private boolean isRecursiveMethod(MethodInvocation methodCall) {
			IMethodBinding bindingForMethodCall= methodCall.resolveMethodBinding();
			IMethodBinding bindingForMethodDeclaration= fMethodDeclaration.resolveBinding();
			boolean isRecursiveMethod= bindingForMethodCall.isEqualTo(bindingForMethodDeclaration);
			return isRecursiveMethod;
		}

		private void populateAllMaps(Block myBlock, Statement parentOfMethodCall, VariableDeclarationStatement taskDeclStatement) {
			Integer taskNum= new Integer(fTaskNumber[0]);
			fAllTaskDeclStatements.put(taskNum, taskDeclStatement);
			populateStatementsToTasks(parentOfMethodCall, taskNum);
			populateNumTasksPerBlock(myBlock);
			populateAllStatementsWithRecursiveMethodInvocation(myBlock, parentOfMethodCall);
			if (!fAllTheBlocks.contains(myBlock)) {
				fAllTheBlocks.add(myBlock);
			}
		}

		private void populateAllStatementsWithRecursiveMethodInvocation(Block myBlock, Statement parentOfMethodCall) {
			if (fAllStatementsWithRecursiveMethodInvocation.containsKey(myBlock)) {
				List<Statement> recursiveList= fAllStatementsWithRecursiveMethodInvocation.get(myBlock);
				if (!recursiveList.contains(parentOfMethodCall)) {
					recursiveList.add(parentOfMethodCall);
				}
			} else {
				List<Statement> recursiveList= new ArrayList<Statement>();
				recursiveList.add(parentOfMethodCall);
				fAllStatementsWithRecursiveMethodInvocation.put(myBlock, recursiveList);
			}
		}

		private void populateNumTasksPerBlock(Block myBlock) {
			if (fNumTasksPerBlock.containsKey(myBlock)) {
				Integer newValue= new Integer(fNumTasksPerBlock.get(myBlock).intValue() + 1);
				fNumTasksPerBlock.put(myBlock, newValue);
			} else {
				fNumTasksPerBlock.put(myBlock, new Integer(1));
			}
		}

		private void populateStatementsToTasks(Statement parentOfMethodCall, Integer taskNum) {
			if (fStatementsToTasks.containsKey(parentOfMethodCall)) {
				List<Integer> taskList= fStatementsToTasks.get(parentOfMethodCall);
				taskList.add(taskNum);
			} else {
				List<Integer> taskList= new ArrayList<Integer>();
				taskList.add(taskNum);
				fStatementsToTasks.put(parentOfMethodCall, taskList);
			}
		}

		private Block createBlockAtIfStatement(Block myBlock, Statement parentOfMethodCall, ASTNode tempNode) {
			IfStatement ifStatement= (IfStatement) tempNode;
			Statement elseStatement= ifStatement.getElseStatement();
			if (elseStatement != null && ifStatement.getThenStatement() != null && !ifStatement.getThenStatement().equals(parentOfMethodCall)) {
				myBlock= getNewBlock(elseStatement);									
			} else {
				Statement thenStatement= ifStatement.getThenStatement();
				if (thenStatement != null && thenStatement.equals(parentOfMethodCall)) {
					myBlock= getNewBlock(thenStatement);
				} else {
					return null;
				}
			}
			return myBlock;
		}
		
		private Block createBlockAtForStatement(Block myBlock, ASTNode tempNode) {
			ForStatement forStatement= (ForStatement) tempNode;
			Statement bodyStatement= forStatement.getBody();
			if (bodyStatement != null) {
				myBlock= getNewBlock(bodyStatement);									
			} else {
				return null;
			}
			return myBlock;
		}

		private Block getNewBlock(Statement targetStatement) {
			Block myBlock;
			if (fLocationOfNewBlocks.containsKey(targetStatement)) {
				myBlock= fLocationOfNewBlocks.get(targetStatement);
			} else {
				myBlock= fAst.newBlock();
				fLocationOfNewBlocks.put(targetStatement, myBlock);
				fBlockWithoutBraces.put(myBlock, targetStatement);
			}
			return myBlock;
		}

		private VariableDeclarationStatement createTaskDeclaration(MethodInvocation methodCall) {
			String codeForTaskDecl= nameForFJTaskSubtype + " task" + ++fTaskNumber[0] +  //$NON-NLS-1$
			" = new " + nameForFJTaskSubtype + "("; //$NON-NLS-1$ //$NON-NLS-2$
			String methodArguments= ConcurrencyRefactorings.ConcurrencyRefactorings_empty_string;
			List<Expression> arguments= methodCall.arguments();
			for (Iterator<Expression> iterator= arguments.iterator(); iterator
					.hasNext();) {
				ASTNode argument= iterator.next();
				methodArguments+= argument.toString();
				if (iterator.hasNext()) {
					methodArguments+= ", "; //$NON-NLS-1$
				}
			}
			codeForTaskDecl+= methodArguments + ");"; //$NON-NLS-1$
			VariableDeclarationStatement taskDeclStatement= (VariableDeclarationStatement) fScratchRewriter.createStringPlaceholder(codeForTaskDecl , ASTNode.VARIABLE_DECLARATION_STATEMENT);
			return taskDeclStatement;
		}
	}
}