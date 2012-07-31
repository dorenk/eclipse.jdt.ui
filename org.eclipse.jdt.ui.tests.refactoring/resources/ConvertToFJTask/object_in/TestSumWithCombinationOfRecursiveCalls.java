package object_in;

public class TestSumWithCombinationOfRecursiveCalls {
	
	public int recursionSumWithCombinationOfRecursiveCalls(int end) {
		if (end <= 0) {
			return 0;
		} else {
			int i = recursionSumWithCombinationOfRecursiveCalls(end - 1);
			return sumCombination(i, recursionSumWithCombinationOfRecursiveCalls(end - 2));
		}
	}
	public int sumCombination(int a, int b) {
		return a + b;
	}
}