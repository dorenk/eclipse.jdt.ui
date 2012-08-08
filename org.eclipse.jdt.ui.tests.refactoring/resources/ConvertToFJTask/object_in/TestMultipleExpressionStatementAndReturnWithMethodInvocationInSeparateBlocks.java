package object_in;

public class TestMultipleExpressionStatementAndReturnWithMethodInvocationInSeparateBlocks {

	public int calculateMiles(int data) {
		if (data <= 0) {
			return data;
		} else {
			if (data < 5) {
				int gas; 
				gas= calculateMiles(data - 1);
				int mpg;
				mpg= calculateMiles(data - 2);
				return gas * mpg;
			} else {
				return electric(calculateMiles(data - 4), calculateMiles(data - 3));
			}
		}
	}
	private int electric(int power, int capacity) {
		return power + capacity;
	}
}