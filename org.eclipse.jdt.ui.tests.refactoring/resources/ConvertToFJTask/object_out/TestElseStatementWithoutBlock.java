package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestElseStatementWithoutBlock {
	
	public int count(int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		CountImpl aCountImpl = new CountImpl(end);
		pool.invoke(aCountImpl);
		return aCountImpl.result;
	}

	public class CountImpl extends RecursiveAction {
		private int end;
		private int result;
		private CountImpl(int end) {
			this.end = end;
		}
		protected void compute() {
			if (end < 10) {
				result = count_sequential(end);
				return;
			} else {
				CountImpl task1 = new CountImpl(end - 1);
				CountImpl task2 = new CountImpl(end - 2);
				invokeAll(task1, task2);
				result = task1.result + task2.result;
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