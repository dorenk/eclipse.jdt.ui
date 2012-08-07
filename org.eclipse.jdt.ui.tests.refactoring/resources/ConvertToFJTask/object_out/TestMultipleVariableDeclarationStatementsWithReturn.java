package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestMultipleVariableDeclarationStatementsWithReturn {
	
	public int coordinates(int num) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		CoordinatesImpl aCoordinatesImpl = new CoordinatesImpl(num);
		return pool.invoke(aCoordinatesImpl);
	}

	public class CoordinatesImpl extends RecursiveTask<Integer> {
		private int num;
		private CoordinatesImpl(int num) {
			this.num = num;
		}
		protected Integer compute() {
			if (num < 10) {
				return coordinates_sequential(num);
			} else {
				CoordinatesImpl task1 = new CoordinatesImpl(num - 1);
				CoordinatesImpl task2 = new CoordinatesImpl(num - 2);
				CoordinatesImpl task3 = new CoordinatesImpl(num - 3);
				invokeAll(task1, task2, task3);
				int x = task1.getRawResult();
				int y = task2.getRawResult();
				int z = task3.getRawResult();
				return x + y + z;
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