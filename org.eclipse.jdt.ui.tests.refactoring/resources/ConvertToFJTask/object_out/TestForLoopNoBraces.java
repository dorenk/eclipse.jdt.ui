package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestForLoopNoBraces {
	
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
				int total = 0;
				for (int i = 0; i < 10; i++) {
					MethodTask task1 = new MethodTask(end - i);
					MethodTask task2 = new MethodTask(end - i * 2);
					invokeAll(task1, task2);
					total += task1.getRawResult() + task2.getRawResult();
				}
				return total;
			}
		}
		public int method_sequential(int end) {
			if (end <= 0)
				return 1;
			else {
				int total = 0;
				for (int i = 0; i < 10; i++)
					total += method_sequential(end - i)
							+ method_sequential(end - i * 2);
				return total;
			}
		}
	}
}