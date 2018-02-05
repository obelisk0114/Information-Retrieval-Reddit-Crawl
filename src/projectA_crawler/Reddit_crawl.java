package projectA_crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
	private String USER_AGENT = "Mozilla/5.0 (Windows NT 6.0; rv:2.0) Gecko/20100101 Firefox/4.0 Opera 12.14";
	private int agentIdx = 7;
	
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
			topic.add(prefix + topicURL + "?limit=500");
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
	
	void changeUserAgent() {
		String[] agentList = {"Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36", 
				"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36", 
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.0 Safari/537.36", 
				"Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2226.0 Safari/537.36", 
				"Mozilla/5.0 (Windows NT 6.4; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2225.0 Safari/537.36", 
				"Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.93 Safari/537.36", 
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.124 Safari/537.36", 
				"Mozilla/5.0 (Windows NT 6.0; rv:2.0) Gecko/20100101 Firefox/4.0 Opera 12.14", 
				"Opera/9.80 (X11; Linux i686; Ubuntu/14.10) Presto/2.12.388 Version/12.16", 
				"Opera/12.80 (Windows NT 5.1; U; en) Presto/2.10.289 Version/12.02", 
				"Mozilla/5.0 (Windows NT 5.1) Gecko/20100101 Firefox/14.0 Opera/12.0", 
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1", 
				"Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0", 
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20130401 Firefox/31.0", 
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20120101 Firefox/29.0", 
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/29.0", 
				"Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.8.1.8pre) Gecko/20070928 Firefox/2.0.0.7 Navigator/9.0RC1", 
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A", 
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.246", 
				"Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 7.0; InfoPath.3; .NET CLR 3.1.40767; Trident/6.0; en-IN)", 
				"Mozilla/5.0 (Windows; U; MSIE 9.0; Windows NT 9.0; en-US)", 
				"w3m/0.5.2 (Linux i686; it; Debian-3.0.6-3)", 
				"Lynx/2.8.8dev.3 libwww-FM/2.14 SSL-MM/1.4.1"};
		agentIdx = (int) (Math.random() * agentList.length);
		USER_AGENT = agentList[agentIdx];
	}
	
	void directoryCheck(String s) {
		String dirName = "data/" + s;
		File dir = new File(dirName);
		if (!dir.exists()) {
			dir.mkdir();
			System.out.println("Create new directory");
		}
		else {
			System.out.println("Already");
		}
	}
	
	void crawlPage(String link) {
		try {
			// create the output file and use 'UTF-8' encoding
			String[] sepLink = link.split("/");
			String fileName = "data/" + sepLink[4] + "/" + sepLink[sepLink.length - 2] + ".txt";
			File file = new File(fileName);
			if (file.exists()) {
				System.out.println("Exists: " + sepLink[sepLink.length - 2]);
				return;
			}
			
			Writer writer = new BufferedWriter(new OutputStreamWriter(
				    new FileOutputStream(file), "UTF-8"));
			
			writer.write(link + "\n\n\n");
			String content = getPageFromUrl(link);
			int pre = content.indexOf("data-author=");
			int post;
			String author;
			String date;
			String comments;
			while (pre != -1) {
				pre = content.indexOf("\"", pre + 1);
				post = content.indexOf("\"", pre + 1);
				author = content.substring(pre + 1, post);
				
				pre = content.indexOf("time title=", post + 1);
				pre = content.indexOf("\"", pre + 1);
				post = content.indexOf("\"", pre + 1);
				date = content.substring(pre + 1, post);
				
				writer.write(author + "     " + date + "\n\n");
				
				pre = content.indexOf("usertext-body may-blank-within md-container \" ><div class=\"md\">", post);
				if (pre == -1)
					break;
				pre = content.indexOf("p", pre + 1);
				post = content.indexOf("</div>", pre + 2);
				comments = content.substring(pre + 2, post);
				comments = comments.replaceAll("\\n|</p>|</blockquote>", "");
				comments = comments.replaceAll("<p>", "\n");
				
				writer.write(comments);
				writer.write("\n\n\n");
				pre = content.indexOf("data-author=", post + 1);
			}
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
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
		String target = "https://www.reddit.com/r/Showerthoughts/";
		String[] targetSeparate = target.split("/");
		test.directoryCheck(targetSeparate[4]);
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 13; i++) {
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
