package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestBlockCombination1 {
	
	public int tryThis(int x) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		TryThisTask aTryThisTask = new TryThisTask(x);
		return pool.invoke(aTryThisTask);
	}

	public class TryThisTask extends RecursiveTask<Integer> {
		private int x;
		private TryThisTask(int x) {
			this.x = x;
		}
		protected Integer compute() {
			if (x < 10) {
				return tryThis_sequential(x);
			} else {
				if (x > 15) {
					TryThisTask task1 = new TryThisTask(x - 12);
					TryThisTask task2 = new TryThisTask(x - 15);
					invokeAll(task1, task2);
					int x1 = task1.getRawResult();
					int x2 = task2.getRawResult();
					return x1 + x2;
				} else {
					int x3 = tryThis_sequential(x - 1);
					return x3;
				}
			}
		}
		public int tryThis_sequential(int x) {
			if (x < 0)
				return 0;
			else {
				if (x > 15) {
					int x1 = tryThis_sequential(x - 12);
					int x2 = tryThis_sequential(x - 15);
					return x1 + x2;
				} else {
					int x3 = tryThis_sequential(x - 1);
					return x3;
				}
			}
		}
	}
}