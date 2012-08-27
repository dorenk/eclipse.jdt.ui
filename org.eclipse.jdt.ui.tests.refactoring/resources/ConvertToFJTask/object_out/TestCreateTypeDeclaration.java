package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestCreateTypeDeclaration {
	
	public void method(int[] array, int start, int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		MethodTask aMethodTask = new MethodTask(array, start, end);
		pool.invoke(aMethodTask);
	}

	public class MethodTask extends RecursiveAction {
		private int[] array;
		private int start;
		private int end;
		private MethodTask(int[] array, int start, int end) {
			this.array = array;
			this.start = start;
			this.end = end;
		}
		protected void compute() {
			if (array.length < 10) {
				method_sequential(array, start, end);
				return;
			} else {
				MethodTask task1 = new MethodTask(array, 0, 1);
				MethodTask task2 = new MethodTask(new int[]{1, 2, 3}, 0, 3);
				invokeAll(task1, task2);
			}
		}
		public void method_sequential(int[] array, int start, int end) {
			if (array.length == 0) {
				return;
			} else {
				method_sequential(array, 0, 1);
				method_sequential(new int[]{1, 2, 3}, 0, 3);
			}
		}
	}
}