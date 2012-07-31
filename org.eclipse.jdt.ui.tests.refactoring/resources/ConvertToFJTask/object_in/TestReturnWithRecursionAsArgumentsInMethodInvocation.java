package object_in;

public class TestReturnWithRecursionAsArgumentsInMethodInvocation {
	
	public int recursion(int end) {
		if (end <= 0) {
			return 0;
		} else {
			return sum(recursion(end - 1), recursion(end - 2));
		}
	}
	public int sum(int a, int b) {
		return a + b;
	}
}