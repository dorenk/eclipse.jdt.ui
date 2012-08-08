package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestCreateResultField {
	
	public int method(int[] array, int start, int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		MethodImpl aMethodImpl = new MethodImpl(array, start, end);
		return pool.invoke(aMethodImpl);
	}

	public class MethodImpl extends RecursiveTask<Integer> {
		private int[] array;
		private int start;
		private int end;
		private MethodImpl(int[] array, int start, int end) {
			this.array = array;
			this.start = start;
			this.end = end;
		}
		protected Integer compute() {
			if (array.length < 10) {
				return method_sequential(array, start, end);
			} else {
				MethodImpl task1 = new MethodImpl(array, 0, 1);
				MethodImpl task2 = new MethodImpl(new int[]{1, 2, 3}, 0, 3);
				invokeAll(task1, task2);
				int i = task1.getRawResult();
				int j = task2.getRawResult();
				return i + j;
			}
		}
		public int method_sequential(int[] array, int start, int end) {
			if (array.length == 0) {
				return 0;
			} else {
				int i = method_sequential(array, 0, 1);
				int j = method_sequential(new int[]{1, 2, 3}, 0, 3);
				return i + j;
			}
		}
	}
}