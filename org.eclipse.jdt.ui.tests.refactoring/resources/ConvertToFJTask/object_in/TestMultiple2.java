package object_in;

public class TestMultiple2 {

	public int test2(int x) {
		if (x <= 0) {
			return 1;
		} else {
			if (x > 10)
				return test2(x - 3) + test2(x - 4);
			else
				return test2(x - 1) + test2(x - 2);
		}
	}
}