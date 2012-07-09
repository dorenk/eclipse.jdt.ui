package invalid;

public class TestSwitchStatements {
	
	public int state(int x) {
		switch (x) {
			case 1:
				return 17;
			case 2:
				return -12;
			default:
				return 0;
		}
	}
}