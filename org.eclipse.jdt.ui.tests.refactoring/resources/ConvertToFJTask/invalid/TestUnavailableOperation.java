package invalid;

public class TestUnavailableOperation {
	
	public String threeDaysIntoFuture(int x) {
		if(x < 5) {
			return "eat me";
		} else {
			if(x < 65) {
				return "avoid me" + threeDaysIntoFuture(x / 2);
			} else {
				return "find me" + threeDaysIntoFuture(x - 5);
			}
		}
	}
}