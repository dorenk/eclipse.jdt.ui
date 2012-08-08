package object_in;

public class TestFibonacciWithCombinationOfRecursiveCalls {
	
	public int fibonacciWithCombinationOfRecursiveCalls(int end) {
		if (end < 2) {
			return end;
		}
		else {
			int i = fibonacciWithCombinationOfRecursiveCalls(end - 1);
			return i + fibonacciWithCombinationOfRecursiveCalls(end - 2);
		}
	}
}