package object_in;

public class TestBlockCombination9 {
	
	int x9= 0;
	public int tryThis(int x) {
		if (x < 0)
			return 0;
		else
			return x9= tryThis(x - 1) + tryThis(x - 2);
	}
}