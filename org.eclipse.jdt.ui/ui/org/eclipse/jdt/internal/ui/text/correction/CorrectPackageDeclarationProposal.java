/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.ui.text.correction;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.refactoring.changes.CompilationUnitChange;
import org.eclipse.jdt.internal.corext.textmanipulation.SimpleTextEdit;
import org.eclipse.jdt.internal.corext.textmanipulation.TextEdit;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;

public class CorrectPackageDeclarationProposal extends CUCorrectionProposal {

	private IProblemLocation fLocation;

	public CorrectPackageDeclarationProposal(ICompilationUnit cu, IProblemLocation location, int relevance) {
		super(CorrectionMessages.getString("CorrectPackageDeclarationProposal.name"), cu, relevance, //$NON-NLS-1$
			JavaPluginImages.get(JavaPluginImages.IMG_OBJS_PACKDECL)); 
		fLocation= location;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.text.correction.CUCorrectionProposal#createCompilationUnitChange(String, ICompilationUnit, TextEdit)
	 */
	protected CompilationUnitChange createCompilationUnitChange(String name, ICompilationUnit cu, TextEdit root) throws CoreException {
		CompilationUnitChange change= super.createCompilationUnitChange(name, cu, root);
		
		IPackageFragment parentPack= (IPackageFragment) cu.getParent();
		IPackageDeclaration[] decls= cu.getPackageDeclarations();
		
		if (parentPack.isDefaultPackage() && decls.length > 0) {
			for (int i= 0; i < decls.length; i++) {
				ISourceRange range= decls[i].getSourceRange();
				root.add(SimpleTextEdit.createDelete(range.getOffset(), range.getLength()));
			}
			return change;
		}
		if (!parentPack.isDefaultPackage() && decls.length == 0) {
			String lineDelim= StubUtility.getLineDelimiterUsed(cu);
			String str= "package " + parentPack.getElementName() + ";" + lineDelim + lineDelim; //$NON-NLS-1$ //$NON-NLS-2$
			root.add(SimpleTextEdit.createInsert(0, str));
			return change;
		}
		
		root.add(SimpleTextEdit.createReplace(fLocation.getOffset(), fLocation.getLength(), parentPack.getElementName()));
		return change;
	}
	
	/*
	 * @see ICompletionProposal#getDisplayString()
	 */
	public String getDisplayString() {
		ICompilationUnit cu= getCompilationUnit();
		IPackageFragment parentPack= (IPackageFragment) cu.getParent();
		try {
			IPackageDeclaration[] decls= cu.getPackageDeclarations();		
			if (parentPack.isDefaultPackage() && decls.length > 0) {
				return CorrectionMessages.getFormattedString("CorrectPackageDeclarationProposal.remove.description", decls[0].getElementName()); //$NON-NLS-1$
			}
			if (!parentPack.isDefaultPackage() && decls.length == 0) {	
				return (CorrectionMessages.getFormattedString("CorrectPackageDeclarationProposal.add.description",  parentPack.getElementName())); //$NON-NLS-1$
			}
		} catch(JavaModelException e) {
			JavaPlugin.log(e);
		}
		return (CorrectionMessages.getFormattedString("CorrectPackageDeclarationProposal.change.description", parentPack.getElementName())); //$NON-NLS-1$
	}
}
