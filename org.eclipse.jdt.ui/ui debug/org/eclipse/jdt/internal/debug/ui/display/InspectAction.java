/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.debug.ui.display;

import java.util.ResourceBundle;import org.eclipse.debug.core.model.IValue;import org.eclipse.debug.ui.DebugUITools;import org.eclipse.swt.widgets.Display;import org.eclipse.ui.IWorkbenchPart;import org.eclipse.jdt.debug.core.IJavaEvaluationResult;


/**
 * Places the result of an evaluation in the debug inspector
 */
public class InspectAction extends EvaluateAction {

	public InspectAction(ResourceBundle bundle, String prefix, IWorkbenchPart workbenchPart) {
		super(bundle, prefix, workbenchPart);
	}
	
	public void evaluationComplete(final IJavaEvaluationResult res) {
		final IValue value= res.getValue();
		if (value != null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					DebugUITools.inspect(res.getSnippet().trim(), value);
				}
			});
		}
	}
}
