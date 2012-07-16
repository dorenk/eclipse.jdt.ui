package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestIfStatementWithoutBracesMethod {
	
	protected String grayCheckHierarchy(String treeElement) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		GrayCheckHierarchyImpl aGrayCheckHierarchyImpl = new GrayCheckHierarchyImpl(
				treeElement);
		pool.invoke(aGrayCheckHierarchyImpl);
		return aGrayCheckHierarchyImpl.result;
	}

	protected class GrayCheckHierarchyImpl extends RecursiveAction {
		private String treeElement;
		private String result;
		private GrayCheckHierarchyImpl(String treeElement) {
			this.treeElement = treeElement;
		}
		protected void compute() {
			if (treeElement.length() < 10) {
				result = grayCheckHierarchy(treeElement);
				return;
			}
			if (treeElement.endsWith(";")) {
				treeElement.split(";");
			}
			String parent = treeElement.toUpperCase();
			if (parent != null) {
				GrayCheckHierarchyImpl task1 = new GrayCheckHierarchyImpl(
						parent);
				GrayCheckHierarchyImpl task2 = new GrayCheckHierarchyImpl(
						parent.toLowerCase());
				invokeAll(task1, task2);
				result = task1.result + task2.result;
			}
		}
		protected String grayCheckHierarchy(String treeElement) {
			if (treeElement.contentEquals("gray"))
				return "gray";
			if (treeElement.endsWith(";")) {
				treeElement.split(";");
			}
			String parent = treeElement.toUpperCase();
			if (parent != null)
				return grayCheckHierarchy(parent)
						+ grayCheckHierarchy(parent.toLowerCase());
		}
	}
}