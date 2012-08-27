package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestMultipleIfStatementsWithoutBlocks {
	
	public int foo(int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		FooTask aFooTask = new FooTask(end);
		return pool.invoke(aFooTask);
	}

	public class FooTask extends RecursiveTask<Integer> {
		private int end;
		private FooTask(int end) {
			this.end = end;
		}
		protected Integer compute() {
			if (end < 10) {
				return foo_sequential(end);
			} else if (end < 10) {
				if (end < 5) {
					FooTask task1 = new FooTask(end - 1);
					FooTask task2 = new FooTask(end - 2);
					invokeAll(task1, task2);
					return task1.getRawResult() + task2.getRawResult();
				} else {
					FooTask task3 = new FooTask(end - 5);
					FooTask task4 = new FooTask(end - 6);
					invokeAll(task3, task4);
					return task3.getRawResult() + task4.getRawResult();
				}
			} else {
				if (end > 25) {
					FooTask task5 = new FooTask(end - 12);
					FooTask task6 = new FooTask(end - 16);
					invokeAll(task5, task6);
					return task5.getRawResult() + task6.getRawResult();
				} else {
					FooTask task7 = new FooTask(end - 8);
					FooTask task8 = new FooTask(end - 10);
					invokeAll(task7, task8);
					return task7.getRawResult() + task8.getRawResult();
				}
			}
		}
		public int foo_sequential(int end) {
			if (end <= 0) {
				return 1;
			} else if (end < 10) {
				if (end < 5)
					return foo_sequential(end - 1) + foo_sequential(end - 2);
				else
					return foo_sequential(end - 5) + foo_sequential(end - 6);
			} else {
				if (end > 25)
					return foo_sequential(end - 12) + foo_sequential(end - 16);
				else
					return foo_sequential(end - 8) + foo_sequential(end - 10);
			}
		}
	}
}