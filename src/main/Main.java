package main;
/**
 * downloadUrlTaken from http://www.codejava.net/coding/how-to-calculate-md5-and-sha-hash-values-in-java
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

import project.MerkleTree;

public class Main {

	public static void main(String[] args){
		
		
		MerkleTree m0 = new MerkleTree("data/1.txt");		
//		String hash = m0.getRoot().getLeft().getRight().getData();
		
		boolean valid = m0.checkAuthenticity("data/1meta.txt");
		System.out.println(valid);
		
		// The following just is an example for you to see the usage. 
		// Although there is none in reality, assume that there are two corrupt chunks in this example.
//		ArrayList<Stack<String>> corrupts = m0.findCorruptChunks("data/1meta.txt");
//		System.out.println("Corrupt hash of first corrupt chunk is: " + corrupts.get(0).pop());
//		System.out.println("Corrupt hash of second corrupt chunk is: " + corrupts.get(1).pop());
		
//		download("secondaryPart/data/download_from_trusted.txt");
		
	}
	
	public static void download(String path) {
		// Entry point for the secondary part
		Scanner urls = null;
		try {
			urls = new Scanner(new File(path));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(urls.hasNext() ) {
			String metahashes = urls.next();
			File metatxt = new File("secondaryPart/data" + metahashes.substring(metahashes.lastIndexOf('/')) );
			metatxt = downloadUrl(metatxt, metahashes);
			//meta klasörünü yap ve metalarý arraylistte tut ya da queue
			
			String firsturls = urls.next();
			//firstpath klasörü yap sonra tree oluþtur
			File firsturlstxt = new File("secondaryPart/data" + firsturls.substring(firsturls.lastIndexOf('/')) );
			firsturlstxt = downloadUrl(firsturlstxt, firsturls);
			
			String alturls = urls.next();
			File alturlstxt = new File("secondaryPart/data" + alturls.substring(alturls.lastIndexOf('/')) );
			alturlstxt = downloadUrl(alturlstxt, alturls);
			
			File keep = new File("secondaryPart/data/split" + firsturls.substring( firsturls.lastIndexOf('/'), firsturls.lastIndexOf('.') ) );
			keep.mkdirs();
			Queue<String> mychunkpaths = new LinkedList<String>();
			HashMap<String, String> myALTchunklinks = new HashMap<String, String>();
			
			Scanner urls1 = null;
			try {
				urls1 = new Scanner(firsturlstxt);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			while(urls1.hasNext()) {
				String thatUrl = urls1.next();
				File thatFile = new File("secondaryPart/data/split" + firsturls.substring( firsturls.lastIndexOf('/'), firsturls.lastIndexOf('.') )  + thatUrl.substring(thatUrl.lastIndexOf('/') ) );
				mychunkpaths.add("secondaryPart/data/split" + firsturls.substring( firsturls.lastIndexOf('/'), firsturls.lastIndexOf('.') )  + thatUrl.substring(thatUrl.lastIndexOf('/') ));
				thatFile = downloadUrl(thatFile, thatUrl);
			}
			Scanner urls2 = null;
			try {
				urls2 = new Scanner(alturlstxt);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			while(urls2.hasNext()) {
				String s = urls2.next();
				myALTchunklinks.put(s.substring(s.lastIndexOf('/') ) ,s);
			}
			
			//create tree
			MerkleTree m0 = new MerkleTree(mychunkpaths);		
			if(m0.checkAuthenticity("secondaryPart/data" + metahashes.substring(metahashes.lastIndexOf('/')) ) ) {

			}
			else {
				Queue<String> replaceUrls = new LinkedList<String>();
				replaceUrls = m0.replace(m0.getRoot(), myALTchunklinks, replaceUrls);
				while(!replaceUrls.isEmpty() ) {
					String replacingUrl = replaceUrls.remove();
					File thatFile = new File("secondaryPart/data/split" + firsturls.substring( firsturls.lastIndexOf('/'), firsturls.lastIndexOf('.') )  + replacingUrl.substring(replacingUrl.lastIndexOf('/') ) );
					thatFile = downloadUrl(thatFile, replacingUrl);
				}
			}
		  
		}
		
	}
		
	public static File downloadUrl(final File file, final String url) {
	    BufferedInputStream in = null;
	    FileOutputStream fout = null;
	    try {
	        try {
				in = new BufferedInputStream(new URL(url).openStream());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        try {
				fout = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	        final byte data[] = new byte[1024];
	        int count;
	        try {
				while ((count = in.read(data, 0, 1024)) != -1) {
				    try {
						fout.write(data, 0, count);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    } finally {
	        if (in != null) {
	            try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	        if (fout != null) {
	            try {
					fout.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	    }
	    return file;
	}
}