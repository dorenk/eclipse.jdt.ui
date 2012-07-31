package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestMultipleVariableDeclarationStatementsWithReturn {
	
	public int coordinates(int num) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		CoordinatesImpl aCoordinatesImpl = new CoordinatesImpl(num);
		pool.invoke(aCoordinatesImpl);
		return aCoordinatesImpl.result;
	}

	public class CoordinatesImpl extends RecursiveAction {
		private int num;
		private int result;
		private CoordinatesImpl(int num) {
			this.num = num;
		}
		protected void compute() {
			if (num < 10) {
				result = coordinates_sequential(num);
				return;
			} else {
				CoordinatesImpl task1 = new CoordinatesImpl(num - 1);
				CoordinatesImpl task2 = new CoordinatesImpl(num - 2);
				CoordinatesImpl task3 = new CoordinatesImpl(num - 3);
				invokeAll(task1, task2, task3);
				int x = task1.result;
				int y = task2.result;
				int z = task3.result;
				result = x + y + z;
			}
		}
		public int coordinates_sequential(int num) {
			if (num <= 0) {
				return 0;
			} else {
				int x = coordinates_sequential(num - 1);
				int y = coordinates_sequential(num - 2);
				int z = coordinates_sequential(num - 3);
				return x + y + z;
			}
		}
	}
}