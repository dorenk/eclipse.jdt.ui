package object_in;

public class TestMultipleVariableDeclarationAndReturnInSeparateBlocks {

	public int distance(int vertex) {
		if (vertex < 3) {
			return -1;
		} else {
			if (vertex < 20) {
				int third = distance(vertex / 3);
				int origin = distance(vertex - 18);
				int half = distance(vertex / 2 - 5);
				return third + origin - half;
			} else {
				return distance(vertex - 50) - distance(vertex / 5);
			}
		}
	}
}