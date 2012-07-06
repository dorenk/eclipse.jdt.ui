package invalid;

public class TestMethodDoesNotHaveRecursion {
	
	public int method(int[] array) {
		if (array.length == 0) {
			array = null;
			return 0;
		}
		else {
			return otherMethod(array);
		}
	}
	
	public int otherMethod(int[] array) {
		return array[0];
	}
}