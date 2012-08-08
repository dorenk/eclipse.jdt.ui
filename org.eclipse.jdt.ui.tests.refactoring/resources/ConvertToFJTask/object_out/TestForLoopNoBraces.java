package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestForLoopNoBraces {
	
	public int method(int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		MethodImpl aMethodImpl = new MethodImpl(end);
		return pool.invoke(aMethodImpl);
	}

	public class MethodImpl extends RecursiveTask<Integer> {
		private int end;
		private MethodImpl(int end) {
			this.end = end;
		}
		protected Integer compute() {
			if (end < 10) {
				return method_sequential(end);
			} else {
				int total = 0;
				for (int i = 0; i < 10; i++) {
					MethodImpl task1 = new MethodImpl(end - i);
					MethodImpl task2 = new MethodImpl(end - i * 2);
					invokeAll(task1, task2);
					total += task1.getRawResult() + task2.getRawResult();
				}
				return total;
			}
		}
		public int method_sequential(int end) {
			if (end <= 0)
				return 1;
			else {
				int total = 0;
				for (int i = 0; i < 10; i++)
					total += method_sequential(end - i)
							+ method_sequential(end - i * 2);
				return total;
			}
		}
	}
}