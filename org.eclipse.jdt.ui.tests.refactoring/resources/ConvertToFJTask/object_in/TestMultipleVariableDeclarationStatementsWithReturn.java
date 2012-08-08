package object_in;

public class TestMultipleVariableDeclarationStatementsWithReturn {
	
	public int coordinates(int num) {
		if (num <= 0) {
			return 0;
		} else {
			int x = coordinates(num - 1);
			int y = coordinates(num - 2);
			int z = coordinates(num - 3);
			return x + y + z;
		}
	}
}