package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestBlockCombination9 {
	
	int x9= 0;
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
				TryThisImpl task1 = new TryThisImpl(x - 1);
				TryThisImpl task2 = new TryThisImpl(x - 2);
				invokeAll(task1, task2);
				result = x9 = task1.result + task2.result;
			}
		}
		public int tryThis(int x) {
			if (x < 0)
				return 0;
			else
				return x9 = tryThis(x - 1) + tryThis(x - 2);
		}
	}
}