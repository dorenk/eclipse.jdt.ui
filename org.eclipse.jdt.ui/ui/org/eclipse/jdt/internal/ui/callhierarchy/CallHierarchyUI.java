/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *   Jesper Kamstrup Linnet (eclipse@kamstrup-linnet.dk) - initial API and implementation 
 * 			(report 36180: Callers/Callees view)
 ******************************************************************************/
package org.eclipse.jdt.internal.ui.callhierarchy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.ui.JavaUI;

import org.eclipse.jdt.internal.ui.IJavaStatusConstants;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.actions.OpenActionUtil;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.CallLocation;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;

public class CallHierarchyUI {
    private static final int DEFAULT_MAX_CALL_DEPTH= 10;    
    private static final String PREF_MAX_CALL_DEPTH = "PREF_MAX_CALL_DEPTH"; //$NON-NLS-1$

    private static CallHierarchyUI fgInstance;

    private CallHierarchyUI() {
        // Do nothing
    }

    public static CallHierarchyUI getDefault() {
        if (fgInstance == null) {
            fgInstance = new CallHierarchyUI();
        }

        return fgInstance;
    }

    /**
     * Returns the maximum tree level allowed
     * @return int
     */
    public int getMaxCallDepth() {
        int maxCallDepth;
        
        IPreferenceStore settings = JavaPlugin.getDefault().getPreferenceStore();
        maxCallDepth = settings.getInt(PREF_MAX_CALL_DEPTH);
        if (maxCallDepth < 1 || maxCallDepth > 99) {
            maxCallDepth= DEFAULT_MAX_CALL_DEPTH;
        }

        return maxCallDepth;
    }

    public void setMaxCallDepth(int maxCallDepth) {
        IPreferenceStore settings = JavaPlugin.getDefault().getPreferenceStore();
        settings.setValue(PREF_MAX_CALL_DEPTH, maxCallDepth);
    }
    
    public static void jumpToMember(IJavaElement element) {
        if (element != null) {
            try {
                IEditorPart methodEditor = EditorUtility.openInEditor(element, true);
                JavaUI.revealInEditor(methodEditor, element);
            } catch (JavaModelException e) {
                JavaPlugin.log(e);
            } catch (PartInitException e) {
                JavaPlugin.log(e);
            }
        }
    }

    public static void jumpToLocation(CallLocation callLocation) {
        try {
            IEditorPart methodEditor = EditorUtility.openInEditor(callLocation.getMember(),
                    false);

            if (methodEditor instanceof ITextEditor) {
                ITextEditor editor = (ITextEditor) methodEditor;
                editor.selectAndReveal(callLocation.getStart(),
                    (callLocation.getEnd() - callLocation.getStart()));
            }
        } catch (JavaModelException e) {
            JavaPlugin.log(e);
        } catch (PartInitException e) {
            JavaPlugin.log(e);
        }
    }

    public static void openInEditor(Object element, Shell shell, String title) {
        CallLocation callLocation= CallHierarchy.getCallLocation(element);

        if (callLocation == null) {
            return;
        }

        try {
            boolean activateOnOpen = OpenStrategy.activateOnOpen();

            IEditorPart methodEditor = EditorUtility.openInEditor(callLocation.getMember(),
                    activateOnOpen);

            if (methodEditor instanceof ITextEditor) {
                ITextEditor editor = (ITextEditor) methodEditor;
                editor.selectAndReveal(callLocation.getStart(),
                    (callLocation.getEnd() - callLocation.getStart()));
            }
        } catch (JavaModelException e) {
            JavaPlugin.log(new Status(IStatus.ERROR, JavaPlugin.getPluginId(),
                    IJavaStatusConstants.INTERNAL_ERROR,
                    CallHierarchyMessages.getString(
                        "CallHierarchyUI.open_in_editor.error.message"), e)); //$NON-NLS-1$

            ErrorDialog.openError(shell, title,
                CallHierarchyMessages.getString(
                    "CallHierarchyUI.open_in_editor.error.messageProblems"), //$NON-NLS-1$
                e.getStatus());
        } catch (PartInitException x) {
            String name = callLocation.getCalledMember().getElementName();
            MessageDialog.openError(shell,
                CallHierarchyMessages.getString(
                    "CallHierarchyUI.open_in_editor.error.messageProblems"), //$NON-NLS-1$
                CallHierarchyMessages.getFormattedString(
                    "CallHierarchyUI.open_in_editor.error.messageArgs", //$NON-NLS-1$
                    new String[] { name, x.getMessage() }));
        }
    }

