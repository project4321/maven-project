package hk.ust.comp4321.tools;
import hk.ust.comp4321.IRUtilities.*;

import java.io.*;
import java.util.Scanner;

public class StopStem {
	static private Porter porter;
	static private java.util.Set<String> stopWords;
	
	static{
		porter = new Porter();
		stopWords = new java.util.HashSet<String>();
		
		readStopWordsList();
	}
	
	public boolean isStopWord(String str) {
		return stopWords.contains(str);
	}

	private static void readStopWordsList() {

		//Get file from resources folder
		InputStream is = StopStem.class.getResourceAsStream("/resources/stopwords.txt");  // for jar
		if (is == null) is = StopStem.class.getClassLoader().getResourceAsStream("stopwords.txt"); // java

		Scanner scanner = new Scanner(is);
		
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			stopWords.add(line);
		}

		scanner.close();
		
		System.out.println("stopWords: " + stopWords);
	}

	public String stem(String str) {
		return porter.stripAffixes(str);
	}
	
	public String removeStopwords(String str){
		return isStopWord(str)?"":stem(str);
	}

}
