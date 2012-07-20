package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestBlockCombination1 {
	
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
				if (x > 15) {
					TryThisImpl task1 = new TryThisImpl(x - 12);
					TryThisImpl task2 = new TryThisImpl(x - 15);
					invokeAll(task1, task2);
					int x1 = task1.result;
					int x2 = task2.result;
					result = x1 + x2;
				} else {
					int x3 = tryThis(x - 1);
					result = x3;
					return x3;
				}
			}
		}
		public int tryThis(int x) {
			if (x < 0)
				return 0;
			else {
				if (x > 15) {
					int x1 = tryThis(x - 12);
					int x2 = tryThis(x - 15);
					return x1 + x2;
				} else {
					int x3 = tryThis(x - 1);
					return x3;
				}
			}
		}
	}
}