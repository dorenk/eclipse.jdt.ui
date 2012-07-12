package object_in;

public class TestIfception {
	
	public int method(int end) {
		if (end <= 0) {
			if(end < 0)
				return 1;
			else
				return 2;
		} else {
			if (end > 10)
				return method(end - 3) + method(end - 4);
			else
				return method(end - 1) + method(end - 1);
		}
	}
}
