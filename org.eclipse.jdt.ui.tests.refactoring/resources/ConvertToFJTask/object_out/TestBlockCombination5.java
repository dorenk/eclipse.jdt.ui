package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestBlockCombination5 {
	
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
					TryThisImpl task3 = new TryThisImpl(x - 3);
					invokeAll(task1, task2, task3);
					int x12 = task1.result + task2.result;
					int x3 = task3.result;
					result = x12 + x3;
				} else {
					result = 1 + tryThis(x - 1);
				}
			}
		}
		public int tryThis(int x) {
			if (x < 0)
				return 0;
			else {
				if (x < 15) {
					int x12 = tryThis(x - 12) + tryThis(x - 15);
					int x3 = tryThis(x - 3);
					return x12 + x3;
				} else
					return 1 + tryThis(x - 1);
			}
		}
	}
}