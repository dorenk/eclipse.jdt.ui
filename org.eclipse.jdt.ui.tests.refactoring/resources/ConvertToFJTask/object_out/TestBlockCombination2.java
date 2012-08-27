package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestBlockCombination2 {
	
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
					int x12 = method(task1.getRawResult(), task2.getRawResult());
					return x12;
				} else
					return method(1, tryThis_sequential(x - 1));
			}
		}
		public int tryThis_sequential(int x) {
			if (x < 0)
				return 0;
			else {
				if (x > 15) {
					int x12 = method(tryThis_sequential(x - 12),
							tryThis_sequential(x - 15));
					return x12;
				} else
					return method(1, tryThis_sequential(x - 1));
			}
		}
	}
	private int method(int x, int y) {
		return x + y;
	}
}