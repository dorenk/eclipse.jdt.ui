package invalid

public class TestBaseCaseAfterReturn {
	
	public String getQualifiedName(String input) {
			if (input.length() >= 2)
				return getQualifiedName(input.substring(1));
			if (! input.contains("x"))
				return input.toUpperCase();
			else
				return input.toLowerCase();
		}
}