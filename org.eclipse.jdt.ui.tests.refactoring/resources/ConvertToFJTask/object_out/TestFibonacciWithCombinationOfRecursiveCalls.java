package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestFibonacciWithCombinationOfRecursiveCalls {
	
	public int fibonacciWithCombinationOfRecursiveCalls(int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		FibonacciWithCombinationOfRecursiveCallsImpl aFibonacciWithCombinationOfRecursiveCallsImpl = new FibonacciWithCombinationOfRecursiveCallsImpl(
				end);
		pool.invoke(aFibonacciWithCombinationOfRecursiveCallsImpl);
		return aFibonacciWithCombinationOfRecursiveCallsImpl.result;
	}

	public class FibonacciWithCombinationOfRecursiveCallsImpl
			extends
				RecursiveAction {
		private int end;
		private int result;
		private FibonacciWithCombinationOfRecursiveCallsImpl(int end) {
			this.end = end;
		}
		protected void compute() {
			if (end < 10) {
				result = fibonacciWithCombinationOfRecursiveCalls_sequential(end);
				return;
			} else {
				FibonacciWithCombinationOfRecursiveCallsImpl task1 = new FibonacciWithCombinationOfRecursiveCallsImpl(
						end - 1);
				FibonacciWithCombinationOfRecursiveCallsImpl task2 = new FibonacciWithCombinationOfRecursiveCallsImpl(
						end - 2);
				invokeAll(task1, task2);
				int i = task1.result;
				result = i + task2.result;
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