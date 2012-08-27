package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestMethodMultipleTasks {
	
	public int method(int num) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		MethodTask aMethodTask = new MethodTask(num);
		pool.invoke(aMethodTask);
		return aMethodTask.result;
	}
	public class MethodTask extends RecursiveAction {
		private int num;
		private int result;
		private MethodTask(int num) {
			this.num = num;
		}
		protected void compute() {
			if (num < 10) {
				result = method(num);
				return;
			} else {
				MethodTask task1 = new MethodTask(num - 1);
				MethodTask task2 = new MethodTask(num - 2);
				MethodTask task3 = new MethodTask(num - 3);
				invokeAll(task1, task2, task3);
				result = sum(task1.result, task2.result, task3.result);
			}
		}
		public int method(int num) {
			if (num <= 0) {
				return 0;
			} else {
				return sum(method(num - 1), method(num - 2), method(num - 3));
			}
		}
	}
	public int sum(int a, int b, int c) {
		return a + b + c;
	}
}