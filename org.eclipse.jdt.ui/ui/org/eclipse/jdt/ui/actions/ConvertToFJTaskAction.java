package org.eclipse.jdt.ui.actions;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.jface.text.ITextSelection;

import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.corext.refactoring.RefactoringAvailabilityTester;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringExecutionStarter;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaTextSelection;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;

/**
 * @since 3.9
 */
public class ConvertToFJTaskAction extends SelectionDispatchAction {

	private JavaEditor fEditor;

	/**
	 * Note: This constructor is for internal use only. Clients should not call this constructor.
	 * @param editor the java editor
	 *
	 * @noreference This constructor is not intended to be referenced by clients.
	 */
	public ConvertToFJTaskAction(JavaEditor editor) {
		this(editor.getEditorSite());
		fEditor= editor;
		setEnabled(SelectionConverter.canOperateOn(fEditor));
	}

	/**
	 * Creates a new <code>ConvertToFJTaskAction</code>. The action requires
	 * that the selection provided by the site's selection provider is of type
	 * <code>org.eclipse.jface.viewers.IStructuredSelection</code>.
	 *
	 * @param site the site providing context information for this action
	 */
	public ConvertToFJTaskAction(IWorkbenchSite site) {
		super(site);
		setText("Convert recursion to ForkJoinTask"); //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.FORK_JOIN_TASK_ACTION);
	}
	
	/* (non-Javadoc)
	 * Method declared on SelectionDispatchAction.
	 */
	@Override
	public void selectionChanged(ITextSelection selection) {
		setEnabled(true);
	}
	
	/**
	 * Note: This method is for internal use only. Clients should not call this method.
	 * 
	 * @param selection the Java text selection
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	public void selectionChanged(JavaTextSelection selection) {
		try {
			setEnabled(RefactoringAvailabilityTester.isConvertToFJTaskAvailable(selection));
		} catch (JavaModelException e) {
			// http://bugs.eclipse.org/bugs/show_bug.cgi?id=19253
			if (JavaModelUtil.isExceptionToBeLogged(e))
				JavaPlugin.log(e);
			setEnabled(false);//no UI
		}
	}
	
	@Override
	public void selectionChanged(IStructuredSelection selection) {
		try {
			setEnabled(RefactoringAvailabilityTester.isConvertToFJTaskAvailable(selection));
		} catch (JavaModelException e) {
			if (JavaModelUtil.isExceptionToBeLogged(e))
				JavaPlugin.log(e);
			setEnabled(false);
		}
	}
	
	/*
	 * @see SelectionDispatchAction#run(IStructuredSelection)
	 */
	@Override
	public void run(IStructuredSelection selection) {
		try {
			// we have to call this here - no selection changed event is sent after a refactoring but it may still invalidate enablement
			if (RefactoringAvailabilityTester.isConvertToFJTaskAvailable(selection)) {
				IMethod method= getSingleSelectedMethod(selection);
				if (! ActionUtil.isEditable(getShell(), method))
					return;
				RefactoringExecutionStarter.startForkJoinTaskRefactoring(method, getShell());
			}
		} catch (JavaModelException e) {
			ExceptionHandler.handle(e, RefactoringMessages.OpenRefactoringWizardAction_refactoring, RefactoringMessages.OpenRefactoringWizardAction_exception);
		}
	}

    /*
     * @see SelectionDispatchAction#run(ITextSelection)
     */
	@Override
	public void run(ITextSelection selection) {
		try {
			if (! ActionUtil.isEditable(fEditor))
				return;
			IMethod method= getSingleSelectedMethod(selection);
			if (RefactoringAvailabilityTester.isConvertToFJTaskAvailable(method)){
				RefactoringExecutionStarter.startForkJoinTaskRefactoring(method, getShell());
			} else {
				MessageDialog.openInformation(getShell(), RefactoringMessages.OpenRefactoringWizardAction_unavailable, RefactoringMessages.ModifyParametersAction_unavailable);
			}
		} catch (JavaModelException e) {
			ExceptionHandler.handle(e, RefactoringMessages.OpenRefactoringWizardAction_refactoring, RefactoringMessages.OpenRefactoringWizardAction_exception);
		}
	}

	private static IMethod getSingleSelectedMethod(IStructuredSelection selection){
		if (selection.isEmpty() || selection.size() != 1)
			return null;
		if (selection.getFirstElement() instanceof IMethod)
			return (IMethod)selection.getFirstElement();
		return null;
	}

	private IMethod getSingleSelectedMethod(ITextSelection selection) throws JavaModelException{
		//- when caret/selection on method name (call or declaration) -> that method
		//- otherwise: caret position's enclosing method declaration
		//  - when caret inside argument list of method declaration -> enclosing method declaration
		//  - when caret inside argument list of method call -> enclosing method declaration (and NOT method call)
		IJavaElement[] elements= SelectionConverter.codeResolve(fEditor);
		if (elements.length > 1)
			return null;
		if (elements.length == 1 && elements[0] instanceof IMethod)
			return (IMethod)elements[0];
		IJavaElement elementAt= SelectionConverter.getInputAsCompilationUnit(fEditor).getElementAt(selection.getOffset());
		if (elementAt instanceof IMethod)
			return (IMethod)elementAt;
		return null;
	}
}

