package object_in;

public class TestForLoopNoBraces {
	
	public int method(int end) {
		if (end <= 0)
			return 1;
		else {
			int total= 0;
			for (int i= 0; i < 10; i++)
				total+= method(end - i) + method(end - i * 2);
			return total;
		}
	}
}