package object_in;

public class TestIfception {
	
	public int method(int end) {
		if (end <= 0) {
				return 1;
		} else if (end < 10){
			if (end < 5)
				return method(end - 1) + method(end - 2);
			else
				return method(end - 5) + method(end - 6);
		} else {
			if (end > 25)
				return method(end - 12) + method(end - 16);
			else
				return method(end - 8) + method(end - 10);
		}
	}
}