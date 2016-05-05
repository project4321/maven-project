import java.io.IOException;

import org.htmlparser.util.ParserException;
import org.springframework.boot.SpringApplication;

import hk.ust.comp4321.server.Server;
import hk.ust.comp4321.tools.Indexer;
import hk.ust.comp4321.tools.Spider;


public class JarMain {
	public static void main (String[] args) throws IOException, ParserException{
		if (args.length == 0){
			System.out.println(JarMain.usageMessage());
			return ;
		}
		
		switch (args[0]) {
		case "-i":
			if (args.length == 1) {
				System.out.println(JarMain.usageMessage());
				return ;
			}
			
			System.out.println("Crawling ...");
			
			Spider spider = new Spider(args[1]);
			spider.crawl();
			
			System.out.println("Indexing ...");
			
			Indexer indexer = new Indexer();
			indexer.indexPage(spider.getNewPages());
			indexer.close();
			
			System.out.println("Done");
			break;
			
		case "-s":
			SpringApplication.run(Server.class, new String[0]);
			break;

		default:
			System.out.println(JarMain.usageMessage());
			break;
		}
	}
	
	public static String usageMessage(){
		return "usage: java -jar searchengine.jar <option>\n" +
			   "option:\n" +
			   "    -i <webpage>    index from root=webpage\n" +
			   "    -s              build the server on port 8090";
	}
}
