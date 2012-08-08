package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestElseStatementWithoutBlock {
	
	public int count(int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		CountImpl aCountImpl = new CountImpl(end);
		return pool.invoke(aCountImpl);
	}

	public class CountImpl extends RecursiveTask<Integer> {
		private int end;
		private CountImpl(int end) {
			this.end = end;
		}
		protected Integer compute() {
			if (end < 10) {
				return count_sequential(end);
			} else {
				CountImpl task1 = new CountImpl(end - 1);
				CountImpl task2 = new CountImpl(end - 2);
				invokeAll(task1, task2);
				return task1.getRawResult() + task2.getRawResult();
			}
		}
		public int count_sequential(int end) {
			if (end <= 0)
				return 1;
			else
				return count_sequential(end - 1) + count_sequential(end - 2);
		}
	}
}