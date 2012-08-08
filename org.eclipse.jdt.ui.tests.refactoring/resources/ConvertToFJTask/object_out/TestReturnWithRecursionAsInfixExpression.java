package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestReturnWithRecursionAsInfixExpression {
	
	public int bar(int num) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		BarImpl aBarImpl = new BarImpl(num);
		return pool.invoke(aBarImpl);
	}

	public class BarImpl extends RecursiveTask<Integer> {
		private int num;
		private BarImpl(int num) {
			this.num = num;
		}
		protected Integer compute() {
			if (num < 10) {
				return bar_sequential(num);
			} else {
				BarImpl task1 = new BarImpl(num - 1);
				BarImpl task2 = new BarImpl(num - 2);
				BarImpl task3 = new BarImpl(num - 3);
				invokeAll(task1, task2, task3);
				return task1.getRawResult() + task2.getRawResult()
						+ task3.getRawResult();
			}
		}
		public int bar_sequential(int num) {
			if (num <= 0) {
				return 0;
			} else {
				return bar_sequential(num - 1) + bar_sequential(num - 2)
						+ bar_sequential(num - 3);
			}
		}
	}
}