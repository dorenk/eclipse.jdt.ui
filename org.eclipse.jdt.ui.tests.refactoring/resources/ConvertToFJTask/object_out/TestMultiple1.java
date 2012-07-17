package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestMultiple1 {

	public int test1(int x) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		Test1Impl aTest1Impl = new Test1Impl(x);
		pool.invoke(aTest1Impl);
		return aTest1Impl.result;
	}
	public class Test1Impl extends RecursiveAction {
		private int x;
		private int result;
		private Test1Impl(int x) {
			this.x = x;
		}
		protected void compute() {
			if (x < 10) {
				result = test1(x);
				return;
			} else {
				if (x < 5) {
					Test1Impl task1 = new Test1Impl(x - 1);
					Test1Impl task2 = new Test1Impl(x - 2);
					invokeAll(task1, task2);
					int x1 = task1.result;
					int x2 = task2.result;
					result = x1 * x2;
				} else {
					Test1Impl task3 = new Test1Impl(x - 4);
					Test1Impl task4 = new Test1Impl(x - 3);
					invokeAll(task3, task4);
					result = method1(task3.result, task4.result);
				}
			}
		}
		public int test1(int x) {
			if (x <= 0) {
				return x;
			} else {
				if (x < 5) {
					int x1 = test1(x - 1);
					int x2 = test1(x - 2);
					return x1 * x2;
				} else {
					return method1(test1(x - 4), test1(x - 3));
				}
			}
		}
	}
	private int method1(int x, int y) {
		return x + y;
	}
}