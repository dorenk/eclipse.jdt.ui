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

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;

import org.eclipse.jdt.internal.corext.dom.NodeFinder;
import org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation;

/**
 *
 */
public class ProblemLocation implements IProblemLocation {
	
	private int fId;
	private String[] fArguments;
	private int fOffset;
	private int fLength;
	
	public ProblemLocation(int offset, int length, IJavaAnnotation annotation) {
		fId= annotation.getId();
		fArguments= annotation.getArguments();
		fOffset= offset;
		fLength= length;
	}
	
	public ProblemLocation(int offset, int length, int id, String[] arguments) {
		fId= id;
		fArguments= arguments;
		fOffset= offset;
		fLength= length;
	}	


	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.text.correction.IProblemLocation#getProblemId()
	 */
	public int getProblemId() {
		return fId;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.text.correction.IProblemLocation#getProblemArguments()
	 */
	public String[] getProblemArguments() {
		return fArguments;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.text.correction.IProblemLocation#getLength()
	 */
	public int getLength() {
		return fLength;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.text.correction.IProblemLocation#getOffset()
	 */
	public int getOffset() {
		return fOffset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.text.correction.IProblemLocation#getCoveringNode(org.eclipse.jdt.internal.ui.text.correction.IAssistContext)
	 */
	public ASTNode getCoveringNode(IAssistContext context) {
		NodeFinder finder= new NodeFinder(fOffset, fLength);
		context.getASTRoot().accept(finder);
		return finder.getCoveringNode();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.text.correction.IProblemLocation#getCoveredNode(org.eclipse.jdt.internal.ui.text.correction.IAssistContext)
	 */
	public ASTNode getCoveredNode(IAssistContext context) {
		NodeFinder finder= new NodeFinder(fOffset, fLength);
		context.getASTRoot().accept(finder);
		return finder.getCoveredNode();
	}
	
	public String toString() {
		StringBuffer buf= new StringBuffer();
		buf.append("Id: ").append(getErrorCode(fId)).append('\n'); //$NON-NLS-1$
		buf.append('[').append(fOffset).append(", ").append(fOffset).append(']').append('\n'); //$NON-NLS-1$
		String[] arg= fArguments;
		if (arg != null) {
			for (int i= 0; i < arg.length; i++) {
				buf.append(arg[i]);
				buf.append('\n');				 //$NON-NLS-1$
			}
		}
		return buf.toString();
	}
	
	private String getErrorCode(int code) {
		StringBuffer buf= new StringBuffer();
			
		if ((code & IProblem.TypeRelated) != 0) {
			buf.append("TypeRelated + "); //$NON-NLS-1$
		}
		if ((code & IProblem.FieldRelated) != 0) {
			buf.append("FieldRelated + "); //$NON-NLS-1$
		}
		if ((code & IProblem.ConstructorRelated) != 0) {
			buf.append("ConstructorRelated + "); //$NON-NLS-1$
		}
		if ((code & IProblem.MethodRelated) != 0) {
			buf.append("MethodRelated + "); //$NON-NLS-1$
		}
		if ((code & IProblem.ImportRelated) != 0) {
			buf.append("ImportRelated + "); //$NON-NLS-1$
		}
		if ((code & IProblem.Internal) != 0) {
			buf.append("Internal + "); //$NON-NLS-1$
		}
		if ((code & IProblem.Syntax) != 0) {
			buf.append("Syntax + "); //$NON-NLS-1$
		}
		buf.append(code & IProblem.IgnoreCategoriesMask);
			
		return buf.toString();
	}	



}
