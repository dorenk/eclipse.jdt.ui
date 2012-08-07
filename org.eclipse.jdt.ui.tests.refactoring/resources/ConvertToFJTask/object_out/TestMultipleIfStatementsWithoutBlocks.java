package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestMultipleIfStatementsWithoutBlocks {
	
	public int foo(int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		FooImpl aFooImpl = new FooImpl(end);
		return pool.invoke(aFooImpl);
	}

	public class FooImpl extends RecursiveTask<Integer> {
		private int end;
		private FooImpl(int end) {
			this.end = end;
		}
		protected Integer compute() {
			if (end < 10) {
				return foo_sequential(end);
			} else if (end < 10) {
				if (end < 5) {
					FooImpl task1 = new FooImpl(end - 1);
					FooImpl task2 = new FooImpl(end - 2);
					invokeAll(task1, task2);
					return task1.getRawResult() + task2.getRawResult();
				} else {
					FooImpl task3 = new FooImpl(end - 5);
					FooImpl task4 = new FooImpl(end - 6);
					invokeAll(task3, task4);
					return task3.getRawResult() + task4.getRawResult();
				}
			} else {
				if (end > 25) {
					FooImpl task5 = new FooImpl(end - 12);
					FooImpl task6 = new FooImpl(end - 16);
					invokeAll(task5, task6);
					return task5.getRawResult() + task6.getRawResult();
				} else {
					FooImpl task7 = new FooImpl(end - 8);
					FooImpl task8 = new FooImpl(end - 10);
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