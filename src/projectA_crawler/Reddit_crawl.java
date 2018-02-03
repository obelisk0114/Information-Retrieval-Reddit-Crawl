package projectA_crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import java.util.List;
import java.util.ArrayList;

public class Reddit_crawl {
	public static int DELAY = 1000;
	private static List<String> topic = new ArrayList<String>();
	private static String prefix = "https://www.reddit.com";
	private String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";
	
	String addTopic(String link) throws IOException {
		String content = getPageFromUrl(link);
		int pre = content.indexOf("data-permalink=");
		pre = content.indexOf("\"", pre);
		int post = content.indexOf("\"", pre + 1);
		for (int i = 0; i < 25; i++) {
			if (pre == -1) {
				System.out.println("Hit the end.");
				break;
			}
			
			String topicURL = content.substring(pre + 1, post);
			topic.add(prefix + topicURL);
			if (i < 24) {
				pre = content.indexOf("data-permalink=", post);
				pre = content.indexOf("\"", pre);
				post = content.indexOf("\"", pre + 1);				
			}
		}
		pre = content.indexOf("class=\"next-button\"><a href=", post);
		pre = content.indexOf("\"", pre + 20);
		post = content.indexOf("\"", pre + 1);
		
		if (pre == -1) {
			return "";
		}
		String next = content.substring(pre + 1, post);
		return next;
	}
	
	void crawlPage(String link) {
		try {
			// create the output file and use 'UTF-8' encoding
			String[] sepLink = link.split("/");
			String fileName = "data/" + sepLink[sepLink.length - 1] + ".txt";
			Writer writer = new BufferedWriter(new OutputStreamWriter(
				    new FileOutputStream(fileName), "UTF-8"));
			
			String content = getPageFromUrl(link);
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void printTopicURL() {
		for (String s : topic) {
			System.out.println(s);
		}
	}
	
	public String getPageFromUrl(String link) throws IOException {
		URL thePage = new URL(link);
		URLConnection yc = thePage.openConnection();
		yc.setRequestProperty("User-Agent", USER_AGENT);
		yc.setConnectTimeout(1000);
		// Change encoding to 'UTF-8'
		BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), "UTF-8"));
		String inputLine;
		String output = "";
		while ((inputLine = in.readLine()) != null) {
			output += inputLine + "\n";
		}
		in.close();
		return output;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		Reddit_crawl test = new Reddit_crawl();
		String target = "https://www.reddit.com/r/Showerthoughts/";
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 6; i++) {
			target = test.addTopic(target);
			if (target.equals("")) {
				System.out.println("No next page.");
				break;
			}
			
			System.out.println("\nPage " + i);
			//test.printTopicURL();
			for (String element : topic) {
				test.crawlPage(element);
			}
			topic.clear();
			//Thread.sleep(DELAY);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("That took " + (endTime - startTime) + " milliseconds");
	}
}
