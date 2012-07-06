package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestNoBracesMultiple {
	
	public int method(int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		MethodImpl aMethodImpl = new MethodImpl(end);
		pool.invoke(aMethodImpl);
		return aMethodImpl.result;
	}
	public class MethodImpl extends RecursiveAction {
		private int end;
		private int result;
		private MethodImpl(int end) {
			this.end = end;
		}
		protected void compute() {
			if (end < 10) {
				result = method(end);
				return;
			} else {
				MethodImpl task1 = new MethodImpl(end - 1);
				MethodImpl task2 = new MethodImpl(end - 2);
				MethodImpl task3 = new MethodImpl(end - 3);
				invokeAll(task1, task2, task3);
				result = otherMethod(task1.result, task2.result, task3.result);
			}
		}
		public int method(int end) {
			if (end <= 0)
				return 1;
			else
				return otherMethod(method(end - 1), method(end - 2),
						method(end - 3));
		}
	}
	public int otherMethod(int x, int y, int z) {
		return x + y + z;
	}
}