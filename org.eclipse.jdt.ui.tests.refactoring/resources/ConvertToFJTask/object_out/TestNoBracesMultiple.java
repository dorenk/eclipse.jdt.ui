package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestNoBracesMultiple {
	
	public int method(int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		MethodTask aMethodTask = new MethodTask(end);
		return pool.invoke(aMethodTask);
	}
	public class MethodTask extends RecursiveTask<Integer> {
		private int end;
		private MethodTask(int end) {
			this.end = end;
		}
		protected Integer compute() {
			if (end < 10) {
				return method_sequential(end);
			} else {
				MethodTask task1 = new MethodTask(end - 1);
				MethodTask task2 = new MethodTask(end - 2);
				MethodTask task3 = new MethodTask(end - 3);
				invokeAll(task1, task2, task3);
				return otherMethod(task1.getRawResult(), task2.getRawResult(),
						task3.getRawResult());
			}
		}
		public int method_sequential(int end) {
			if (end <= 0)
				return 1;
			else
				return otherMethod(method_sequential(end - 1),
						method_sequential(end - 2), method_sequential(end - 3));
		}
	}
	public int otherMethod(int x, int y, int z) {
		return x + y + z;
	}
}