/*******************************************************************************
 * Copyright (c) 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.corext.refactoring.participants;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.internal.corext.refactoring.base.IChange;
import org.eclipse.jdt.internal.corext.refactoring.base.RefactoringStatus;


public interface IRenameProcessor {

	public void initialize(RenameRefactoring refactoring, Object element) throws CoreException;
	
	public String getRefactoringName();
	
	public boolean isAvailable();
	
	public Object[] getProcessableElements();
	
	public String getCurrentName();
	
	public RefactoringStatus checkNewName(String newName) throws CoreException;
	
	public String getNewName();
	
	public void setNewName(String newName);
	
	public RefactoringStatus checkActivation() throws CoreException;
	
	public RefactoringStatus checkInput(IProgressMonitor pm) throws CoreException;
	
	public IChange createChange(IProgressMonitor pm) throws CoreException;

}
