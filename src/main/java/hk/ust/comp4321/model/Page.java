package hk.ust.comp4321.model;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Vector;


public class Page implements Serializable { 
	
	private static final long serialVersionUID = -8544223146268759686L;

	private static int idcount = 1;
	
	private int id, size;
	private String url, title;
	private Vector<String> parentLinks, childLinks;
	private Timestamp lastMod;
	private String htmlContent;
	
	public Page(){
		id = idcount++;
		this.url = "";
		this.title = "";
		this.parentLinks = new Vector<String>();
		this.childLinks = new Vector<String>();
		this.lastMod = new Timestamp(0);
		this.htmlContent = "";
	}
	
	public Page(String url, String title, Vector<String> parentLinks, Vector<String> childLinks, Timestamp lastMod, String htmlContent, int size){
		id = idcount++;
		this.url = url;
		this.title = title;
		this.parentLinks = parentLinks;
		this.childLinks = childLinks;
		this.lastMod = lastMod;
		this.htmlContent = htmlContent;
		this.size = size;
	}
	
	@Override
	public String toString(){
		return "{\n"
				+ "\tid: " + id + ", \n"
				+ "\ttitle: " + title + ", \n"
				+ "\turl: " + url + ", \n"
				+ "\tparentLinks:" + parentLinks.toString() + ", \n"
				+ "\tchildLinks: " + childLinks.toString() + ", \n"
				+ "\tlastMod: " + lastMod + "\n"
//				+ "\thtmlContent: " + htmlContent + "\n"
				+ "}";
	}
	
	public int getId() { return this.id; }
	public String getURL() { return this.url; }
	public String getHTMLContent() { return this.htmlContent; }
	public Vector<String> getparentLinks() { return this.parentLinks; }
	public Vector<String> getchildLinks() { return this.childLinks; }
	public Timestamp getLastMod() { return this.lastMod; }
	public String getTitle() { return this.title; }
	public int getSize() { return this.size; }
	
	public void setParentLinks(Vector<String> parentLinks) { this.parentLinks = parentLinks; }
	public void setLastMod(Timestamp ts) { this.lastMod = ts; }
}
