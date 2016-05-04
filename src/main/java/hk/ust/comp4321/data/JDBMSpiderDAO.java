package hk.ust.comp4321.data;

import hk.ust.comp4321.model.Page;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

public class JDBMSpiderDAO extends SpiderDAO {

	private RecordManager recman;
    private HTree graphHashTable, invertedHashTable;
	
	public JDBMSpiderDAO() throws IOException{
		// create or open spider record manager
        recman = RecordManagerFactory.createRecordManager("spider", new Properties());

        // create or load graph
        long recid = recman.getNamedObject("graph"); // pageId -> page
        if (recid != 0) {
            // System.out.println("Reloading existing graph...");
            graphHashTable = HTree.load(recman, recid);
        } 
        else {
            // System.out.println("Creating new graph...");
            graphHashTable = HTree.createInstance(recman);
            recman.setNamedObject("graph", graphHashTable.getRecid());
        }
        
        recid = recman.getNamedObject("invert"); // pageUrl -> pageId
        if (recid != 0) {
            // System.out.println("Reloading existing invert...");
            invertedHashTable = HTree.load(recman, recid);
        } 
        else {
            // System.out.println("Creating new invert...");
            invertedHashTable = HTree.createInstance(recman);
            recman.setNamedObject("invert", invertedHashTable.getRecid());
        }
        // System.out.println("All objects in graph :");
        // System.out.println(this.getAllPages());
        // System.out.println("Invert : ");
        // System.out.println(this.getInvert());
	}
	
	@Override
	public Vector<Page> getAllPages() throws IOException {
		Vector<Page> pages = new Vector<Page>();
		
		FastIterator iter = graphHashTable.keys();
		Integer id;
		while ( (id = (Integer) iter.next()) != null ) {
			pages.add((Page) graphHashTable.get(id));
		}

		return pages;
	}
	
	public Map<String, Integer> getInvert() throws IOException{
		Map<String, Integer> map = new HashMap<String, Integer>();
		
		FastIterator iter = invertedHashTable.keys();
		String url;
		while ( (url = (String) iter.next()) != null) {
			map.put(url, (Integer) invertedHashTable.get(url));
		}
		return map;
	}

	@Override
	public Page getPageById(int id) throws IOException {
		return (Page) graphHashTable.get(id);
	}
	
	@Override 
	public Integer getPageId(String url) throws IOException {
		return (Integer) invertedHashTable.get(url);
	}
	
	@Override
	public void add(Page n) throws IOException {
		graphHashTable.put(n.getId(), n);
		invertedHashTable.put(n.getURL(), n.getId());
		recman.commit();
	}
	
	@Override
	public void update(Page n) throws IOException{
		graphHashTable.put(n.getId(), n);
//		invertedHashTable.put(n.getURL(), n.getId()); // not suppose to be change id with url -> no need mod that mapping
		recman.commit();
	}
	
	@Override
	public void close() throws IOException{
		recman.close();
	}
	
}
