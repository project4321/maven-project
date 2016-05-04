package hk.ust.comp4321.tools;
import hk.ust.comp4321.data.JDBMSpiderDAO;
import hk.ust.comp4321.data.SpiderDAO;
import hk.ust.comp4321.html.HTTPClient;
import hk.ust.comp4321.html.LinkExtractor;
import hk.ust.comp4321.model.Page;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import org.htmlparser.util.ParserException;

public class Spider {
	
	private final int maxCount = 300;
	private String url;
	private SpiderDAO dao;
	private Vector<Page> newPages, deletedPages;
	
	public Spider(String rootURL) throws IOException{
		url = rootURL;
		dao = new JDBMSpiderDAO();
		newPages = new Vector<Page>();
		deletedPages = new Vector<Page>();
	}
	
	public void crawl() throws IOException, ParserException {
		
		int count = 0;
		Queue<String> queue = new LinkedBlockingQueue<String>(); // Queue for BFS
		Set<String> finishedSet = new HashSet<String>(); // collects all links that processed
		Set<String> processingSet = new HashSet<String>(); // collects all links that are processing
		Map<String, Vector<String>> parents = new HashMap<String, Vector<String>>();  
		
		queue.add(url);
		processingSet.add(url);
		
		while (queue.size() > 0 && count < maxCount){
			String url = queue.poll();
			
			
			Timestamp lastMod = HTTPClient.getLastMod(url);
			int size = HTTPClient.getSize(url);
			Vector<String> children = (new LinkExtractor(url)).extractLinks();
			String htmlContent = HTTPClient.getHTMLContent(url);
			String title = HTTPClient.getTitle(htmlContent);
			
			Page page = new Page(url, title, new Vector<String>(), children, lastMod, htmlContent, size);
			// System.out.print("id: " + dao.getPageId(url) + "; ");// + " lastMod: " + dao.getPageById(dao.getPageId(url)).getLastMod());
			if (dao.getPageId(url) == null    // not exists in db && new lastMod 
					|| dao.getPageById(dao.getPageId(url)).getLastMod().before(lastMod)) {
				
				dao.add(page); // only insert db if the page is new to db
				newPages.add(page);
				// System.out.print("new page: " + page.getURL());
			}
			// // System.out.println();
			
			for (String child: children){
				if (!parents.containsKey(child)) parents.put(child, new Vector<String>());
				parents.get(child).add(url);
				
				// if not processed && not processing -> not yet process -> add in Queue
				if (!finishedSet.contains(child) && !processingSet.contains(child)){
					processingSet.add(child);
					queue.add(child);
				}
			}
			
			processingSet.remove(url);
			finishedSet.add(url); // finish process 
			count ++;
		}
		
		for (Page page : dao.getAllPages()){
			if (parents.containsKey(page.getURL()))
				page.setParentLinks(parents.get(page.getURL()));
			dao.update(page);
		}
		
//		// // System.out.println("\nAll objects in graph :");
//		// // System.out.println(dao.getAllPages());
//		// // System.out.println("Invert : ");
//      // // System.out.println(((JDBMSpiderDAO) dao).getInvert());
//        
//		// // System.out.println("\nhave fun\n*********\n********\n*******\n******\n*****\n****\n***\n**\n*");
//		
//		Page temppage = dao.getPageById(12);
//		temppage.setLastMod(new Timestamp(0));
//		dao.update(temppage);
//		
//		// // System.out.println("\nAll objects in graph :");
//		// // System.out.println(dao.getAllPages());
//		// // System.out.println("(id:12): " + dao.getPageById(12));
		
		dao.close();
	}
	
	public Vector<Page> getNewPages() { return this.newPages; }
	public Vector<Page> getDeletedPages() { return this.deletedPages; }
}
