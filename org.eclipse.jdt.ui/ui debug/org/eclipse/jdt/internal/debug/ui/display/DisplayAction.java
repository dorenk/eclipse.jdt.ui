/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.ui.display;

import java.util.ResourceBundle;import org.eclipse.debug.core.DebugException;import org.eclipse.swt.widgets.Display;import org.eclipse.jface.text.BadLocationException;import org.eclipse.jface.text.IDocument;import org.eclipse.ui.IWorkbenchPart;import org.eclipse.jdt.debug.core.IJavaEvaluationResult;import org.eclipse.jdt.debug.core.IJavaValue;



/**
 * Displays the result of an evaluation in the display view
 */
public class DisplayAction extends EvaluateAction {
	
	public DisplayAction(ResourceBundle bundle, String prefix, IWorkbenchPart workbenchPart) {
		super(bundle, prefix, workbenchPart);
	}
	
	public void evaluationComplete(final IJavaEvaluationResult res) {
		final IJavaValue value= res.getValue();
		if (value != null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					insertResult(value);
				}
			});
		}
	}
	
	protected void insertResult(IJavaValue result) {
		
		StringBuffer resultString= new StringBuffer();
		try {
			String sig= result.getSignature();
			if ("V".equals(sig)) {
				resultString.append(' ');
				resultString.append(getErrorResourceString("noreturn"));
			} else {
				if (sig != null) {
					resultString.append(" (");
					resultString.append(result.getReferenceTypeName());
					resultString.append(") ");
				} else {
					resultString.append(' ');
				}  
				resultString.append(result.evaluateToString());
			}
		} catch(DebugException x) {
			reportError(x.getStatus());
		}
		
		IDataDisplay dataDisplay= getDataDisplay(fWorkbenchPart);
		if (dataDisplay != null)
			dataDisplay.display(fExpression, resultString.toString());
	}
	
	protected IDataDisplay getDataDisplay(IWorkbenchPart workbenchPart) {
		
		Object value= workbenchPart.getAdapter(IDataDisplay.class);
		if (value instanceof IDataDisplay)
			return (IDataDisplay) value;
		
		return null;
	}
}
