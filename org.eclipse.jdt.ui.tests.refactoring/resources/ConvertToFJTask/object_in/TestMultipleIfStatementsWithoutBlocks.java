package object_in;

public class TestMultipleIfStatementsWithoutBlocks {
	
	public int foo(int end) {
		if (end <= 0) {
				return 1;
		} else if (end < 10){
			if (end < 5)
				return foo(end - 1) + foo(end - 2);
			else
				return foo(end - 5) + foo(end - 6);
		} else {
			if (end > 25)
				return foo(end - 12) + foo(end - 16);
			else
				return foo(end - 8) + foo(end - 10);
		}
	}
}