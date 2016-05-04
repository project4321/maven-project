package hk.ust.comp4321.html;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;


public class HTTPClient {

	public static String getHTMLContent(String urlstring) throws IOException{
		
		URL url = new URL(urlstring);
		URLConnection con = url.openConnection();
		InputStream in = con.getInputStream();
		String encoding = con.getContentEncoding();
		encoding = encoding == null ? "UTF-8" : encoding;
		
		return IOUtils.toString(in, encoding);
	}
	
	public static Timestamp getLastMod(String urlstring) throws IOException {
		
		URL url = new URL(urlstring);
	    HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();

	    long date = httpCon.getLastModified();
	    if (date == 0) return new Timestamp(httpCon.getDate()); 
	    else return new Timestamp(date);
	}

	public static int getSize(String urlstring) throws IOException {
		URL url = new URL(urlstring);
		
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		if (httpCon.getHeaderField("size") == null) return HTTPClient.getHTMLContent(urlstring).length();
		else return Integer.parseInt(httpCon.getHeaderField("size"));
	}
	
	
	public static String getTitle(String htmlContent) {

		htmlContent = htmlContent.replaceAll("\\s+", " ");
	    Pattern p = Pattern.compile("<title>(.*?)</title>");
	    Matcher m = p.matcher(htmlContent);
	    
	    if (m.find() == true) { return m.group(1).trim(); }
	    else { return ""; }
	}
	
}
