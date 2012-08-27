package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TestSequentialMergeSort {

	private int[] listToSort;

	/**
	 * Construct a new MergeSort object that will
	 * sort the specified array of integers.
	 *
	 * @param listToSort the array of integers to be sorted.
	 */
	public TestSequentialMergeSort(int[] listToSort) {
		this.listToSort = listToSort;
	}

	/**
	 * Get a reference to the array of integers in this
	 * MergeSort object.
	 *
	 * @return a reference to the array of integers.
	 */
	public int[] getList() {
		return listToSort;
	}

	/**
	 * Recursive helper method which sorts the array referred to 
	 * by whole using the merge sort algorithm.
	 *
	 * @param whole the array to be sorted.
	 * @return a reference to an array that holds the elements
	 *         of whole sorted into non-decreasing order.
	 */
	private int[] sort(int[] whole) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		SortTask aSortTask = new SortTask(whole);
		return pool.invoke(aSortTask);
	}

	private class SortTask extends RecursiveTask<int[]> {
		private int[] whole;
		private SortTask(int[] whole) {
			this.whole = whole;
		}
		protected int[] compute() {
			if (whole.length < 10) {
				return sort_sequential(whole);
			} else {
				int[] left = new int[whole.length / 2];
				System.arraycopy(whole, 0, left, 0, left.length);
				int[] right = new int[whole.length - left.length];
				System.arraycopy(whole, left.length, right, 0, right.length);
				SortTask task1 = new SortTask(left);
				SortTask task2 = new SortTask(right);
				invokeAll(task1, task2);
				left = task1.getRawResult();
				right = task2.getRawResult();
				merge(left, right, whole);
				return whole;
			}
		}
		private int[] sort_sequential(int[] whole) {
			if (whole.length == 1) {
				return whole;
			} else {
				int[] left = new int[whole.length / 2];
				System.arraycopy(whole, 0, left, 0, left.length);
				int[] right = new int[whole.length - left.length];
				System.arraycopy(whole, left.length, right, 0, right.length);
				left = sort_sequential(left);
				right = sort_sequential(right);
				merge(left, right, whole);
				return whole;
			}
		}
	}

	/**
	 * Merge the two sorted arrays left and right into the
	 * array whole.
	 *
	 * @param left a sorted array.
	 * @param right a sorted array.
	 * @param whole the array to hold the merged left and right arrays.
	 */
	private void merge(int[] left, int[] right, int[] whole) {
		int leftIndex = 0;
		int rightIndex = 0;
		int wholeIndex = 0;

		// As long as neither the left nor the right array has
		// been used up, keep taking the smaller of left[leftIndex]
		// or right[rightIndex] and adding it at both[bothIndex].
		while (leftIndex < left.length &&
				rightIndex < right.length) {
			if (left[leftIndex] < right[rightIndex]) {
				whole[wholeIndex] = left[leftIndex];
				leftIndex++;
			}
			else {
				whole[wholeIndex] = right[rightIndex];
				rightIndex++;
			}
			wholeIndex++;
		}

		int[] rest;
		int restIndex;
		if (leftIndex >= left.length) {
			// The left array has been use up...
			rest = right;
			restIndex = rightIndex;
		}
		else {
			// The right array has been used up...
			rest = left;
			restIndex = leftIndex;
		}

		// Copy the rest of whichever array (left or right) was
		// not used up.
		for (int i=restIndex; i<rest.length; i++) {
			whole[wholeIndex] = rest[i];
			wholeIndex++;
		}
	}

	/**
	 * Sort the values in the array of integers in this
	 * MergeSort object into non-decreasing order.
	 */
	public void sort() {
		listToSort = sort(listToSort);
	}

	public static void main(String[] args) {

		int[] arrayToSort = ArrayUtil.randomArray(25,50);

		System.out.println("Unsorted:");
		ArrayUtil.printArray(arrayToSort, 5);

		TestSequentialMergeSort sortObj = new TestSequentialMergeSort(arrayToSort);
		sortObj.sort();

		System.out.println("Sorted:");
		ArrayUtil.printArray(sortObj.getList(), 5);
	}
}