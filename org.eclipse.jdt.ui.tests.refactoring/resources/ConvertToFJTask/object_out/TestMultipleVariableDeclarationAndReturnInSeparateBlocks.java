package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestMultipleVariableDeclarationAndReturnInSeparateBlocks {

	public int distance(int vertex) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		DistanceTask aDistanceTask = new DistanceTask(vertex);
		return pool.invoke(aDistanceTask);
	}

	public class DistanceTask extends RecursiveTask<Integer> {
		private int vertex;
		private DistanceTask(int vertex) {
			this.vertex = vertex;
		}
		protected Integer compute() {
			if (vertex < 100) {
				return distance_sequential(vertex);
			} else {
				if (vertex < 20) {
					DistanceTask task1 = new DistanceTask(vertex / 3);
					DistanceTask task2 = new DistanceTask(vertex - 18);
					DistanceTask task3 = new DistanceTask(vertex / 2 - 5);
					invokeAll(task1, task2, task3);
					int third = task1.getRawResult();
					int origin = task2.getRawResult();
					int half = task3.getRawResult();
					return third + origin - half;
				} else {
					DistanceTask task4 = new DistanceTask(vertex - 50);
					DistanceTask task5 = new DistanceTask(vertex / 5);
					invokeAll(task4, task5);
					return task4.getRawResult() - task5.getRawResult();
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