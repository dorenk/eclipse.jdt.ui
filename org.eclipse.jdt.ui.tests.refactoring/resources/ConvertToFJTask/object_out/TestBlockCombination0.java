package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestBlockCombination0 {
	
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
					result = task1.result + task2.result;
				} else {
					result = tryThis(x - 1);
				}
			}
		}
		public int tryThis(int x) {
			if (x < 0)
				return 0;
			else {
				if (x > 15)
					return tryThis(x - 12) + tryThis(x - 15);
				else
					return tryThis(x - 1);
			}
		}
	}
}