package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestBlockCombination6 {
	
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
				if (x < 15) {
					TryThisTask task1 = new TryThisTask(x - 12);
					TryThisTask task2 = new TryThisTask(x - 15);
					invokeAll(task1, task2);
					return method(1, task1.getRawResult(), task2.getRawResult());
				} else {
					TryThisTask task3 = new TryThisTask(x - 1);
					TryThisTask task4 = new TryThisTask(x - 2);
					TryThisTask task5 = new TryThisTask(x - 3);
					TryThisTask task6 = new TryThisTask(x - 4);
					invokeAll(task3, task4, task5, task6);
					return method(task3.getRawResult(), task4.getRawResult(),
							task5.getRawResult()) + task6.getRawResult();
				}
			}
		}
		public int tryThis_sequential(int x) {
			if (x < 0)
				return 0;
			else {
				if (x < 15)
					return method(1, tryThis_sequential(x - 12),
							tryThis_sequential(x - 15));
				else
					return method(tryThis_sequential(x - 1),
							tryThis_sequential(x - 2),
							tryThis_sequential(x - 3))
							+ tryThis_sequential(x - 4);
			}
		}
	}
	private int method(int x, int y, int z) {
		return x + y + z;
	}
}