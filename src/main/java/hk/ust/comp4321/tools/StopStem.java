package hk.ust.comp4321.tools;
import hk.ust.comp4321.IRUtilities.*;

import java.io.*;
import java.util.Scanner;

public class StopStem {
	static private Porter porter;
	static private java.util.HashSet<String> stopWords;
	
	static{
		porter = new Porter();
		stopWords = new java.util.HashSet<String>();
		
		readStopWordsList();
	}
	

	public boolean isStopWord(String str) {
//		if(stopWords.contains(str))  System.out.println(str+" is Stop word");
		return stopWords.contains(str);
	}

	private static void readStopWordsList() {
		//super();
		/*porter = new Porter();
		stopWords = new java.util.HashSet<String>();*/

		Scanner s = null;

		try {
			s = new Scanner(new BufferedReader(new FileReader("stopwords.txt")));

			while (s.hasNext()) {
				//// System.out.println(s.next());
				stopWords.add(s.next());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(s!=null)
			s.close();
		

	}

	public String stem(String str) {
		return porter.stripAffixes(str);
	}
	
	public String removeStopwords(String str){
		return isStopWord(str)?"":stem(str);
	}

	/*public static void main(String[] arg) {
		StopStem stopStem = new StopStem();
		String input = "";
		try {
			do {
				System.out.print("Please enter a single English word: ");
				BufferedReader in = new BufferedReader(new InputStreamReader(
						System.in));
				input = in.readLine();
				if (input.length() > 0) {
					if (stopStem.isStopWord(input))
						// System.out.println("It should be stopped");
					else
						// System.out.println("The stem of it is \""
								+ stopStem.stem(input) + "\"");
				}
			} while (input.length() > 0);
		} catch (IOException ioe) {
			System.err.println(ioe.toString());
		}
	}*/
}
