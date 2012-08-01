package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestMultiple0 {

	public int test0(int x) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		Test0Impl aTest0Impl = new Test0Impl(x);
		pool.invoke(aTest0Impl);
		return aTest0Impl.result;
	}

	public class Test0Impl extends RecursiveAction {
		private int x;
		private int result;
		private Test0Impl(int x) {
			this.x = x;
		}
		protected void compute() {
			if (x < 10) {
				result = test0(x);
				return;
			} else {
				if (x < 20) {
					Test0Impl task1 = new Test0Impl(x / 3);
					Test0Impl task2 = new Test0Impl(x - 18);
					Test0Impl task3 = new Test0Impl(x / 2 - 5);
					invokeAll(task1, task2, task3);
					int x1 = task1.result;
					int x2 = task2.result;
					int x3 = task3.result;
					result = x1 + x2 - x3;
				} else {
					Test0Impl task4 = new Test0Impl(x - 50);
					Test0Impl task5 = new Test0Impl(x / 5);
					invokeAll(task4, task5);
					result = task4.result - task5.result;
				}
			}
		}
		public int test0(int x) {
			if (x < 3) {
				return -1;
			} else {
				if (x < 20) {
					int x1 = test0(x / 3);
					int x2 = test0(x - 18);
					int x3 = test0(x / 2 - 5);
					return x1 + x2 - x3;
				} else {
					return test0(x - 50) - test0(x / 5);
				}
			}
		}
	}
}