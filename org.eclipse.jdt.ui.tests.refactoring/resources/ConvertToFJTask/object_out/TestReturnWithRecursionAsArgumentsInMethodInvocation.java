package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestReturnWithRecursionAsArgumentsInMethodInvocation {
	
	public int recursion(int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		RecursionImpl aRecursionImpl = new RecursionImpl(end);
		pool.invoke(aRecursionImpl);
		return aRecursionImpl.result;
	}
	public class RecursionImpl extends RecursiveAction {
		private int end;
		private int result;
		private RecursionImpl(int end) {
			this.end = end;
		}
		protected void compute() {
			if (end < 5) {
				result = recursion_sequential(end);
				return;
			} else {
				RecursionImpl task1 = new RecursionImpl(end - 1);
				RecursionImpl task2 = new RecursionImpl(end - 2);
				invokeAll(task1, task2);
				result = sum(task1.result, task2.result);
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