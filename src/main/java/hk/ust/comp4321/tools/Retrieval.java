package hk.ust.comp4321.tools;

import hk.ust.comp4321.model.Page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;

public class Retrieval {
	private RecordManager recman;
	private HTree graphHashTable, invertedContentHashTable, invertedTitleHashTable, pageWordHashTable;

	public Retrieval(String recordmanager) throws IOException{
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
	
	public Number [][] search(String[] keywords) throws IOException {
		String[] keywords2 = keywords;
		for (int i = 0; i < keywords2.length; i++){
			StopStem stopStem = new StopStem();
			if (keywords2[i].contains(" ")){
				String[] phrase = keywords2[i].split(" ");
				keywords2[i] = "";
				for (int j = 0; j < phrase.length; j++){
					phrase[j] = stopStem.removeStopwords(phrase[j]);
					keywords2[i] += phrase[j];
					if (j < phrase.length - 1){
						keywords2[i] += " ";
					}
				}
			}
			else {
				keywords2[i] = stopStem.removeStopwords(keywords2[i]);
			}
		}
		
		Vector<Integer> pageIDs = new Vector<Integer>();
		FastIterator iter = graphHashTable.keys();
	    Object key;
	    
        while( (key = iter.next())!=null){
        	int int_key = (Integer) key;
        	Page page;
        	page = (Page) graphHashTable.get(int_key);
        	for (int i = 0; i < keywords.length; i++){
        		if (page.getTitle().toLowerCase().contains(keywords[i].toLowerCase())){
        			if (!pageIDs.contains(int_key)){
        				pageIDs.addElement(int_key);
        			}
        		}
        		else if (page.getHTMLContent().toLowerCase().contains(keywords[i].toLowerCase())){
        			if (!pageIDs.contains(int_key)){
        				pageIDs.addElement(int_key);
        			}
        		}
        	}
        }
        HashMap<Integer, Float> pageRanks = new HashMap<Integer, Float>();
        for (int pageID : pageIDs){
        	pageRanks = pageRank(pageRanks, keywords2, pageID);
        }
        
        List<Map.Entry<Integer, Float>> list = new ArrayList<Map.Entry<Integer, Float>>(pageRanks.entrySet ());
        
        Collections.sort(list, new Comparator<Map.Entry<Integer, Float>>() {  
            public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {  
                return  o2.getValue().compareTo (o1.getValue());
            }  
        });
        //System.out.println(list);
        Number [][] sortedPageIDs = new Number[50][2];
        int index = 0;
        while (index < 50){
        	if (index >= list.size()){
        		break;
        	}
        	sortedPageIDs[index][0] = list.get(index).getKey();
        	sortedPageIDs[index][1] = list.get(index).getValue();
        	index++;
        }
	
        for (int i = 1; i <= 300; i++){
        	Boolean add = true;
        	for (int j = 0; j < index; j++){
        		if (sortedPageIDs[j][0].intValue() == i){
        			add = false;
        			break;
        		}
        	}
        	if (add && index < 50){
        		sortedPageIDs[index][0] = i;
        		sortedPageIDs[index][1] = 0;
        		index++;
        	}
        }
		return sortedPageIDs;
	}
	
	public HashMap<Integer, Float> pageRank(HashMap<Integer, Float> pageRanks, String[] keywords2, int pageID) throws IOException {
		HashMap<Integer, Float> new_pageRanks = pageRanks;
		HashMap<String, Float> tf = new HashMap<String, Float>();
		
		Page page = (Page) graphHashTable.get(pageID);
		Vector<String> keywords = (Vector<String>) pageWordHashTable.get(pageID);
		
		int totalWordFreq = 0;
		int maxtf = 0;
		if (keywords != null){
	    	for (int i = 0; i < keywords.size(); i++){
	    		HashMap<Integer, Vector<Integer>> posting;
	    		Vector<Integer> position;
	    		if (invertedContentHashTable.get(keywords.get(i)) != null){
	    			posting = (HashMap<Integer, Vector<Integer>>) invertedContentHashTable.get(keywords.get(i));
	    			if (posting.get(pageID) != null){
	    				position = (Vector<Integer>) posting.get(pageID);
	    				totalWordFreq += position.size();
	    				if (position.size() > maxtf){
	    					maxtf = position.size();
	    				}
	    			}
	    		}
	    		
	    		if (invertedTitleHashTable.get(keywords.get(i)) != null){
	    			posting = (HashMap<Integer, Vector<Integer>>) invertedTitleHashTable.get(keywords.get(i));
	    			if (posting.get(pageID) != null){
	    				position = (Vector<Integer>) posting.get(pageID);
	    				totalWordFreq += position.size();
	    				if (position.size() > maxtf){
	    					maxtf = position.size();
	    				}
	    			}
	    		}
	    	}
	    }
		
		int count = 0;
		for (int i = 0; i < keywords2.length; i++){
			if (keywords2[i].contains(" ")){
				String [] phraseArray = keywords2[i].split(" ");
				count += StringUtils.countMatches(page.getHTMLContent().toLowerCase(), keywords2[i]) * (phraseArray.length - 1);
				count += StringUtils.countMatches(page.getTitle().toLowerCase(), keywords2[i]) * (phraseArray.length - 1);
				if (StringUtils.countMatches(page.getHTMLContent().toLowerCase(), keywords2[i]) > maxtf){
					maxtf = StringUtils.countMatches(page.getHTMLContent().toLowerCase(), keywords2[i]);
				}
				if (StringUtils.countMatches(page.getTitle().toLowerCase(), keywords2[i]) > maxtf){
					maxtf = StringUtils.countMatches(page.getTitle().toLowerCase(), keywords2[i]);
				}
			}
		}
		
		for (int i = 0; i < keywords2.length; i++){
			int freq = 0;
			if (keywords2[i].contains(" ")){
				freq = StringUtils.countMatches(page.getHTMLContent().toLowerCase(), keywords2[i]);
				freq += 1.5 *StringUtils.countMatches(page.getTitle().toLowerCase(), keywords2[i]);
			}
			float tfxidf = (float) (freq * Math.log(totalWordFreq - count) / Math.log(2));
			tf.put(keywords2[i], tfxidf);
		}
		
	    if (keywords != null){
	    	for (int i = 0; i < keywords.size(); i++){
	    		HashMap<Integer, Vector<Integer>> posting;
	    		Vector<Integer> position;
	    		int freq = 0;
	    		float tfxidf = 0;
	    		if (invertedContentHashTable.get(keywords.get(i)) != null){
	    			posting = (HashMap<Integer, Vector<Integer>>) invertedContentHashTable.get(keywords.get(i));
	    			if (posting.get(pageID) != null){
	    				position = (Vector<Integer>) posting.get(pageID);
	    				freq = position.size();
	    			}
	    		}
	    		
	    		if (invertedTitleHashTable.get(keywords.get(i)) != null){
	    			posting = (HashMap<Integer, Vector<Integer>>) invertedTitleHashTable.get(keywords.get(i));
	    			if (posting.get(pageID) != null){
	    				position = (Vector<Integer>) posting.get(pageID);
	    				freq += 1.5 * position.size();
	    			}
	    		}
	    		
	    		tfxidf = (float) (freq * Math.log(totalWordFreq) / Math.log(2));
				tf.put(keywords.get(i), tfxidf);
	    	}
	    }
	    
	    HashMap<String, Float> querytf = new HashMap<String, Float>();
	    for (int i = 0; i < keywords2.length; i++){
	    	float freq = 0;
	    	if (i != 0){
	 			Boolean done = false;
	 			for (int j = 0; j < i; j++){
	 				if (keywords2[j].equals(keywords2[i])){
	 					done = true;
	 					break;
	 				}
	 			}
	 			if (!done){
	 				freq = 1;
	 				for (int k = i+1; k < keywords2.length; k++){
	 					if (keywords2[k].equals(keywords2[i])){
	 						freq++;
	 					}
	 				}
	 			}
	 		}
	 		else {
	 			freq = 1;
	 			for (int k = i+1; k < keywords2.length; k++){
	 				if (keywords2[k].equals(keywords2[i])){
	 					freq++;
	 				}
	 			}
	 		}
	    	if (freq != 0){
	    		querytf.put(keywords2[i], freq);
	    	}
	    }
	    
	    // |query|
	 	float queryLength = 0;
	 	for (String key : querytf.keySet()) {
	 		queryLength += Math.pow(querytf.get(key), 2);
	 	}
	 	queryLength = (float) Math.sqrt(queryLength);
	    
	    // |pageID|
	 	float pageLength = 0;
	 	for (String key : tf.keySet()) {
	 	    pageLength += Math.pow(tf.get(key), 2);
	 	}
	 	pageLength = (float) Math.sqrt(pageLength);
	 	
	 	
	 	//cos
	 	float totaltf = 0;
	 	for (int i = 0; i < keywords2.length; i++){
	 		if (tf.get(keywords2[i]) != null && querytf.get(keywords2[i]) != null){
	 			totaltf += tf.get(keywords2[i]) * querytf.get(keywords2[i]);
	 		}
	 	}
	 	float cosSim = totaltf / (pageLength * queryLength);
	 	new_pageRanks.put(pageID, cosSim);
		return new_pageRanks;
	}
	
	public void finalize() throws IOException {
		recman.commit();
		recman.close();
	}
	
	public static void main(String[] args) throws IOException {
		Retrieval retrieval = new Retrieval("spider");
		String[] keywords = {"approx", "zhi", "zhuotianlin", "zo"};
		Number [][] pageID = retrieval.search(keywords);
		for (int i = 0; i < 50; i++){
        	System.out.println(pageID[i][0] + " ," + pageID[i][1]);
        }
		/*
		for (int i = 0; i < pageID.length; i++){
			System.out.println(pageID[i]);
		}
		*/
		retrieval.finalize();
	}
}
