package object_in;

public class TestBlockCombination1 {
	
	public int tryThis(int x) {
		if (x < 0)
			return 0;
		else {
			if (x > 15) {
				int x1= tryThis(x - 12);
				int x2= tryThis(x - 15);
				return x1 + x2;
			} else {
				int x3= tryThis(x - 1);
				return x3;
			}
		}
	}
}