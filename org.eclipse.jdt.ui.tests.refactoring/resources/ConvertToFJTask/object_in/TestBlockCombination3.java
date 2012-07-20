package object_in;

public class TestBlockCombination3 {
	
	public int tryThis(int x) {
		if (x < 0)
			return 0;
		else {
			if (x < 15) {
				int x12= tryThis(x - 12) + tryThis(x - 15);
				return x12;
			} else
				return 1 + tryThis(x - 1);
		}
	}
}