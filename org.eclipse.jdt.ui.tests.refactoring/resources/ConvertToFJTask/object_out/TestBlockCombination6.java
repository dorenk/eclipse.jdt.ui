package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestBlockCombination6 {
	
	public int tryThis(int x) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		TryThisImpl aTryThisImpl = new TryThisImpl(x);
		pool.invoke(aTryThisImpl);
		return aTryThisImpl.result;
	}
	public class TryThisImpl extends RecursiveAction {
		private int x;
		private int result;
		private TryThisImpl(int x) {
			this.x = x;
		}
		protected void compute() {
			if (x < 10) {
				result = tryThis(x);
				return;
			} else {
				if (x < 15) {
					TryThisImpl task1 = new TryThisImpl(x - 12);
					TryThisImpl task2 = new TryThisImpl(x - 15);
					invokeAll(task1, task2);
					result = method(1, task1.result, task2.result);
				} else {
					TryThisImpl task3 = new TryThisImpl(x - 1);
					TryThisImpl task4 = new TryThisImpl(x - 2);
					TryThisImpl task5 = new TryThisImpl(x - 3);
					TryThisImpl task6 = new TryThisImpl(x - 4);
					invokeAll(task3, task4, task5, task6);
					result = method(task3.result, task4.result, task5.result)
							+ task6.result;
				}
			}
		}
		public int tryThis(int x) {
			if (x < 0)
				return 0;
			else {
				if (x < 15)
					return method(1, tryThis(x - 12), tryThis(x - 15));
				else
					return method(tryThis(x - 1), tryThis(x - 2),
							tryThis(x - 3)) + tryThis(x - 4);
			}
		}
	}
	private int method(int x, int y, int z) {
		return x + y + z;
	}
}