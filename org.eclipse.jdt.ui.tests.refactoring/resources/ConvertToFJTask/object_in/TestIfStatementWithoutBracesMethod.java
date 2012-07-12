package object_in;

public class TestIfStatementWithoutBracesMethod {
	
	protected String grayCheckHierarchy(String treeElement) {

		// if this tree element is already gray then its ancestors all are as well
		if (treeElement.contentEquals("gray"))
			return "gray"; // no need to proceed upwards from here

		if (treeElement.endsWith(";")) {
			treeElement.split(";");
		}
		String parent= treeElement.toUpperCase();
		if (parent != null)
			return grayCheckHierarchy(parent) + grayCheckHierarchy(parent.toLowerCase());
	}
}