package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestFibonacci {
	
	public int fibonacci(int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		FibonacciImpl aFibonacciImpl = new FibonacciImpl(end);
		return pool.invoke(aFibonacciImpl);
	}

	public class FibonacciImpl extends RecursiveTask<Integer> {
		private int end;
		private FibonacciImpl(int end) {
			this.end = end;
		}
		protected Integer compute() {
			if (end < 10) {
				return fibonacci_sequential(end);
			} else {
				FibonacciImpl task1 = new FibonacciImpl(end - 1);
				FibonacciImpl task2 = new FibonacciImpl(end - 2);
				invokeAll(task1, task2);
				return task1.getRawResult() + task2.getRawResult();
			}
		}
		public int fibonacci_sequential(int end) {
			if (end < 2) {
				return end;
			} else {
				return fibonacci_sequential(end - 1)
						+ fibonacci_sequential(end - 2);
			}
		}
	}
}