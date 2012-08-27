package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestSumWithCombinationOfRecursiveCalls {
	
	public int recursionSumWithCombinationOfRecursiveCalls(int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		RecursionSumWithCombinationOfRecursiveCallsTask aRecursionSumWithCombinationOfRecursiveCallsTask = new RecursionSumWithCombinationOfRecursiveCallsTask(
				end);
		return pool.invoke(aRecursionSumWithCombinationOfRecursiveCallsTask);
	}
	public class RecursionSumWithCombinationOfRecursiveCallsTask
			extends
				RecursiveTask<Integer> {
		private int end;
		private RecursionSumWithCombinationOfRecursiveCallsTask(int end) {
			this.end = end;
		}
		protected Integer compute() {
			if (end < 5) {
				return recursionSumWithCombinationOfRecursiveCalls_sequential(end);
			} else {
				RecursionSumWithCombinationOfRecursiveCallsTask task1 = new RecursionSumWithCombinationOfRecursiveCallsTask(
						end - 1);
				RecursionSumWithCombinationOfRecursiveCallsTask task2 = new RecursionSumWithCombinationOfRecursiveCallsTask(
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