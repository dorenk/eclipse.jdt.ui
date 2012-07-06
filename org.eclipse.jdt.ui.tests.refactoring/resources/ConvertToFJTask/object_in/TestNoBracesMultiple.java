package object_in;

public class TestNoBracesMultiple {
	
	public int method(int end) {
		if (end <= 0)
			return 1;
		else
			return otherMethod(method(end - 1), method(end - 2), method(end - 3));
	}
	public int otherMethod(int x, int y, int z) {
		return x + y + z;
	}
}