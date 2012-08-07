package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestFibonacciWithCombinationOfRecursiveCalls {
	
	public int fibonacciWithCombinationOfRecursiveCalls(int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		FibonacciWithCombinationOfRecursiveCallsImpl aFibonacciWithCombinationOfRecursiveCallsImpl = new FibonacciWithCombinationOfRecursiveCallsImpl(
				end);
		return pool.invoke(aFibonacciWithCombinationOfRecursiveCallsImpl);
	}

	public class FibonacciWithCombinationOfRecursiveCallsImpl
			extends
				RecursiveTask<Integer> {
		private int end;
		private FibonacciWithCombinationOfRecursiveCallsImpl(int end) {
			this.end = end;
		}
		protected Integer compute() {
			if (end < 10) {
				return fibonacciWithCombinationOfRecursiveCalls_sequential(end);
			} else {
				FibonacciWithCombinationOfRecursiveCallsImpl task1 = new FibonacciWithCombinationOfRecursiveCallsImpl(
						end - 1);
				FibonacciWithCombinationOfRecursiveCallsImpl task2 = new FibonacciWithCombinationOfRecursiveCallsImpl(
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