package object_in;

public class TestBlockCombination5 {
	
	public int tryThis(int x) {
		if (x < 0)
			return 0;
		else {
			if (x < 15) {
				int x12= tryThis(x - 12) + tryThis(x - 15);
				int x3= tryThis(x - 3);
				return x12 + x3;
			} else
				return 1 + tryThis(x - 1);
		}
	}
}