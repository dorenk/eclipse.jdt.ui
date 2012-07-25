package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestForLoopNoBraces {
	
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
				int total= 0;
				for (int i = 0; i < 10; i++) {
					MethodImpl task1 = new MethodImpl(end - i);
					MethodImpl task2 = new MethodImpl(end - i * 2);
					invokeAll(task1, task2);
					total += task1.result + task2.result;
				}
				result = total;
			}
		}
		public int method(int end) {
			if (end <= 0)
				return 1;
			else {
				int total= 0;
				for (int i= 0; i < 10; i ++)
					total+= method(end - i) + method(end - i * 2);
				return total;
			}
		}
	}
}