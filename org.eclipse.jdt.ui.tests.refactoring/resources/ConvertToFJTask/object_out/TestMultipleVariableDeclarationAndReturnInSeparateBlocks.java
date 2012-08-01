package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestMultipleVariableDeclarationAndReturnInSeparateBlocks {

	public int distance(int vertex) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		DistanceImpl aDistanceImpl = new DistanceImpl(vertex);
		pool.invoke(aDistanceImpl);
		return aDistanceImpl.result;
	}

	public class DistanceImpl extends RecursiveAction {
		private int vertex;
		private int result;
		private DistanceImpl(int vertex) {
			this.vertex = vertex;
		}
		protected void compute() {
			if (vertex < 100) {
				result = distance_sequential(vertex);
				return;
			} else {
				if (vertex < 20) {
					DistanceImpl task1 = new DistanceImpl(vertex / 3);
					DistanceImpl task2 = new DistanceImpl(vertex - 18);
					DistanceImpl task3 = new DistanceImpl(vertex / 2 - 5);
					invokeAll(task1, task2, task3);
					int third = task1.result;
					int origin = task2.result;
					int half = task3.result;
					result = third + origin - half;
				} else {
					DistanceImpl task4 = new DistanceImpl(vertex - 50);
					DistanceImpl task5 = new DistanceImpl(vertex / 5);
					invokeAll(task4, task5);
					result = task4.result - task5.result;
				}
			}
		}
		public int distance_sequential(int vertex) {
			if (vertex < 3) {
				return -1;
			} else {
				if (vertex < 20) {
					int third = distance_sequential(vertex / 3);
					int origin = distance_sequential(vertex - 18);
					int half = distance_sequential(vertex / 2 - 5);
					return third + origin - half;
				} else {
					return distance_sequential(vertex - 50)
							- distance_sequential(vertex / 5);
				}
			}
		}
	}
}