package object_in;

public class TestReturnWithRecursionAsInfixExpression {
	
	public int bar(int num) {
		if (num <= 0) {
			return 0;
		} else {
			return bar(num - 1) + bar(num - 2) + bar(num - 3);
		}
	}
}