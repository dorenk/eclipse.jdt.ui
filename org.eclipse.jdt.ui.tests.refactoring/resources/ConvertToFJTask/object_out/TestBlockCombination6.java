package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestBlockCombination6 {
	
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
				if (x < 15) {
					TryThisImpl task1 = new TryThisImpl(x - 12);
					TryThisImpl task2 = new TryThisImpl(x - 15);
					invokeAll(task1, task2);
					return method(1, task1.getRawResult(), task2.getRawResult());
				} else {
					TryThisImpl task3 = new TryThisImpl(x - 1);
					TryThisImpl task4 = new TryThisImpl(x - 2);
					TryThisImpl task5 = new TryThisImpl(x - 3);
					TryThisImpl task6 = new TryThisImpl(x - 4);
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