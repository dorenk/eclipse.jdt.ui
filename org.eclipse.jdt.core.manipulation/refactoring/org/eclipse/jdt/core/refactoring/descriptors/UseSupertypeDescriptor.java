/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.refactoring.descriptors;

import org.eclipse.core.runtime.Assert;

import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;

import org.eclipse.jdt.internal.core.refactoring.descriptors.DescriptorMessages;

/**
 * Refactoring descriptor for the use supertype refactoring.
 * <p>
 * An instance of this refactoring descriptor may be obtained by calling
 * {@link RefactoringContribution#createDescriptor()} on a refactoring
 * contribution requested by invoking
 * {@link RefactoringCore#getRefactoringContribution(String)} with the
 * appropriate refactoring id.
 * </p>
 * <p>
 * Note: this class is not intended to be instantiated by clients.
 * </p>
 * 
 * @since 3.3
 */
public final class UseSupertypeDescriptor extends JavaRefactoringDescriptor {

	/** The instanceof attribute */
	private static final String ATTRIBUTE_INSTANCEOF= "instanceof"; //$NON-NLS-1$

	/** The instanceof attribute */
	private boolean fInstanceof= false;

	/** The subtype attribute */
	private IType fSubType= null;

	/** The supertype attribute */
	private IType fSupertype= null;

	/**
	 * Creates a new refactoring descriptor.
	 */
	public UseSupertypeDescriptor() {
		super(IJavaRefactorings.USE_SUPER_TYPE);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void populateArgumentMap() {
		super.populateArgumentMap();
		fArguments.put(ATTRIBUTE_INSTANCEOF, Boolean.valueOf(fInstanceof).toString());
		fArguments.put(JavaRefactoringDescriptor.ATTRIBUTE_INPUT, elementToHandle(getProject(), fSubType));
		fArguments.put(JavaRefactoringDescriptor.ATTRIBUTE_ELEMENT + 1, elementToHandle(getProject(), fSupertype));
	}

	/**
	 * Determines whether 'instanceof' statements are considered as candidates
	 * to replace the subtype occurrence by one of its supertypes.
	 * <p>
	 * The default is to not replace the subtype occurrence.
	 * </p>
	 * 
	 * @param replace
	 *            <code>true</code> to replace subtype occurrences in
	 *            'instanceof' statements, <code>false</code> otherwise
	 */
	public void setReplaceInstanceof(final boolean replace) {
		fInstanceof= replace;
	}

	/**
	 * Sets the subtype of the refactoring.
	 * <p>
	 * Occurrences of the subtype are replaced by the supertype set by
	 * {@link #setSupertype(IType)} where possible.
	 * </p>
	 * 
	 * @param type
	 *            the subtype to set
	 */
	public void setSubtype(final IType type) {
		Assert.isNotNull(type);
		fSubType= type;
	}

	/**
	 * Sets the supertype of the refactoring.
	 * <p>
	 * Occurrences of the subtype set by {@link #setSubtype(IType)} are replaced
	 * by the supertype where possible.
	 * </p>
	 * 
	 * @param type
	 *            the supertype to set
	 */
	public void setSupertype(final IType type) {
		Assert.isNotNull(type);
		fSupertype= type;
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringStatus validateDescriptor() {
		RefactoringStatus status= super.validateDescriptor();
		if (fSubType == null)
			status.merge(RefactoringStatus.createFatalErrorStatus(DescriptorMessages.UseSupertypeDescriptor_no_subtype));
		if (fSupertype == null)
			status.merge(RefactoringStatus.createFatalErrorStatus(DescriptorMessages.UseSupertypeDescriptor_no_supertype));
		return status;
	}
}