package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestFibonacciWithCombinationOfRecursiveCalls {
	
	public int fibonacciWithCombinationOfRecursiveCalls(int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		FibonacciWithCombinationOfRecursiveCallsTask aFibonacciWithCombinationOfRecursiveCallsTask = new FibonacciWithCombinationOfRecursiveCallsTask(
				end);
		return pool.invoke(aFibonacciWithCombinationOfRecursiveCallsTask);
	}

	public class FibonacciWithCombinationOfRecursiveCallsTask
			extends
				RecursiveTask<Integer> {
		private int end;
		private FibonacciWithCombinationOfRecursiveCallsTask(int end) {
			this.end = end;
		}
		protected Integer compute() {
			if (end < 10) {
				return fibonacciWithCombinationOfRecursiveCalls_sequential(end);
			} else {
				FibonacciWithCombinationOfRecursiveCallsTask task1 = new FibonacciWithCombinationOfRecursiveCallsTask(
						end - 1);
				FibonacciWithCombinationOfRecursiveCallsTask task2 = new FibonacciWithCombinationOfRecursiveCallsTask(
						end - 2);
				invokeAll(task1, task2);
				int i = task1.getRawResult();
				return i + task2.getRawResult();
			}
		}
		public int fibonacciWithCombinationOfRecursiveCalls_sequential(int end) {
			if (end < 2) {
				return end;
			} else {
				int i = fibonacciWithCombinationOfRecursiveCalls_sequential(end - 1);
				return i
						+ fibonacciWithCombinationOfRecursiveCalls_sequential(end - 2);
			}
		}
	}
}