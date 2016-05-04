
import hk.ust.comp4321.model.Page;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Vector;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;

public class TestProgram {
	private RecordManager recman;
	private HTree graphHashTable, invertedContentHashTable, invertedTitleHashTable, pageWordHashTable;;
	
	TestProgram(String recordmanager) throws IOException{
		recman = RecordManagerFactory.createRecordManager(recordmanager);

		long graph = recman.getNamedObject("graph");
		if (graph != 0) {
			graphHashTable = HTree.load(recman, graph);
        } 
        else {
            System.out.println("No graphHashTable");
            graphHashTable = HTree.createInstance(recman);
            recman.setNamedObject("graph", graphHashTable.getRecid());
        }
		
		long invertedContent = recman.getNamedObject("invertedContent");
		if (invertedContent != 0) {
			invertedContentHashTable = HTree.load(recman, invertedContent);
        } 
        else {
            System.out.println("No invertedContentHashTable");
            invertedContentHashTable = HTree.createInstance(recman);
            recman.setNamedObject("invertedContent", invertedContentHashTable.getRecid());
        }
		
		long invertedTitle = recman.getNamedObject("invertedTitle");
		if (invertedTitle != 0) {
			invertedTitleHashTable = HTree.load(recman, invertedTitle);
        } 
        else {
            System.out.println("No invertedTitleHashTable");
            invertedTitleHashTable = HTree.createInstance(recman);
            recman.setNamedObject("invertedTitle", invertedTitleHashTable.getRecid());
        }
		
		long pageWord = recman.getNamedObject("pageWord");
		if (pageWord != 0) {
			pageWordHashTable = HTree.load(recman, pageWord);
        } 
        else {
            System.out.println("No pageWordHashTable");
            pageWordHashTable = HTree.createInstance(recman);
            recman.setNamedObject("pageWord", pageWordHashTable.getRecid());
        }
	}
	
	public void print() throws IOException {
		File file = new File("spider_result.txt");

	    // Create a file
	    PrintWriter output = new PrintWriter(file);	    
	    FastIterator iter = graphHashTable.keys();
	    Object key;
        while( (key = iter.next())!=null){
        	int int_key = (Integer) key;
        	Page page;
        	page = (Page) graphHashTable.get(int_key);
        	output.print("Page title: ");
        	output.println(page.getTitle());
        	output.print("URL: ");
    	    output.println(page.getURL());
    	    output.print("Last modification date: ");
    	    output.print(page.getLastMod());
    	    output.print(", size of page: ");
    	    output.println(page.getSize());
    	    
    	    Vector<String> keywords = (Vector<String>) pageWordHashTable.get(int_key);
    	    if (keywords != null){
    	    	for (int i = 0; i < keywords.size(); i++){
    	    		HashMap<Integer, Vector<Integer>> posting;
    	    		Vector<Integer> position;
    	    		int freq = 0;
    	    		if (invertedContentHashTable.get(keywords.get(i)) != null){
    	    			posting = (HashMap<Integer, Vector<Integer>>) invertedContentHashTable.get(keywords.get(i));
    	    			if (posting.get(int_key) != null){
    	    				position = (Vector<Integer>) posting.get(int_key);
    	    				freq = position.size();
    	    			}
    	    		}
    	    		
    	    		if (invertedTitleHashTable.get(keywords.get(i)) != null){
    	    			posting = (HashMap<Integer, Vector<Integer>>) invertedTitleHashTable.get(keywords.get(i));
    	    			position = (Vector<Integer>) posting.get(int_key);
    	    			if (posting.get(int_key) != null){
    	    				position = (Vector<Integer>) posting.get(int_key);
    	    				freq = position.size();
    	    			}
    	    		}
    	    	
    	    		output.print(keywords.get(i) + " " + freq + ";");
    	    	}
    	    	output.println();
    	   	}
    	    
    	    Vector<String> parentLinks = page.getparentLinks();
    	    for (int i = 0; i < parentLinks.size(); i++){
    	    	output.print("Parent Link" + (i+1) + ": ");
        	    output.println(parentLinks.get(i));
    	    }
    	    Vector<String> childLinks = page.getchildLinks();
    	    for (int i = 0; i < childLinks.size(); i++){
    	    	output.print("Child Link" + (i+1) + ": ");
        	    output.println(childLinks.get(i));
    	    }
        	output.println("-------------------------------------------------------------------------------------------");
        }
        
        // Close the file
	    output.close();
	}
	
	public void finalize() throws IOException {
		recman.commit();
		recman.close();
	} 
	
	public static void main(String[] args) throws IOException {
		TestProgram test = new TestProgram("spider");
		test.print();
		test.finalize();
	}
}
