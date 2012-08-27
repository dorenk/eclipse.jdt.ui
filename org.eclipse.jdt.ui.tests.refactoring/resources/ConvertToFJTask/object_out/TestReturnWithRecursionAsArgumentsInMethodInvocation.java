package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestReturnWithRecursionAsArgumentsInMethodInvocation {
	
	public int recursion(int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		RecursionTask aRecursionTask = new RecursionTask(end);
		return pool.invoke(aRecursionTask);
	}
	public class RecursionTask extends RecursiveTask<Integer> {
		private int end;
		private RecursionTask(int end) {
			this.end = end;
		}
		protected Integer compute() {
			if (end < 5) {
				return recursion_sequential(end);
			} else {
				RecursionTask task1 = new RecursionTask(end - 1);
				RecursionTask task2 = new RecursionTask(end - 2);
				invokeAll(task1, task2);
				return sum(task1.getRawResult(), task2.getRawResult());
			}
		}
		public int recursion_sequential(int end) {
			if (end <= 0) {
				return 0;
			} else {
				return sum(recursion_sequential(end - 1),
						recursion_sequential(end - 2));
			}
		}
	}
	public int sum(int a, int b) {
		return a + b;
	}
}