package hk.ust.comp4321.tools;
import hk.ust.comp4321.data.JDBMIndexerDAO;
import hk.ust.comp4321.model.Page;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import org.jsoup.Jsoup;

import jdbm.helper.IterationException;

public class Indexer {
	private StopStem stopStem;
	private JDBMIndexerDAO idao;
	
	public Indexer() throws IOException{
		stopStem = new StopStem();
		idao = new JDBMIndexerDAO();
		
	}
	
	public void indexPage(Vector<Page> page) throws IOException{
		//Vector<Page> page = sdao.getAllPages();
		
		
		for(Page p : page){
			if(idao.containsPageId(p.getId()))
				deletePage(p.getId());
			Vector<String> keywords = new Vector<String>();		//for title
			HashMap<String, Vector<Integer>> t_positions = new HashMap<String, Vector<Integer>>();
			
			//Vector<String> keywords = new Vector<String>();		//for content
			HashMap<String, Vector<Integer>> c_positions = new HashMap<String, Vector<Integer>>();
			
			String[] t_wordList = p.getTitle().split("\\s+");
			for(int i=0; i<t_wordList.length; i++){
				String t_s = stopStem.removeStopwords(t_wordList[i]);
				if(!t_s.equals("")){
					if(!keywords.contains(t_s))
						keywords.add(t_s);
					
					if(!idao.containsWord(t_s))	//add to word->wordId
						idao.addWord(t_s);
					
					if(!t_positions.containsKey(t_s))
						t_positions.put(t_s, new Vector<Integer>());
					
					t_positions.get(t_s).add(i);
				}
			}
			// System.out.println("t_positions: " + t_positions);
			
			//for content
//			// System.out.println("raw html: " + p.getHTMLContent() + "\n");
			String html = Jsoup.parse(p.getHTMLContent()).text();
//			System.out .println("processed html: " + html + "\n");
			
			Vector<String> c_wordList = new Vector<String>();
			c_wordList.addAll(Arrays.asList(html.split("\\s+")));
//			// System.out.println("word list: " + c_wordList.toString());
			
			for(int i=0; i<c_wordList.size(); i++){
				String c_s = stopStem.removeStopwords(c_wordList.get(i));
				if(!c_s.equals(""))		//not stopword and stemmed
				{
					if(!keywords.contains(c_s))
						keywords.add(c_s);
					if(!idao.containsWord(c_s))	//add to word->wordId
						idao.addWord(c_s);
					
					if(!c_positions.containsKey(c_s))
						c_positions.put(c_s, new Vector<Integer>());
					
					c_positions.get(c_s).add(i);
				}
			}
			// System.out.println("c_positions: " + c_positions);
			
			idao.addIndex(p.getId(), keywords, t_positions, c_positions);
		}
		idao.hello();
		//idao.close();
	}
	
	public void deletePage(int id) throws IOException{
		for(String s : idao.getWordsByPageId(id)){
			idao.deleteWordPosting(s, id);
		}
		
		idao.deletePageWord(id);
	}
	
	public void printAll() throws IterationException, IOException{
		idao.printAll();
	}
	
	public void close() throws IOException{
		idao.close();
	}

}
