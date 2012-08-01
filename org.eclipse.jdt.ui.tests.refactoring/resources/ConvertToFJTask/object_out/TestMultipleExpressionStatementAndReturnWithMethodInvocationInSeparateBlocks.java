package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestMultipleExpressionStatementAndReturnWithMethodInvocationInSeparateBlocks {

	public int calculateMiles(int data) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		CalculateMilesImpl aCalculateMilesImpl = new CalculateMilesImpl(data);
		pool.invoke(aCalculateMilesImpl);
		return aCalculateMilesImpl.result;
	}
	public class CalculateMilesImpl extends RecursiveAction {
		private int data;
		private int result;
		private CalculateMilesImpl(int data) {
			this.data = data;
		}
		protected void compute() {
			if (data < 100) {
				result = calculateMiles_sequential(data);
				return;
			} else {
				if (data < 5) {
					int gas;
					CalculateMilesImpl task1 = new CalculateMilesImpl(data - 1);
					int mpg;
					CalculateMilesImpl task2 = new CalculateMilesImpl(data - 2);
					invokeAll(task1, task2);
					gas = task1.result;
					mpg = task2.result;
					result = gas * mpg;
				} else {
					CalculateMilesImpl task3 = new CalculateMilesImpl(data - 4);
					CalculateMilesImpl task4 = new CalculateMilesImpl(data - 3);
					invokeAll(task3, task4);
					result = electric(task3.result, task4.result);
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