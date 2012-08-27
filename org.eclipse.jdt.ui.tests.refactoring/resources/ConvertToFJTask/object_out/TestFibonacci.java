package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestFibonacci {
	
	public int fibonacci(int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		FibonacciTask aFibonacciTask = new FibonacciTask(end);
		return pool.invoke(aFibonacciTask);
	}

	public class FibonacciTask extends RecursiveTask<Integer> {
		private int end;
		private FibonacciTask(int end) {
			this.end = end;
		}
		protected Integer compute() {
			if (end < 10) {
				return fibonacci_sequential(end);
			} else {
				FibonacciTask task1 = new FibonacciTask(end - 1);
				FibonacciTask task2 = new FibonacciTask(end - 2);
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