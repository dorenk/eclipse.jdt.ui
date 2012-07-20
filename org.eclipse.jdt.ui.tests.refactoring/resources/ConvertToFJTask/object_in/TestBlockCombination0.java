package object_in;

public class TestBlockCombination0 {
	
	public int tryThis(int x) {
		if (x < 0)
			return 0;
		else {
			if (x > 15)
				return tryThis(x - 12) + tryThis(x - 15);
			else
				return tryThis(x - 1);
		}
	}
}