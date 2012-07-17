package object_in;

public class TestMultiple0 {

	public int test0(int x) {
		if (x < 3) {
			return -1;
		} else {
			if (x < 20) {
				int x1 = test0(x / 3);
				int x2 = test0(x - 18);
				int x3 = test0(x / 2 - 5);
				return x1 + x2 - x3;
			} else {
				return test0(x - 50) - test0(x / 5);
			}
		}
	}
}