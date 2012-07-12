package invalid;

public class TestMethodDoesNotHaveRecursion {
	
	public int method(int[] array, int x) {
		if (array.length == 0)
			return 0;
		
		if (x == 7)
			return -3;
		
		return method(array, x - 1);
	}
}