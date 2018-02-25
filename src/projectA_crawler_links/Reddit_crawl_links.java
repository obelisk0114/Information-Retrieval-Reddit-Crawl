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
	
	void directoryCheck(String s) {            // Create folder
		File dir = new File(s);
		if (!dir.exists()) {
			dir.mkdir();
			System.out.println("Create directory " + s);
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		Reddit_crawl_links test = new Reddit_crawl_links();
		test.directoryCheck("data");
		
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
		
		String inputData = "seed.txt";
		try {
			switch (args.length) {
			case 0:
				break;
			case 1:
				threadPool = Integer.parseInt(args[0]);
				break;
			case 2:
				inputData = args[0];
				threadPool = Integer.parseInt(args[1]);
				break;
			default:
				System.out.println("\n    Invalid input parameters!"
					+ "\n    Usage: java -jar reddit.jar             # data = seed.txt, threads = 10"
					+ "\n    Usage: java -jar reddit.jar <m>         # data = seed.txt, threads = m"
					+ "\n    Usage: java -jar reddit.jar <s> <m>     # data = s, threads = m\n");
				System.exit(1);
			}
		} catch (NumberFormatException ne) {
			System.out.println("Number of threads must be integer.\n");
			ne.printStackTrace();
			System.exit(1);
		}
		
		FileReader fr = new FileReader(inputData);
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
			String dirName = "data/" + targetSeparate[4];
			test.directoryCheck(dirName);        // Create directory when reads file
			total.get(threadCounter).add(line + "?limit=500");   // Get 500 comments
			threadCounter = (threadCounter + 1) % threadPool;
		}
		br.close();
		
		long startTime = System.currentTimeMillis();
		
		// Multithread
		crawler[] crawlerList = new crawler[threadPool];
		for (int i = 0; i < crawlerList.length; i++) {
			crawlerList[i] = new crawler(total.get(i));
			crawlerList[i].setName("crawler " + i);
			crawlerList[i].start();
		}
		
		// Wait for all of them to complete
		try {
			for (crawler t : crawlerList) {
				t.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("\nNumber of threads = " + threadPool);
		System.out.println("That took " + (endTime - startTime) + " milliseconds\n");
	}

}
