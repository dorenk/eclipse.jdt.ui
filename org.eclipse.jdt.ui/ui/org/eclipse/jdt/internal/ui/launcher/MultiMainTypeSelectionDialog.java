/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.ui.launcher;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.util.Assert;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;

import org.eclipse.jdt.ui.JavaElementLabelProvider;

import org.eclipse.jdt.internal.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.internal.ui.util.MainMethodSearchEngine;

/**
 * A dialog to select a type from a list of types. The dialog allows
 * multiple selections.
 */
public class MultiMainTypeSelectionDialog extends ElementListSelectionDialog {

	private IRunnableContext fRunnableContext;
	private IJavaSearchScope fScope;
	private int fStyle;
		
	/**
	 * Constructor.
	 */
	public MultiMainTypeSelectionDialog(Shell shell, IRunnableContext context,
		IJavaSearchScope scope, int style)
	{
		super(shell, new JavaElementLabelProvider(
			JavaElementLabelProvider.SHOW_CONTAINER | 
			JavaElementLabelProvider.SHOW_POSTIFIX_QUALIFICATION |
			JavaElementLabelProvider.SHOW_CONTAINER_QUALIFICATION));

		setMultipleSelection(true);

		Assert.isNotNull(context);
		Assert.isNotNull(scope);
		
		fRunnableContext= context;
		fScope= scope;
		fStyle= style;
	}

	/**
	 *
	 */
	public int open() {
		MainMethodSearchEngine engine= new MainMethodSearchEngine();
		IType[] types;
		try {
			types= engine.searchMainMethods(fRunnableContext, fScope, fStyle);
		} catch (InterruptedException e) {
			return CANCEL;
		} catch (InvocationTargetException e) {
			//XX: to do
			ExceptionHandler.handle(e, "Error", e.getMessage());
			return CANCEL;
		}
		
		setElements(types);
		setFilter("A"); //$NON-NLS-1$
		return super.open();
	}
	
}