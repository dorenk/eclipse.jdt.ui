package object_in;

public class TestMultiple1 {

	public int test1(int x) {
		if (x <= 0) {
			return x;
		} else {
			if (x < 5) {
				int x1 = test1(x - 1);
				int x2 = test1(x - 2);
				return x1 * x2;
			} else {
				return method1(test1(x - 4), test1(x - 3));
			}
		}
	}
	private int method1(int x, int y) {
		return x + y;
	}
}