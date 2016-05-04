package hk.ust.comp4321.data;

import hk.ust.comp4321.model.Page;

import java.io.IOException;
import java.util.Vector;

public abstract class SpiderDAO {

	public abstract Vector<Page> getAllPages() throws IOException;
	
	public abstract Page getPageById(int id) throws IOException;
	
	public abstract void add(Page n) throws IOException;
	
	public abstract void update(Page n) throws IOException;
	
	public abstract void close() throws IOException;

	public abstract Integer getPageId(String url) throws IOException;
	
}
