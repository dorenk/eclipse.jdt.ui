package invalid;

public class TestBooleanReturn {
	
	public boolean elementAncestor(String ancestor, String element) {
		if (element != null)
			return element.equals(ancestor) || elementAncestor(ancestor, element.toLowerCase());
		else
			return false;
	}
}