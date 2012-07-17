package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestMultiple2 {

	public int test2(int x) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		Test2Impl aTest2Impl = new Test2Impl(x);
		pool.invoke(aTest2Impl);
		return aTest2Impl.result;
	}

	public class Test2Impl extends RecursiveAction {
		private int x;
		private int result;
		private Test2Impl(int x) {
			this.x = x;
		}
		protected void compute() {
			if (x < 10) {
				result = test2(x);
				return;
			} else {
				if (x > 10) {
					Test2Impl task1 = new Test2Impl(x - 3);
					Test2Impl task2 = new Test2Impl(x - 4);
					invokeAll(task1, task2);
					result = task1.result + task2.result;
				} else {
					Test2Impl task3 = new Test2Impl(x - 1);
					Test2Impl task4 = new Test2Impl(x - 2);
					invokeAll(task3, task4);
					result = task3.result + task4.result;
				}
			}
		}
		public int test2(int x) {
			if (x <= 0) {
				return 1;
			} else {
				if (x > 10)
					return test2(x - 3) + test2(x - 4);
				else
					return test2(x - 1) + test2(x - 2);
			}
		}
	}
}