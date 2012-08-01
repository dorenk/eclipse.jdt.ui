package object_in;

public class TestElseStatementWithoutBlock {
	
	public int count(int end) {
		if (end <= 0)
			return 1;
		else
			return count(end - 1) + count(end - 2);
	}
}