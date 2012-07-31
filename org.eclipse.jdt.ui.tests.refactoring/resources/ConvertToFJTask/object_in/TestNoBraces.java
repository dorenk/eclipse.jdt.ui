package object_in;

public class TestNoBraces {
	
	public int method(int end) {
		if (end <= 0)
			return 1;
		else
			return method(end - 1) + method(end - 2);
	}
}