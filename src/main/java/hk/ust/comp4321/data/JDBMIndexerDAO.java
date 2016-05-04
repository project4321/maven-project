package hk.ust.comp4321.data;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.helper.IterationException;
import jdbm.htree.HTree;

public class JDBMIndexerDAO{

	/*import java.io.IOException;
	import java.util.HashMap;
	import java.util.Map;
	import java.util.Properties;
	import java.util.Vector;

	import model.Page;*/


		private RecordManager recman;
	    private HTree wordIdHashTable, invertedContentHashTable, invertedTitleHashTable, pageWordHashTable;
	    private static int wordNum = 0;
		
		public JDBMIndexerDAO() throws IOException{
			// create or open spider record manager
	        recman = RecordManagerFactory.createRecordManager("spider", new Properties());

	        // create or load wordId
	        long recid = recman.getNamedObject("wordId"); // word -> wordID
	        if (recid != 0) {
	            // System.out.println("Reloading existing wordId...");
	            wordIdHashTable = HTree.load(recman, recid);
	        } 
	        else {
	            // System.out.println("Creating new wordId...");
	            wordIdHashTable = HTree.createInstance(recman);
	            recman.setNamedObject("wordId", wordIdHashTable.getRecid());
	        }
	        
	     // create or load invertedContent
	        recid = recman.getNamedObject("invertedContent"); // word -> {pageId, <wordPositions>} for body
	        if (recid != 0) {
	            // System.out.println("Reloading existing invertedContent...");
	            invertedContentHashTable = HTree.load(recman, recid);
	        } 
	        else {
	            // System.out.println("Creating new invertedContent...");
	            invertedContentHashTable = HTree.createInstance(recman);
	            recman.setNamedObject("invertedContent", invertedContentHashTable.getRecid());
	        }
	        
		// create or load invertedTitle
	        recid = recman.getNamedObject("invertedTitle"); // word -> {pageId, <wordPositions>} for title
	        if (recid != 0) {
	            // System.out.println("Reloading existing invertedTitle...");
	            invertedTitleHashTable = HTree.load(recman, recid);
	        } 
	        else {
	            // System.out.println("Creating new invertedTitle...");
	            invertedTitleHashTable = HTree.createInstance(recman);
	            recman.setNamedObject("invertedTitle", invertedTitleHashTable.getRecid());
	        }
	        
	     // create or load pageWord
	        recid = recman.getNamedObject("pageWord"); // pageId -> <keywords>
	        if (recid != 0) {
	            // System.out.println("Reloading existing pageWord...");
	            pageWordHashTable = HTree.load(recman, recid);
	        } 
	        else {
	            // System.out.println("Creating new pageWord...");
	            pageWordHashTable = HTree.createInstance(recman);
	            recman.setNamedObject("pageWord", pageWordHashTable.getRecid());
	        }

	        /*while(wordIdHashTable.keys().next()!=null)
	        	wordNum++;*/
		}

		public boolean containsWord(String s) throws IOException{
			return (wordIdHashTable.get(s) != null);
		}
		
		public boolean containsPageId(int id) throws IOException{
			return (pageWordHashTable.get(id) != null);
		}
		
		public void addWord(String s) throws IOException{
			// System.out.println("addWord:"+ s);
			int n=wordNum+1;
			wordIdHashTable.put(s,n);  		//wordId starts from 1
			wordNum+=1;
			// System.out.println(s+" -> "+wordIdHashTable.get(s));
			
			recman.commit();
		}
		
		public void addIndex(int pageId, Vector<String>keywords, HashMap<String, Vector<Integer>> t_positions, HashMap<String, Vector<Integer>> c_positions) throws IOException{
			pageWordHashTable.put(pageId, keywords);
			for(String s:t_positions.keySet())
			{
				HashMap<Integer, Vector<Integer>> posting;
				if(invertedTitleHashTable.get(s) == null)
					{posting = new HashMap<Integer, Vector<Integer>>();}
				else
					{posting = (HashMap<Integer, Vector<Integer>>) invertedTitleHashTable.get(s);}
				
				posting.put(pageId, t_positions.get(s));
				invertedTitleHashTable.put(s, posting);
				// System.out.println("invertedTitleHashTable.get(" + s + "): " + invertedTitleHashTable.get(s));
			}
			
			for(String s:c_positions.keySet())
			{
				HashMap<Integer, Vector<Integer>> posting;
				if(invertedContentHashTable.get(s) == null)
					posting = new HashMap<Integer, Vector<Integer>>();
				else
					posting = (HashMap<Integer, Vector<Integer>>) invertedContentHashTable.get(s);
				posting.put(pageId, c_positions.get(s));
				invertedContentHashTable.put(s, posting);
				// System.out.println("invertedContentHashTable.get(" + s + "): " + invertedContentHashTable.get(s));
			}	
			recman.commit();
			
			
		}
		
