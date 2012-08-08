package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestReturnWithRecursionAsArgumentsInMethodInvocation {
	
	public int recursion(int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		RecursionImpl aRecursionImpl = new RecursionImpl(end);
		return pool.invoke(aRecursionImpl);
	}
	public class RecursionImpl extends RecursiveTask<Integer> {
		private int end;
		private RecursionImpl(int end) {
			this.end = end;
		}
		protected Integer compute() {
			if (end < 5) {
				return recursion_sequential(end);
			} else {
				RecursionImpl task1 = new RecursionImpl(end - 1);
				RecursionImpl task2 = new RecursionImpl(end - 2);
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