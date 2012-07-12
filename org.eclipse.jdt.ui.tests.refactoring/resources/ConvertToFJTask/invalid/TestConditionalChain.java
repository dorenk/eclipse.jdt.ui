package invalid;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

public class TestConditionalChain {
	public static IBinding resolveBinding(Expression expression){
		if (expression instanceof Name)
			return ((Name)expression).resolveBinding();
		if (expression instanceof ParenthesizedExpression)
			return resolveBinding(((ParenthesizedExpression)expression).getExpression());
		else if (expression instanceof Assignment)
			return resolveBinding(((Assignment)expression).getLeftHandSide());//TODO ???
		else if (expression instanceof MethodInvocation)
			return ((MethodInvocation)expression).resolveMethodBinding();
		else if (expression instanceof SuperMethodInvocation)
			return ((SuperMethodInvocation)expression).resolveMethodBinding();
		else if (expression instanceof FieldAccess)
			return ((FieldAccess)expression).resolveFieldBinding();
		else if (expression instanceof SuperFieldAccess)
			return ((SuperFieldAccess)expression).resolveFieldBinding();
		else if (expression instanceof ConditionalExpression)
			return resolveBinding(((ConditionalExpression)expression).getThenExpression());
		return null;
	}
}