package invalid;

public class TestIfStatementWithoutBraces {
	
	protected void grayCheckHierarchy(String treeElement) {

		// if this tree element is already gray then its ancestors all are as well
		if (treeElement.contentEquals("gray"))
			return; // no need to proceed upwards from here

		if (treeElement.endsWith(";")) {
			treeElement.split(";");
		}
		String parent= treeElement.toUpperCase();
		if (parent != null)
			grayCheckHierarchy(parent);
	}
}