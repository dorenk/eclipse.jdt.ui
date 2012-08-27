package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestBlockCombination9 {
	
	int x9= 0;
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
				TryThisTask task1 = new TryThisTask(x - 1);
				TryThisTask task2 = new TryThisTask(x - 2);
				invokeAll(task1, task2);
				return x9 = task1.getRawResult() + task2.getRawResult();
			}
		}
		public int tryThis_sequential(int x) {
			if (x < 0)
				return 0;
			else
				return x9 = tryThis_sequential(x - 1)
						+ tryThis_sequential(x - 2);
		}
	}
}