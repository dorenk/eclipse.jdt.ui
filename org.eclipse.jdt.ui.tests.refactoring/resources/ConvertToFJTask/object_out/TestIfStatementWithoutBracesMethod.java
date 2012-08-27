package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestIfStatementWithoutBracesMethod {
	
	protected String grayCheckHierarchy(String treeElement) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		GrayCheckHierarchyTask aGrayCheckHierarchyTask = new GrayCheckHierarchyTask(
				treeElement);
		return pool.invoke(aGrayCheckHierarchyTask);
	}

	protected class GrayCheckHierarchyTask extends RecursiveTask<String> {
		private String treeElement;
		private GrayCheckHierarchyTask(String treeElement) {
			this.treeElement = treeElement;
		}
		protected String compute() {
			if (treeElement.length() < 10) {
				return grayCheckHierarchy_sequential(treeElement);
			}
			if (treeElement.endsWith(";")) {
				treeElement.split(";");
			}
			String parent = treeElement.toUpperCase();
			if (parent != null) {
				GrayCheckHierarchyTask task1 = new GrayCheckHierarchyTask(
						parent);
				GrayCheckHierarchyTask task2 = new GrayCheckHierarchyTask(
						parent.toLowerCase());
				invokeAll(task1, task2);
				return task1.getRawResult() + task2.getRawResult();
			}
		}
		protected String grayCheckHierarchy_sequential(String treeElement) {
			if (treeElement.contentEquals("gray"))
				return "gray";
			if (treeElement.endsWith(";")) {
				treeElement.split(";");
			}
			String parent = treeElement.toUpperCase();
			if (parent != null)
				return grayCheckHierarchy_sequential(parent)
						+ grayCheckHierarchy_sequential(parent.toLowerCase());
		}
	}
}