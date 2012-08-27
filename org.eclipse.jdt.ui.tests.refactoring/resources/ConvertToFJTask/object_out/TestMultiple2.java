package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestMultiple2 {

	public int test2(int x) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		Test2Task aTest2Task = new Test2Task(x);
		pool.invoke(aTest2Task);
		return aTest2Task.result;
	}

	public class Test2Task extends RecursiveAction {
		private int x;
		private int result;
		private Test2Task(int x) {
			this.x = x;
		}
		protected void compute() {
			if (x < 10) {
				result = test2(x);
				return;
			} else {
				if (x > 10) {
					Test2Task task1 = new Test2Task(x - 3);
					Test2Task task2 = new Test2Task(x - 4);
					invokeAll(task1, task2);
					result = task1.result + task2.result;
				} else {
					Test2Task task3 = new Test2Task(x - 1);
					Test2Task task4 = new Test2Task(x - 2);
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