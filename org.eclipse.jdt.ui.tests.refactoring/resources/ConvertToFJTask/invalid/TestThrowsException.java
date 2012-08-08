package invalid;

public class TestThrowsException {
	
	public void run(int seconds) throws Exception {
		
		if (seconds <= 0)
			return;
		else {
			run(seconds / 2);
			run(seconds / 2);
		}
	}
}