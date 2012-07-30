package org.eclipse.jdt.ui.tests.refactoring;

import java.util.Hashtable;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.corext.refactoring.Checks;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringAvailabilityTester;
import org.eclipse.jdt.internal.corext.refactoring.concurrency.ConvertToFJTaskRefactoring;

public class ConvertToFJTaskTests extends AbstractSelectionTestCase {

	private static ConvertToFJTaskTestSetup fgTestSetup;
	
	public ConvertToFJTaskTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		fgTestSetup= new ConvertToFJTaskTestSetup(new TestSuite(ConvertToFJTaskTests.class));
		return fgTestSetup;
	}
	
	public static Test setUpTest(Test test) {
		fgTestSetup= new ConvertToFJTaskTestSetup(test);
		return fgTestSetup;
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		fIsPreDeltaTest= true;
	}

	protected String getResourceLocation() {
		return "ConvertToFJTask/";
	}
	
	protected String adaptName(String name) {
		return Character.toUpperCase(name.charAt(0)) + name.substring(1) + ".java";
	}	
	
	protected void performTest(IPackageFragment packageFragment, String id, String outputFolder, String methodName, String sequentialThresholdCheck) throws Exception {
		ICompilationUnit unit= createCU(packageFragment, id);
		IMethod method= getMethod(unit, methodName);
		assertNotNull(method);
		
		initializePreferences();

		ConvertToFJTaskRefactoring refactoring= ((Checks.checkAvailability(method).hasFatalError() || !RefactoringAvailabilityTester.isConvertToFJTaskAvailable(method)) ? null : new ConvertToFJTaskRefactoring(method));
		if(refactoring != null) {
			refactoring.setSequentialThreshold(sequentialThresholdCheck);
		}
		performTest(unit, refactoring, COMPARE_WITH_OUTPUT, getProofedContent(outputFolder, id), true);
	}


	protected void performInvalidTest(IPackageFragment packageFragment, String id, String methodName) throws Exception {
		ICompilationUnit unit= createCU(packageFragment, id);
		IMethod method= getMethod(unit, methodName);
		assertNotNull(method);
		
		initializePreferences();

		ConvertToFJTaskRefactoring refactoring= ((Checks.checkAvailability(method).hasFatalError() || !RefactoringAvailabilityTester.isConvertToFJTaskAvailable(method)) ? null : new ConvertToFJTaskRefactoring(method));
		if (refactoring != null) {
			RefactoringStatus status= refactoring.checkAllConditions(new NullProgressMonitor());
			assertTrue(status.hasError());
		}
	}	
	
	private void initializePreferences() {
		Hashtable preferences= new Hashtable();
		preferences.put(JavaCore.CODEASSIST_FIELD_PREFIXES, "");
		preferences.put(JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES, "");
		preferences.put(JavaCore.CODEASSIST_FIELD_SUFFIXES, "");
		preferences.put(JavaCore.CODEASSIST_STATIC_FIELD_SUFFIXES, "");
		JavaCore.setOptions(preferences);
	}
	
	private IMethod getMethod(ICompilationUnit unit, String methodName) throws JavaModelException {
		IType[] types= unit.getAllTypes();
		for (int i= 0; i < types.length; i++) {
			IType type= types[i];
			IMethod[] methods = type.getMethods();
			for (IMethod method : methods) {
				if (method.getElementName().equals(methodName))
					return method;
			}
		}
		return null;
	}

	private void objectTest(String methodName, String sequentialThresholdCheck) throws Exception {
		performTest(fgTestSetup.getObjectPackage(), getName(), "object_out", methodName, sequentialThresholdCheck);
	}
	
//	private void baseTest(String methodName) throws Exception {
//		performTest(fgTestSetup.getBasePackage(), getName(), "base_out", methodName);
//	}
	
	private void invalidTest(String methodName) throws Exception {
		performInvalidTest(fgTestSetup.getInvalidPackage(), getName(), methodName);
	}
	
//	private void existingTest(String methodName) throws Exception {
//		performTest(fgTestSetup.getExistingMethodPackage(), getName(), "existingmethods_out", methodName);
//	}
	
	//=====================================================================================
	// Basic Object Test
	//=====================================================================================
	
	public void testCreateTypeDeclaration() throws Exception {
		objectTest("method", "array.length < 10");
	}
	
	public void testCreateResultField() throws Exception {
		objectTest("method", "array.length < 10");
	}
	
	public void testMaxConsecutiveSum() throws Exception {
		objectTest("maxSumRec", "right -left < 4");
	}
	
	public void testSequentialMergeSort() throws Exception {
		objectTest("sort", "whole.length < 10");
	}
	
	public void testQuickSort() throws Exception {
		objectTest("quicksort", "right - left < 10");
	}
	
