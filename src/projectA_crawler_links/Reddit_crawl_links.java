package projectA_crawler_links;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import java.util.List;
import java.util.ArrayList;

public class Reddit_crawl_links {
	public static int DELAY = 1000;
	private static int threadPool = 10;        // The number of threads
	
	void directoryCheck(String s) {            // Create subreddit folder
		String dirName = "data/" + s;
		File dir = new File(dirName);
		if (!dir.exists()) {
			dir.mkdir();
			System.out.println("Create new directory");
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		Reddit_crawl_links test = new Reddit_crawl_links();
		String dirName = "data";
		File dir = new File(dirName);
		if (!dir.exists()) {
			dir.mkdir();
			System.out.println("Create data directory");
		}
		
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
		
		FileReader fr = new FileReader("seed.txt");
		BufferedReader br = new BufferedReader(fr);
		
		// Each threads have their own link pool.
		List<List<String>> total = new ArrayList<List<String>>();
		for (int i = 0; i < threadPool; i++) {
			List<String> eachThread = new ArrayList<String>();
			total.add(eachThread);
		}
		
		int threadCounter = 0;
		String line;
		while ((line = br.readLine()) != null) {
			String[] targetSeparate = line.split("/");
			test.directoryCheck(targetSeparate[4]); // Create directory when reads file
			total.get(threadCounter).add(line + "?limit=500");   // Get 500 comments
			threadCounter = (threadCounter + 1) % threadPool;
		}
		br.close();
		
		long startTime = System.currentTimeMillis();
		
		// Multithread
		crawler[] crawlerList = new crawler[threadPool];
		for (int i = 0; i < crawlerList.length; i++) {
			crawlerList[i] = new crawler(total.get(i));
			crawlerList[i].start();
		}
		
		// Wait for all of them to complete
		for (crawler t : crawlerList) {
			t.join();
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("\nNumber of threads = " + threadPool);
		System.out.println("That took " + (endTime - startTime) + " milliseconds\n");
	}

}
