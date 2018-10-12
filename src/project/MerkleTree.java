package project;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import util.HashGeneration;

public class MerkleTree {
	
	public Node root = null;

	Queue<String> chunks = new LinkedList<String>();
	Queue<String> metadatas = new LinkedList<String>();
	ArrayList<Stack<String>> corrupts = new ArrayList<Stack<String>>();
	Boolean metadatasEmpty = true;

	int height;
	
	public MerkleTree(String path) {
		Scanner datachunks = null;
		try {
			datachunks = new Scanner(new File(path));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while(datachunks.hasNext()) {
			chunks.add(datachunks.next());
		}
		this.height = (int)Math.ceil( Math.log(chunks.size() ) / Math.log(2) );
		this.root = create(root);
		datachunks.close();
	}
	
	public MerkleTree(Queue<String> chunkpaths) {
		while(!chunkpaths.isEmpty() ) {
			this.chunks.add(chunkpaths.remove() );
		}
		this.height = (int)Math.ceil( Math.log(chunks.size() ) / Math.log(2) );
		this.root = create(root);
	}
	
	private Node create(Node root)  {
		root = createTree_1(root, this.height);
		root = fillLeaves(root);
		for(int i=this.height-1; i>=0; i--) {
			root=fillLevel(root, i, 0);
		}
		return root;
	}

	private Node createTree_1(Node root, int height) {
		if(height==-1) {
			return root;
		}
		root = new Node("");
		root.left = createTree_1(root.left, height-1);
		root.right = createTree_1(root.right, height-1);
		return root;
	}

	private Node fillLeaves(Node node)  {
		if(node.left==null && node.right==null && !chunks.isEmpty()) {
			String s = chunks.remove();
			File c = new File(s);
			try {
				node.data=HashGeneration.generateSHA256(c);
			} catch (NoSuchAlgorithmException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			node.leafdatapath = s;
			return node;
		}
		else if(node.left==null && node.right==null)
			return node;
		node.left = fillLeaves(node.left);
		node.right = fillLeaves(node.right);
		return node;
	}

//    public Node fillLastLevel(Node node, int n) { //level is height-1
//    	if(this.height-1==n) {
//    		//do the hashing return the node?
//			//leafleri hashle
//			if(node.right.data.equals("") && node.left.data.equals(""))
//				node.data = "";
//			else if(!node.left.data.equals("") && node.right.data.equals(""))
//				node.data = HashGeneration.generateSHA256(node.left.data);
//			else if(!node.left.data.equals("") && !node.right.data.equals(""))
//				node.data = HashGeneration.generateSHA256(node.left.data+node.right.data);
//			return node;    		
//    	}
//    	//just call them and return node?
//    	node.leftfillLastLevel(node.left, n+1);
//    	fillLastLevel(node.right, n+1);
//    	return node;
//    }
    public Node fillLevel(Node node, int level, int n ) {
    	if(node == null ) {
    		return null;
    	}
    	if(level==n) {
    		//do the hashing return the node?
			if(node.right.data.equals("") && node.left.data.equals(""))
				node.data = "";
			else if(!node.left.data.equals("") && node.right.data.equals(""))
				try {
					node.data = HashGeneration.generateSHA256(node.left.data);
				} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			else if(!node.left.data.equals("") && !node.right.data.equals(""))
				try {
					node.data = HashGeneration.generateSHA256(node.left.data+node.right.data);
				} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			return node;      		
    	}
    	//just call them and return node?  
    	node.left = fillLevel(node.left, level, n+1);
    	node.right = fillLevel(node.right, level, n+1);
    	return node;
    }
//	private Node fillInternals(Node node, int h) { //recursive
//		
//		if(h==1) {
//			//leafleri hashle
//			if(node.right.data.equals("") && node.left.data.equals(""))
//				node.data = "";
//			else if(!node.left.data.equals("") && node.right.data.equals(""))
//				node.data = HashGeneration.generateSHA256(node.left.data);
//			else if(!node.left.data.equals("") && !node.right.data.equals(""))
//				node.data = HashGeneration.generateSHA256(node.left.data+node.right.data);
//			return node;
//		}
//		
//		//recursive hashleme
//		return node;
//	}
	public void bftaddmetadatas() {
		ArrayList<Node> st = new ArrayList<Node>();

		st.add(root);
		while (!st.isEmpty()) {
			Node n = st.remove(0);

			if (n.left != null) {
				st.add(n.left);
			}
			if (n.right != null) {
				st.add(n.right);
			}
			if(!n.data.equals("") )
				n.metadata = metadatas.remove();
			else 
				n.metadata = "";
		}
	}


	public boolean checkAuthenticity(String path) {
		Scanner metadata = null;
		try {
			metadata = new Scanner(new File(path));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while(metadata.hasNext()) {
			metadatas.add(metadata.next());
		}
		metadata.close();
		bftaddmetadatas();
		metadatasEmpty = false;
		//check root
		return root.data.equals(root.metadata);
	}
//		ArrayList<Stack<String>> corrupts = new ArrayList<Stack<String>>();
	
	public ArrayList<Stack<String>> findCorruptChunks(String path) {
		if(metadatasEmpty) {
			Scanner metadata = null;
			try {
				metadata = new Scanner(new File(path));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			while(metadata.hasNext()) {
				metadatas.add(metadata.next());
			}
			metadata.close();
			bftaddmetadatas();
			metadatasEmpty = false;			
		}
		//ASIL ÝÞLEM
		//1. kaç tane stack olcak bak
		//2. stackleri doldur (bunun için leaflere bi iþaret koymalýsýn
		System.out.println(NumofStacks() );
		for(int i=1; i<=NumofStacks(); i++) {
			Stack<String> s = new Stack<String>();
			corrupts.add(s);
		}
		if(!checkAuthenticity(path) ) 
			findCorruptChunks(root, 0);
		return corrupts;
	}

	private void findCorruptChunks(Node node, int index) {
		if(node.left==null && node.right==null) {
			corrupts.get(index).push(node.data);
			return;
		}
		
		corrupts.get(index).push(node.data);
		
		if(!node.left.data.equals(node.left.metadata) && node.right.data.equals(node.right.metadata) ) {
			findCorruptChunks(node.left, index);
		}
		else if(node.left.data.equals(node.left.metadata) && !node.right.data.equals(node.right.metadata) ) {
			findCorruptChunks(node.right, index);
		}
		else if(!node.left.data.equals(node.left.metadata) && !node.right.data.equals(node.right.metadata) ) {
			findCorruptChunks(node.left, index);
		    Queue<String> dummy = new LinkedList<String>();
		            
		    while(!corrupts.get(index).isEmpty())
		        corrupts.get(index+1).push(corrupts.get(index).pop());
		                            
		    while(!corrupts.get(index+1).isEmpty())
		        dummy.add(corrupts.get(index+1).pop());
		                                            
		    while(!dummy.isEmpty()) {
		        String n = dummy.remove();
		        corrupts.get(index).push(n);
		        corrupts.get(index+1).push(n);
		    }			
		    findCorruptChunks(node.right, index+1);
		}		
	}

	public Node getRoot() {
		return this.root;
	}
	private int NumofStacks() {
		int a = NumofStacks(root);
		return a;
	}
	private int NumofStacks(Node node) {
		if(node.left==null && node.right==null) {
			if(!node.data.equals(node.metadata))
			return 1;
			else 
				return 0;
		}
		return NumofStacks(node.left) + NumofStacks(node.right);
	}

	public Queue<String> replace(Node node, HashMap<String, String> myALTchunklinks, Queue<String> replaceUrls) {
		if(node.left==null && node.right==null ) {
			if(node.data.equals(node.metadata))
				return replaceUrls;
			else {
				String path = node.leafdatapath;
				replaceUrls.add(myALTchunklinks.get(path.substring(path.lastIndexOf('/') ) ) );
				node.leafdatapath = myALTchunklinks.get( path.substring(path.lastIndexOf('/') ) );
				return replaceUrls;
			}
		}
		replace(node.left, myALTchunklinks, replaceUrls);
		replace(node.right, myALTchunklinks, replaceUrls);
		return replaceUrls;
	}


}