/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *   Jesper Kamstrup Linnet (eclipse@kamstrup-linnet.dk) - initial API and implementation 
 *          (report 36180: Callers/Callees view)
 ******************************************************************************/
package org.eclipse.jdt.internal.ui.callhierarchy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.help.WorkbenchHelp;

import org.eclipse.jdt.core.IJavaElement;

import org.eclipse.jdt.ui.actions.SelectionDispatchAction;

import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.IJavaStatusConstants;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;

/**
 * This action opens a call hierarchy on the selected method.
 * <p>
 * The action is applicable to selections containing elements of type
 * <code>IMethod</code>.
 */
public class OpenCallHierarchyAction extends SelectionDispatchAction {
    
    private JavaEditor fEditor;
    
    /**
     * Creates a new <code>OpenCallHierarchyAction</code>. The action requires
     * that the selection provided by the site's selection provider is of type <code>
     * org.eclipse.jface.viewers.IStructuredSelection</code>.
     * 
     * @param site the site providing context information for this action
     */
    public OpenCallHierarchyAction(IWorkbenchSite site) {
        super(site);
        setText(CallHierarchyMessages.getString("OpenCallHierarchyAction.label")); //$NON-NLS-1$
        setToolTipText(CallHierarchyMessages.getString("OpenCallHierarchyAction.tooltip")); //$NON-NLS-1$
        setDescription(CallHierarchyMessages.getString("OpenCallHierarchyAction.description")); //$NON-NLS-1$
        WorkbenchHelp.setHelp(this, IJavaHelpContextIds.CALL_HIERARCHY_OPEN_ACTION);
    }
    
    /**
     * Note: This constructor is for internal use only. Clients should not call this constructor.
     */
    public OpenCallHierarchyAction(JavaEditor editor) {
        this(editor.getEditorSite());
        fEditor= editor;
        setEnabled(SelectionConverter.canOperateOn(fEditor));
    }
    
    /* (non-Javadoc)
     * Method declared on SelectionDispatchAction.
     */
    protected void selectionChanged(ITextSelection selection) {
    }

    /* (non-Javadoc)
     * Method declared on SelectionDispatchAction.
     */
    protected void selectionChanged(IStructuredSelection selection) {
        setEnabled(isEnabled(selection));
    }
    
    private boolean isEnabled(IStructuredSelection selection) {
        if (selection.size() != 1)
            return false;
        Object input= selection.getFirstElement();
        if (!(input instanceof IJavaElement))
            return false;
        switch (((IJavaElement)input).getElementType()) {
            case IJavaElement.METHOD:
                return true;
            default:
                return false;
        }
    }
    
    /* (non-Javadoc)
     * Method declared on SelectionDispatchAction.
     */
    protected void run(ITextSelection selection) {
        IJavaElement input= SelectionConverter.getInput(fEditor);
        if (!ActionUtil.isProcessable(getShell(), input))
            return;     
        
        IJavaElement[] elements= SelectionConverter.codeResolveOrInputHandled(fEditor, getShell(), getDialogTitle());
        if (elements == null)
            return;
        List candidates= new ArrayList(elements.length);
        for (int i= 0; i < elements.length; i++) {
            IJavaElement[] resolvedElements= CallHierarchyUI.getCandidates(elements[i]);
            if (resolvedElements != null)   
                candidates.addAll(Arrays.asList(resolvedElements));
        }
        run((IJavaElement[])candidates.toArray(new IJavaElement[candidates.size()]));
    }
    
    /* (non-Javadoc)
     * Method declared on SelectionDispatchAction.
     */
    protected void run(IStructuredSelection selection) {
        if (selection.size() != 1)
            return;
        Object input= selection.getFirstElement();

        if (!(input instanceof IJavaElement)) {
            IStatus status= createStatus(CallHierarchyMessages.getString("OpenCallHierarchyAction.messages.no_java_element")); //$NON-NLS-1$
            ErrorDialog.openError(getShell(), getDialogTitle(), CallHierarchyMessages.getString("OpenCallHierarchyAction.messages.title"), status); //$NON-NLS-1$
            return;
        }
        IJavaElement element= (IJavaElement) input;
        if (!ActionUtil.isProcessable(getShell(), element))
            return;

        List result= new ArrayList(1);
        IStatus status= compileCandidates(result, element);
        if (status.isOK()) {
            run((IJavaElement[]) result.toArray(new IJavaElement[result.size()]));
        } else {
            ErrorDialog.openError(getShell(), getDialogTitle(), CallHierarchyMessages.getString("OpenCallHierarchyAction.messages.title"), status); //$NON-NLS-1$
        }
    }
    
    private void run(IJavaElement[] elements) {
        if (elements.length == 0) {
            getShell().getDisplay().beep();
            return;
        }
        CallHierarchyUI.open(elements, getSite().getWorkbenchWindow());
    }
    
    private static String getDialogTitle() {
        return CallHierarchyMessages.getString("OpenCallHierarchyAction.dialog.title"); //$NON-NLS-1$
    }
    
    private static IStatus compileCandidates(List result, IJavaElement elem) {
        IStatus ok= new Status(IStatus.OK, JavaPlugin.getPluginId(), 0, "", null); //$NON-NLS-1$        
        switch (elem.getElementType()) {
            case IJavaElement.METHOD:
                result.add(elem);
                return ok;
        }
        return createStatus(CallHierarchyMessages.getString("OpenCallHierarchyAction.messages.no_valid_java_element")); //$NON-NLS-1$
    }
    
    private static IStatus createStatus(String message) {
        return new Status(IStatus.INFO, JavaPlugin.getPluginId(), IJavaStatusConstants.INTERNAL_ERROR, message, null);
    }           
}