//	public void testReimplementRecursiveMethod() throws Exception {
//		objectTest("method");
//	}
	
	public void testFibonacci() throws Exception {
		objectTest("fibonacci", "end < 10");
	}
	
	public void testFibonacciCombination() throws Exception {
		objectTest("fibonacciCombination", "end < 10");
	}
	
	public void testSum() throws Exception {
		objectTest("recursionSum", "end < 5");
	}
	
	public void testSumCombination() throws Exception {
		objectTest("recursionSumCombination", "end < 5");
	}
	
	public void testCreateMultipleTasks() throws Exception {
		objectTest("method", "num < 10");
	}
	
	public void testReturnMultipleTasks() throws Exception {
		objectTest("method", "num < 10");
	}
	
	public void testMethodMultipleTasks() throws Exception {
		objectTest("method", "num < 10");
	}
	
	public void testNoBraces() throws Exception {
		objectTest("method", "end < 10");
	}
	
	public void testNoBracesMultiple() throws Exception {
		objectTest("method", "end < 10");
	}
	
	public void testIfStatementWithoutBracesMethod() throws Exception {
		objectTest("grayCheckHierarchy", "treeElement.length() < 10");
	}
	
	public void testIfception() throws Exception {
		objectTest("method", "end < 10");
	}
	
	public void testMultiple0() throws Exception {
		objectTest("test0", "x < 10");
	}
	
	public void testMultiple1() throws Exception {
		objectTest("test1", "x < 10");
	}
	
	public void testMultiple2() throws Exception {
		objectTest("test2", "x < 10");
	}
	
	public void testBlockCombination0() throws Exception {
		objectTest("tryThis", "x < 10");
	}
	
	public void testBlockCombination1() throws Exception {
		objectTest("tryThis", "x < 10");
	}
	
	public void testBlockCombination2() throws Exception {
		objectTest("tryThis", "x < 10");
	}
	
	public void testBlockCombination3() throws Exception {
		objectTest("tryThis", "x < 10");
	}
	
	public void testBlockCombination4() throws Exception {
		objectTest("tryThis", "x < 10");
	}
	
	public void testBlockCombination5() throws Exception {
		objectTest("tryThis", "x < 10");
	}
	
	public void testBlockCombination6() throws Exception {
		objectTest("tryThis", "x < 10");
	}
	
	public void testBlockCombination7() throws Exception {
		objectTest("tryThis", "x < 10");
	}
	
	public void testBlockCombination8() throws Exception {
		objectTest("tryThis", "x < 10");
	}
	
	public void testForLoopNoBraces() throws Exception {
		objectTest("method", "end < 10");
	}
	
	//=====================================================================================
	// Basic Invalid Test
	//=====================================================================================
	
	public void testBaseCaseDoesNotHaveReturn() throws Exception {
		invalidTest("method");
	}
	
	public void testBaseCaseHasRecursiveCall() throws Exception {
		invalidTest("method");
	}
	
	public void testMethodDoesNotHaveRecursion() throws Exception {
		invalidTest("method");
	}
	
	public void testTooManyBaseCaseOptions() throws Exception {
		invalidTest("method");
	}
	
	public void testBaseCaseAfterReturn() throws Exception {
		invalidTest("getQualifiedName");
	}
	
	public void testSwitchStatements() throws Exception {
		invalidTest("state");
	}
	
	public void testBooleanReturn() throws Exception {
		invalidTest("elementAncestor");
	}
	
	public void testConditionalChain() throws Exception {
		invalidTest("resolveBinding");
	}
	
	public void testIfStatementWithoutBraces() throws Exception {
		invalidTest("grayCheckHierarchy");
	}
	
	public void testOnlyOneCall() throws Exception {
		invalidTest("yesterday");
	}
	
	public void testAbstractMethod() throws Exception {
		invalidTest("tomorrow");
	}
	
	public void testNoBody() throws Exception {
		invalidTest("today");
	}
	
	public void testUnavailableOperation() throws Exception {
		invalidTest("threeDaysIntoFuture");
	}
}
