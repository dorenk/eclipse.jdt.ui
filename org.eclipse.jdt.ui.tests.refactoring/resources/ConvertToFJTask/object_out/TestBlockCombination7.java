package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestBlockCombination7 {
	
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
				TryThisTask task1 = new TryThisTask(x - 12);
				TryThisTask task2 = new TryThisTask(x - 15);
				invokeAll(task1, task2);
				int x1 = task1.getRawResult();
				int x2 = task2.getRawResult();
				int x3 = 0;
				if (x > 15)
					x3 = tryThis_sequential(x - 16);
				return x1 + x2 + x3;
			}
		}
		public int tryThis_sequential(int x) {
			if (x < 0)
				return 0;
			else {
				int x1 = tryThis_sequential(x - 12);
				int x2 = tryThis_sequential(x - 15);
				int x3 = 0;
				if (x > 15)
					x3 = tryThis_sequential(x - 16);
				return x1 + x2 + x3;
			}
		}
	}
}