		public Vector<String> getWordsByPageId(int id) throws IOException{
			if (pageWordHashTable.get(id) == null) return new Vector<String>();
			else return (Vector<String>) pageWordHashTable.get(id);
		}
		
		public HashMap<Integer, Vector<Integer>> getTitlePostingByWord(String word) throws IOException{
			return (HashMap<Integer, Vector<Integer>>) invertedTitleHashTable.get(word);
		}
		
		public HashMap<Integer, Vector<Integer>> getContentPostingByWord(String word) throws IOException{
			return (HashMap<Integer, Vector<Integer>>) invertedContentHashTable.get(word);
		}
		
		public void deleteWordPosting(String word, int pageId) throws IOException{
			Vector<String> removedWords = new Vector<String>();
			
			//delete words in title
			if(getTitlePostingByWord(word)!= null && getTitlePostingByWord(word).containsKey(pageId))
			{
				
				if(getTitlePostingByWord(word).size()!=1)
				{
					((HashMap<Integer, Vector<Integer>>) invertedTitleHashTable.get(word)).remove(pageId);
				}else{
					invertedTitleHashTable.remove(word);
					removedWords.add(word);
					/*wordIdHashTable.remove(word);
					wordNum--;*/
				}
			}
			//delete words in content
			if(getContentPostingByWord(word)!= null && getContentPostingByWord(word).containsKey(pageId))
			{
				
				if(getContentPostingByWord(word).size()!=1)
				{
					((HashMap<Integer, Vector<Integer>>) invertedContentHashTable.get(word)).remove(pageId);
				}else{
					invertedContentHashTable.remove(word);
					removedWords.add(word);
				}
			}
			
			//remove word in word->wordId
			if(getTitlePostingByWord(word) == null && getContentPostingByWord(word) == null)
			{
				wordIdHashTable.remove(word);
				wordNum--;
			}
				
		}
		
		public void deletePageWord(int pageId) throws IOException{
			pageWordHashTable.remove(pageId);
		}
		
		public Vector<Pair<String, Integer>> getWordFrequencyByPageId(int pageId) throws IOException{
	    	
			Vector<Pair<String, Integer>> frequency = new Vector<Pair<String, Integer>>();
	    	
			Vector<String> words = this.getWordsByPageId(pageId);
	    	for (String word : words){
	    		HashMap<Integer, Vector<Integer>> contentPosting = this.getContentPostingByWord(word);
	    		Vector<Integer> wordPosition = contentPosting.get(pageId);
	    		Pair<String, Integer> pair = new ImmutablePair<String, Integer>(word, wordPosition.size());
	    		frequency.add(pair);
	    	}
	    	
	    	Collections.sort(frequency, new Comparator<Pair<String, Integer>>() {
				public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
					if (o1.getRight() == o2.getRight())		return 0;
					else if (o1.getRight() > o2.getRight())	return -1;
					else 									return 1;
				}
			});
	    	
	    	return frequency;
	    }
		
		public Vector<String> getAllWords() throws IOException{
			
			Vector<String> words = new Vector<String>();
			
			FastIterator it = invertedContentHashTable.keys();
			String word;
			while ( (word = (String) it.next()) != null ) {
				words.add(word);
			}
			
			return words;
		}
		
		public void printAll() throws IterationException, IOException{
			// System.out.println("word->wordId:");
			int n=0;
			/*for(int i=0; i<10; i++){
				FastIterator word = wordIdHashTable.keys();
				
				while(word.next()!= null && n<10){
					// System.out.println((String) word.next()+" -> "+wordIdHashTable.get(word.next()));
					n++;
					
				}

				
			}
			// System.out.println("pageWord:     "+pageWordHashTable.keys());*/
			
			//pageWordHashTable.keys()
			// System.out.println("Printing....");
			Vector<String> list = (Vector<String>) pageWordHashTable.get(1);
			for(String s: list)
			{
				// System.out.println(s);
			}
			
			}
		//}

		public void close() throws IOException {
			// TODO Auto-generated method stub
			recman.close();
		}
		

		public void hello() throws IterationException, IOException {
			// System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n");
			String word;
			FastIterator it = wordIdHashTable.keys();
			while ( (word = (String) it.next()) != null) {
				// System.out.println((String) word + " -> " + wordIdHashTable.get(word));				
			}
		}
		
}
