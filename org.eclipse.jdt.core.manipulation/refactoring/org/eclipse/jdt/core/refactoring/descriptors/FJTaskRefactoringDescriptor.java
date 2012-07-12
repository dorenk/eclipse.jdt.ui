package org.eclipse.jdt.core.refactoring.descriptors;

import java.util.Map;

import org.eclipse.jdt.core.refactoring.IJavaRefactorings;

/**
 * @since 1.6
 */
public class FJTaskRefactoringDescriptor extends JavaRefactoringDescriptor {

	public FJTaskRefactoringDescriptor(String project,
			String description, String comment, Map arguments, int flags) {
		super(IJavaRefactorings.FORK_JOIN_TASK, project, description, comment, arguments, flags);
		// TODO Auto-generated constructor stub
	}

	public FJTaskRefactoringDescriptor() {
		super(IJavaRefactorings.FORK_JOIN_TASK);
		// TODO Auto-generated constructor stub
	}
}
