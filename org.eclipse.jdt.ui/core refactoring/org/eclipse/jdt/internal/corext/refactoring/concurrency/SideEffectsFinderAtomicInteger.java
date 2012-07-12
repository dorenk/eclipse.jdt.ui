package org.eclipse.jdt.internal.corext.refactoring.concurrency;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperFieldAccess;

public class SideEffectsFinderAtomicInteger extends ASTVisitor {

	private final IVariableBinding notIncludingField;
	private boolean hasSideEffects;
	
	public SideEffectsFinderAtomicInteger(
			IVariableBinding notIncludingField) {
		this.notIncludingField= notIncludingField;
	}

	@Override
	public boolean visit(Assignment assignment){

		Expression rightHandSide= assignment.getRightHandSide();
		rightHandSide.accept(this);
		return true;
	}
	
	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (considerBinding(resolveBinding(methodInvocation.getExpression()))) {
			List<?> arguments= methodInvocation.arguments();
			for (Iterator<?> iterator= arguments.iterator(); iterator.hasNext();) {
				Expression expression= (Expression) iterator.next();
				if (!(expression instanceof NumberLiteral) && !(expression instanceof SimpleName)) {
					hasSideEffects= true;
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean visit(PostfixExpression postfixExpression) {
		if (!considerBinding(resolveBinding(postfixExpression.getOperand()))) {
			hasSideEffects= true;
		}
		return true;
	}
	
	@Override
	public boolean visit(PrefixExpression prefixExpression) {
		if (!considerBinding(resolveBinding(prefixExpression.getOperand()))) {
			hasSideEffects= true;
		}
		return true;
	}

	@Override
	public boolean visit(InfixExpression infixExpression) {

		Expression leftOperand= infixExpression.getLeftOperand();
		Expression rightOperand= infixExpression.getRightOperand();
		
		if (infixExpression.hasExtendedOperands()) {
			hasSideEffects= true;
		}
		if (!considerBinding(resolveBinding(leftOperand)) && !considerBinding(resolveBinding(rightOperand))) {
			hasSideEffects= true;
		}
		if (leftOperand instanceof MethodInvocation || rightOperand instanceof MethodInvocation) {
			hasSideEffects= true;
		}
		return true;
		
		
	}

	@Override
	public boolean visit(ParenthesizedExpression parenthesizedExpression) {
		Expression expression= parenthesizedExpression.getExpression();
		expression.accept(this);
		return true;
	}

	private IBinding resolveBinding(Expression expression) {

		if (expression instanceof SimpleName) {
			return ((SimpleName) expression).resolveBinding();
		} else if (expression instanceof QualifiedName) {
			return ((QualifiedName) expression).resolveBinding();
		} else if (expression instanceof FieldAccess) {
			return ((FieldAccess) expression).getName().resolveBinding();
		} else if (expression instanceof SuperFieldAccess) {
			return ((SuperFieldAccess) expression).getName().resolveBinding();
		}
		return null;
	}

	private boolean considerBinding(IBinding binding) {

		if (!(binding instanceof IVariableBinding)) {
			return false;
		}
		return notIncludingField.isEqualTo(((IVariableBinding) binding).getVariableDeclaration());
	}
	
	public boolean hasSideEffects(Statement statement) {
		hasSideEffects= false;
		statement.accept(this);
		return hasSideEffects;
	}
}
