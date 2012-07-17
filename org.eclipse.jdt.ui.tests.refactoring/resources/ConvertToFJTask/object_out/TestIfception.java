package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestIfception {
	
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
			} else if (end < 10) {
				if (end < 5) {
					MethodImpl task1 = new MethodImpl(end - 1);
					MethodImpl task2 = new MethodImpl(end - 2);
					invokeAll(task1, task2);
					result = task1.result + task2.result;
				} else {
					MethodImpl task3 = new MethodImpl(end - 5);
					MethodImpl task4 = new MethodImpl(end - 6);
					invokeAll(task3, task4);
					result = task3.result + task4.result;
				}
			} else {
				if (end > 25) {
					MethodImpl task5 = new MethodImpl(end - 12);
					MethodImpl task6 = new MethodImpl(end - 16);
					invokeAll(task5, task6);
					result = task5.result + task6.result;
				} else {
					MethodImpl task7 = new MethodImpl(end - 8);
					MethodImpl task8 = new MethodImpl(end - 10);
					invokeAll(task7, task8);
					result = task7.result + task8.result;
				}
			}
		}
		public int method(int end) {
			if (end <= 0) {
				return 1;
			} else if (end < 10) {
				if (end < 5)
					return method(end - 1) + method(end - 2);
				else
					return method(end - 5) + method(end - 6);
			} else {
				if (end > 25)
					return method(end - 12) + method(end - 16);
				else
					return method(end - 8) + method(end - 10);
			}
		}
	}
}