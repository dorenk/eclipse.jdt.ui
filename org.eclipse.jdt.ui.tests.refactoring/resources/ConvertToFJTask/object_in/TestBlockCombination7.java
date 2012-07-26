package object_in;

public class TestBlockCombination7 {
	
	public int tryThis(int x) {
		if (x < 0)
			return 0;
		else {
			int x1= tryThis(x - 12);
			int x2= tryThis(x - 15);
			int x3= 0;
			if (x > 15)
				x3= tryThis(x - 16);// + tryThis(x - 17);
			return x1 + x2 + x3;
		}
	}
}