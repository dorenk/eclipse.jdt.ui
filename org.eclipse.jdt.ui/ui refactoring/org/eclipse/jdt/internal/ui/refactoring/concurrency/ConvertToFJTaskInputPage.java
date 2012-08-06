package org.eclipse.jdt.internal.ui.refactoring.concurrency;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;

import org.eclipse.jdt.internal.corext.refactoring.concurrency.ConvertToFJTaskRefactoring;

public class ConvertToFJTaskInputPage extends UserInputWizardPage {

	private final class InputListener implements ModifyListener {
		public void modifyText(ModifyEvent event) {
			handleInputChanged();
		}
	}
	
	private final class MyMouseListener implements MouseListener {
		boolean used= false;

		public void mouseDoubleClick(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void mouseDown(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void mouseUp(MouseEvent e) {
			// TODO Auto-generated method stub
			if (!used) {
				handleSequentialInputChanged();
				used = true;
			}
		}
		
	}
	
	private final class MySelectionListener implements SelectionListener {

		public void widgetSelected(SelectionEvent e) {
			handleButtonPressed();
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	Text fRecursiveMethod;
	Text sequentialThreshold;
	Text FJTaskClassName;
	MessageBox helpDialog;

	//Combo fTypeCombo;

	public ConvertToFJTaskInputPage(String name) {
		super(name);
	}

	public void createControl(Composite parent) {
		Composite result= new Composite(parent, SWT.NONE);

		setControl(result);

		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		result.setLayout(layout);

		Label label= new Label(result, SWT.NONE);
		label.setText("&Recursive method:"); //$NON-NLS-1$

		fRecursiveMethod= createNameField(result);
		fRecursiveMethod.setEditable(false);

		Label label2= new Label(result, SWT.NONE);
		label2.setText("&ForkJoinTask class name:"); //$NON-NLS-1$

		FJTaskClassName= createNameField(result);

		Label label3= new Label(result, SWT.NONE);
		label3.setText("&Sequential threshold:"); //$NON-NLS-1$

		sequentialThreshold= createNameField(result);
		
		final ConvertToFJTaskRefactoring refactoring= getConvertToFJTaskRefactoring();
		
		fRecursiveMethod.setText(refactoring.getMethodNameAndSignature());
		FJTaskClassName.setText(refactoring.suggestTaskName());
		sequentialThreshold.setMessage(refactoring.suggestSequentialThreshold());

		InputListener inputListener = new InputListener();
		FJTaskClassName.addModifyListener(inputListener);
		sequentialThreshold.addModifyListener(inputListener);
		
		MyMouseListener mouseListener= new MyMouseListener();
		sequentialThreshold.addMouseListener(mouseListener);
		
		Button helpButton= new Button(result, SWT.PUSH);
		helpButton.setText("?"); //$NON-NLS-1$
		
		MySelectionListener selectionListener= new MySelectionListener();
		helpButton.addSelectionListener(selectionListener);
		
		helpDialog= new MessageBox(getShell());
		helpDialog.setMessage("The sequential threshold is what is used to determine when the algorithm should switch to the sequential version."); //$NON-NLS-1$
		helpDialog.setText("Sequential Threshold Help"); //$NON-NLS-1$
		
		handleInputChanged();
	}

	private Text createNameField(Composite result) {
		Text field= new Text(result, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return field;
	}

	private ConvertToFJTaskRefactoring getConvertToFJTaskRefactoring() {
		return (ConvertToFJTaskRefactoring) getRefactoring();
	}

	void handleInputChanged() {
		RefactoringStatus status= new RefactoringStatus();
		ConvertToFJTaskRefactoring refactoring= getConvertToFJTaskRefactoring();
		status.merge(refactoring.setNameForFJTaskSubtype(FJTaskClassName.getText()));
		status.merge(refactoring.setSequentialThreshold(sequentialThreshold.getText()));
		
		setPageComplete(!status.hasError());
		int severity= status.getSeverity();
		String message= status.getMessageMatchingSeverity(severity);
		if (severity >= RefactoringStatus.INFO) {
			setMessage(message, severity);
		} else {
			setMessage("", NONE); //$NON-NLS-1$
		}
	}
	
	void handleSequentialInputChanged() {
		sequentialThreshold.setText(sequentialThreshold.getMessage());
//		handleInputChanged();
	}
	
	void handleButtonPressed() {
		helpDialog.open();
	}
}
