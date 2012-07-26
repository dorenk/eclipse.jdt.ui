package object_in;

public class TestBlockCombination6 {
	
	public int tryThis(int x) {
		if (x < 0)
			return 0;
		else {
			if (x < 15)
				return method(1, tryThis(x - 12), tryThis(x - 15));
			else
				return method(tryThis(x - 1), tryThis(x - 2), tryThis(x - 3)) + tryThis(x - 4);
		}
	}
	private int method(int x, int y, int z) {
		return x + y + z;
	}
}