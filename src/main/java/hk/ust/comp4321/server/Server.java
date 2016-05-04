package hk.ust.comp4321.server;

import hk.ust.comp4321.data.JDBMIndexerDAO;
import hk.ust.comp4321.data.JDBMSpiderDAO;
import hk.ust.comp4321.model.Page;
import hk.ust.comp4321.tools.Retrieval;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

@Controller
@EnableAutoConfiguration
public class Server {

    @RequestMapping("/")
    @ResponseBody
    public String home() {
        return "Hello World!";
    }
    
    @RequestMapping("/search")
    @ResponseBody
    public Vector<Map<String, Object>> search(@RequestParam(required=true) String[] qs) throws IOException{
    	
    	for (String q : qs ){ System.out.println(q); }
    	System.out.println();
    	
    	HashMap<Integer, Float> search = (new Retrieval("spider")).search(qs);
    	
    	Vector<Map<String, Object>> results = new Vector<Map<String,Object>>();
    	
    	for (Integer id : search.keySet()){
    		
    		Map<String, Object> map = new HashMap<String, Object>();
    		map.put("page", (new JDBMSpiderDAO()).getPageById(id));
    		map.put("score", search.get(id));
    		map.put("keywords", getMostSimilarWords(id));
    		
    		results.add(map);
    	}
    	System.out.println("done");
    	
    	return results;
    }
    
    @RequestMapping("/pages")
    @ResponseBody
    public Vector<Page> getPages() throws IOException{
    	return (new JDBMSpiderDAO()).getAllPages();
    }
    
    @RequestMapping("/words")
    @ResponseBody
    public String[] getAllWords() throws IOException{
    	
    	Vector<String> words = (new JDBMIndexerDAO()).getAllWords();
    	Collections.sort(words);

    	return words.toArray(new String[words.size()]);
    }
    
    private Vector<Map<String, String>> getMostSimilarWords(int pageId) throws IOException{
        	
    	Vector<Pair<String, Integer>> frequency = (new JDBMIndexerDAO()).getWordFrequencyByPageId(pageId);
//    	System.out.println(frequency.toString());
    	
    	Vector<Map<String, String>> result = new Vector<Map<String, String>>();
    	for (int i=0; i<frequency.size() && i<5; i++){
    		Map<String, String> map = new HashMap<String, String>();
    		map.put("word", frequency.get(i).getLeft());
    		map.put("freq", frequency.get(i).getRight().toString());
    		
    		result.add(map);
    	}
    	if (result.size() > 5) System.out.println("greater than 5, pageId: " + pageId);
    	return result;
    }
    
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Server.class, args);
    	
    }
}