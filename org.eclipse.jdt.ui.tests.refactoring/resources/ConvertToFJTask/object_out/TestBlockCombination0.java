package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestBlockCombination0 {
	
	public int tryThis(int x) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		TryThisImpl aTryThisImpl = new TryThisImpl(x);
		return pool.invoke(aTryThisImpl);
	}

	public class TryThisImpl extends RecursiveTask<Integer> {
		private int x;
		private TryThisImpl(int x) {
			this.x = x;
		}
		protected Integer compute() {
			if (x < 10) {
				return tryThis_sequential(x);
			} else {
				if (x > 15) {
					TryThisImpl task1 = new TryThisImpl(x - 12);
					TryThisImpl task2 = new TryThisImpl(x - 15);
					invokeAll(task1, task2);
					return task1.getRawResult() + task2.getRawResult();
				} else
					return tryThis_sequential(x - 1);
			}
		}
		public int tryThis_sequential(int x) {
			if (x < 0)
				return 0;
			else {
				if (x > 15)
					return tryThis_sequential(x - 12)
							+ tryThis_sequential(x - 15);
				else
					return tryThis_sequential(x - 1);
			}
		}
	}
}