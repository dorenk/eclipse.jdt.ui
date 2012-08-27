package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestMultipleExpressionStatementAndReturnWithMethodInvocationInSeparateBlocks {

	public int calculateMiles(int data) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		CalculateMilesTask aCalculateMilesTask = new CalculateMilesTask(data);
		return pool.invoke(aCalculateMilesTask);
	}
	public class CalculateMilesTask extends RecursiveTask<Integer> {
		private int data;
		private CalculateMilesTask(int data) {
			this.data = data;
		}
		protected Integer compute() {
			if (data < 100) {
				return calculateMiles_sequential(data);
			} else {
				if (data < 5) {
					int gas;
					CalculateMilesTask task1 = new CalculateMilesTask(data - 1);
					CalculateMilesTask task2 = new CalculateMilesTask(data - 2);
					invokeAll(task1, task2);
					gas = task1.getRawResult();
					int mpg;
					mpg = task2.getRawResult();
					return gas * mpg;
				} else {
					CalculateMilesTask task3 = new CalculateMilesTask(data - 4);
					CalculateMilesTask task4 = new CalculateMilesTask(data - 3);
					invokeAll(task3, task4);
					return electric(task3.getRawResult(), task4.getRawResult());
				}
			}
		}
		public int calculateMiles_sequential(int data) {
			if (data <= 0) {
				return data;
			} else {
				if (data < 5) {
					int gas;
					gas = calculateMiles_sequential(data - 1);
					int mpg;
					mpg = calculateMiles_sequential(data - 2);
					return gas * mpg;
				} else {
					return electric(calculateMiles_sequential(data - 4),
							calculateMiles_sequential(data - 3));
				}
			}
		}
	}
	private int electric(int power, int capacity) {
		return power + capacity;
	}
}