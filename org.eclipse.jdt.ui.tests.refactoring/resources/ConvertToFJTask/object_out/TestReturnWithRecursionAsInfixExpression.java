package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestReturnWithRecursionAsInfixExpression {
	
	public int bar(int num) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		BarImpl aBarImpl = new BarImpl(num);
		pool.invoke(aBarImpl);
		return aBarImpl.result;
	}

	public class BarImpl extends RecursiveAction {
		private int num;
		private int result;
		private BarImpl(int num) {
			this.num = num;
		}
		protected void compute() {
			if (num < 10) {
				result = bar_sequential(num);
				return;
			} else {
				BarImpl task1 = new BarImpl(num - 1);
				BarImpl task2 = new BarImpl(num - 2);
				BarImpl task3 = new BarImpl(num - 3);
				invokeAll(task1, task2, task3);
				result = task1.result + task2.result + task3.result;
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