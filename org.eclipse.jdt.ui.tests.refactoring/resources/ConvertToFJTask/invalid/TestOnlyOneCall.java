package invalid;

public class TestOnlyOneCall {
	
	public void yesterday() {
		if(false){
			return;
		} else {
			yesterday();
		}
	}
}