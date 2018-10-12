package project;

public class Node {

	String data;
	String metadata;
	Node left = null;
	Node right = null;
	String leafdatapath;
	
	public Node(String data) {
		this.data = data;
	}
	public Node getLeft() {
		return this.left;
	}
	
	public Node getRight() {
		return this.right;
	}
	public String getData() {
		return this.data;
	}

}