    /**
     * @param elem
     * @return
     */
    public static IEditorPart isOpenInEditor(Object elem) {
        IJavaElement javaElement= null;
        if (elem instanceof MethodWrapper) {
            javaElement= ((MethodWrapper) elem).getMember();
        } else if (elem instanceof CallLocation) {
            javaElement= ((CallLocation) elem).getCalledMember();
        }
        if (javaElement != null) {
            return EditorUtility.isOpenInEditor(javaElement);
        }
        return null;
    }

    /**
     * Converts the input to a possible input candidates
     */ 
    public static IJavaElement[] getCandidates(Object input) {
        if (!(input instanceof IJavaElement)) {
            return null;
        }
        IJavaElement elem= (IJavaElement) input;
        if (elem.getElementType() == IJavaElement.METHOD) {
            return new IJavaElement[] { elem };
        }
        return null;    
    }
    
    public static CallHierarchyViewPart open(IJavaElement[] candidates, IWorkbenchWindow window) {
        Assert.isTrue(candidates != null && candidates.length != 0);
            
        IJavaElement input= null;
        if (candidates.length > 1) {
            String title= CallHierarchyMessages.getString("CallHierarchyUI.selectionDialog.title");  //$NON-NLS-1$
            String message= CallHierarchyMessages.getString("CallHierarchyUI.selectionDialog.message"); //$NON-NLS-1$
            input= OpenActionUtil.selectJavaElement(candidates, window.getShell(), title, message);         
        } else {
            input= candidates[0];
        }
        if (input == null)
            return null;
            
        return openInViewPart(window, input);
    }

    private static void openEditor(Object input, boolean activate) throws PartInitException, JavaModelException {
        IEditorPart part= EditorUtility.openInEditor(input, activate);
        if (input instanceof IJavaElement)
            EditorUtility.revealInEditor(part, (IJavaElement) input);
    }
    
    private static CallHierarchyViewPart openInViewPart(IWorkbenchWindow window, IJavaElement input) {
        IWorkbenchPage page= window.getActivePage();
        try {
            CallHierarchyViewPart result= (CallHierarchyViewPart)page.showView(CallHierarchyViewPart.ID_CALL_HIERARCHY);
            result.setMethod((IMethod)input);
            openEditor(input, false);
            return result;
        } catch (CoreException e) {
            ExceptionHandler.handle(e, window.getShell(), 
                CallHierarchyMessages.getString("CallHierarchyUI.error.open_view"), e.getMessage()); //$NON-NLS-1$
        }
        return null;        
    }
    
    /**
     * Converts an ISelection (containing MethodWrapper instances) to an ISelection
     * with the MethodWrapper's replaced by their corresponding IMembers. If the selection
     * contains elements which are not MethodWrapper instances or not already IMember instances
     * they are discarded.  
     * @param selection The selection to convert.
     * @return An ISelection containing IMember's in place of MethodWrapper instances.
     */
    static ISelection convertSelection(ISelection selection) {
        if (selection.isEmpty()) {
            return selection;   
        }
        
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection= (IStructuredSelection) selection;
            List javaElements= new ArrayList();
            for (Iterator iter= structuredSelection.iterator(); iter.hasNext();) {
                Object element= iter.next();
                if (element instanceof MethodWrapper) {
                    IMember member= ((MethodWrapper)element).getMember();
                    if (member != null) {
                        javaElements.add(member);
                    }
                } else if (element instanceof IMember) {
                    javaElements.add(element);
                }
            }
            return new StructuredSelection(javaElements);
        }
        return StructuredSelection.EMPTY; 
    }
}
