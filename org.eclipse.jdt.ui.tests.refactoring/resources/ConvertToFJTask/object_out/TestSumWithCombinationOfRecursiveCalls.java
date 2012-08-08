package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestSumWithCombinationOfRecursiveCalls {
	
	public int recursionSumWithCombinationOfRecursiveCalls(int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		RecursionSumWithCombinationOfRecursiveCallsImpl aRecursionSumWithCombinationOfRecursiveCallsImpl = new RecursionSumWithCombinationOfRecursiveCallsImpl(
				end);
		return pool.invoke(aRecursionSumWithCombinationOfRecursiveCallsImpl);
	}
	public class RecursionSumWithCombinationOfRecursiveCallsImpl
			extends
				RecursiveTask<Integer> {
		private int end;
		private RecursionSumWithCombinationOfRecursiveCallsImpl(int end) {
			this.end = end;
		}
		protected Integer compute() {
			if (end < 5) {
				return recursionSumWithCombinationOfRecursiveCalls_sequential(end);
			} else {
				RecursionSumWithCombinationOfRecursiveCallsImpl task1 = new RecursionSumWithCombinationOfRecursiveCallsImpl(
						end - 1);
				RecursionSumWithCombinationOfRecursiveCallsImpl task2 = new RecursionSumWithCombinationOfRecursiveCallsImpl(
						end - 2);
				invokeAll(task1, task2);
				int i = task1.getRawResult();
				return sumCombination(i, task2.getRawResult());
			}
		}
		public int recursionSumWithCombinationOfRecursiveCalls_sequential(
				int end) {
			if (end <= 0) {
				return 0;
			} else {
				int i = recursionSumWithCombinationOfRecursiveCalls_sequential(end - 1);
				return sumCombination(
						i,
						recursionSumWithCombinationOfRecursiveCalls_sequential(end - 2));
			}
		}
	}
	public int sumCombination(int a, int b) {
		return a + b;
	}
}