package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestNoBracesMultiple {
	
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
				MethodImpl task1 = new MethodImpl(end - 1);
				MethodImpl task2 = new MethodImpl(end - 2);
				MethodImpl task3 = new MethodImpl(end - 3);
				invokeAll(task1, task2, task3);
				return otherMethod(task1.getRawResult(), task2.getRawResult(),
						task3.getRawResult());
			}
		}
		public int method_sequential(int end) {
			if (end <= 0)
				return 1;
			else
				return otherMethod(method_sequential(end - 1),
						method_sequential(end - 2), method_sequential(end - 3));
		}
	}
	public int otherMethod(int x, int y, int z) {
		return x + y + z;
	}
}