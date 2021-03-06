package projectA_crawler_links;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.SocketTimeoutException;
import java.net.ProtocolException;
import java.net.UnknownHostException;

import java.util.List;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class Reddit_crawl_links {
	public static int DELAY = 1000;
	private String USER_AGENT = "Mozilla/5.0 (Windows NT 6.0; rv:2.0) Gecko/20100101 Firefox/4.0 Opera 12.14";
	private int agentIdx = 7;
	
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
	
	void directoryCheck(String s) {            // Create folder
		File dir = new File(s);
		if (!dir.exists()) {
			dir.mkdir();
			System.out.println("Create directory " + s);
		}
	}
	
	String getURLencode(String[] sep) {
		StringBuilder sb = new StringBuilder();
		sb.append("https:/");
		for (int i = 1; i < sep.length - 2; i++) {
			sb.append(sep[i] + "/");
		}
		try {
			sb.append(URLEncoder.encode(sep[sep.length - 2], "UTF-8"));
			sb.append("/" + sep[sep.length - 1]);
		} catch (IOException e) {
			e.printStackTrace();
			sb.append(sep[sep.length - 2]);
			sb.append("/" + sep[sep.length - 1]);
		}
		return sb.toString();
	}
	
	void crawlPage(String link) {
		// create the output file and use 'UTF-8' encoding
		String[] sepLink = link.split("/");
		String fileName = "data/" + sepLink[4] + "/" + sepLink[sepLink.length - 3]
				+ "-" + sepLink[sepLink.length - 2] + ".json";
		File file = new File(fileName);
		if (file.exists() && file.length() > 23) {     // Link is larger than 23 B.
			System.out.println("Exists: " + sepLink[sepLink.length - 3]
					+ "-" + sepLink[sepLink.length - 2]);
			return;
		}
		JSONArray allComments = new JSONArray();
		
		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter(
				    new FileOutputStream(file), "UTF-8"));
			
			JSONObject permalink = new JSONObject();
			permalink.put("permalink", link);
			
			allComments.put(permalink);
	        
			String content = getPageFromUrl(getURLencode(sepLink));
			
			// load more comments & continue this thread
			More_comments loadMore = new More_comments(content, writer, USER_AGENT);
			
			// Get and write title
			int pre = content.indexOf("<head><title");
			pre = content.indexOf(">", pre + 10);
			int post = content.indexOf(":", pre);
			String title = content.substring(pre + 1, post);
			JSONObject mainTitle = new JSONObject();
			mainTitle.put("title", title);
			
			allComments.put(mainTitle);
			
			pre = content.indexOf("data-author=", post);
			int nextAuthorPre;
			String author;
			String date;
			String comments;
			while (pre != -1) {
				JSONObject eachComments = new JSONObject();
				
				// Get author
				pre = content.indexOf("\"", pre + 1);
				post = content.indexOf("\"", pre + 1);
				author = content.substring(pre + 1, post);
				
				// Get time
				pre = content.indexOf("time title=", post + 1);
				pre = content.indexOf("\"", pre + 1);
				post = content.indexOf("\"", pre + 1);
				date = content.substring(pre + 1, post);
				
				eachComments.put("author", author);
				eachComments.put("date", date);
				
				// Get content
				pre = content.indexOf("usertext-body may-blank-within md-container", post);
				if (pre == -1) {
					eachComments.put("comments", "");
					allComments.put(eachComments);
					break;
				}
				
				// Check whether the poster only threw a link
				nextAuthorPre = content.indexOf("data-author=", post);
				if (nextAuthorPre != -1 && pre > nextAuthorPre) {
					eachComments.put("comments", "");
				}
				else {
					pre = content.indexOf("p>", pre + 1);
					post = content.indexOf("</div>", pre + 2);
					comments = content.substring(pre + 2, post);
					comments = comments.replaceAll("\\n|</p>|</blockquote>", "");
					comments = comments.replaceAll("<p>", "\n");   // Mark paragraph
					
					// HTML Name substitution
					// https://www.ascii.cl/htmlcodes.htm
					comments = comments.replaceAll("&#39;", "'");
					comments = comments.replaceAll("&amp;", "&");
					comments = comments.replaceAll("&lt;", "<");
					comments = comments.replaceAll("&gt;", ">");
					
					eachComments.put("comments", comments);					
				}
				
				pre = nextAuthorPre;
				allComments.put(eachComments);
			}
			writer.write(allComments.toString());
			writer.close();
		} catch (UnknownHostException e) {
			if (e.getMessage().equals("www.reddit.com")) {
				System.out.println("Unknown host: Reddit");
				System.out.println("Maybe your internet is out of work.");
			}
			System.out.println("\n" + link);
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			System.out.println("\n" + link);
			e.printStackTrace();
		} catch (ProtocolException e) {
			System.out.println("\n" + link);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getPageFromUrl(String link) throws IOException {
		URL thePage = new URL(link);
		URLConnection yc = thePage.openConnection();
		yc.setRequestProperty("User-Agent", USER_AGENT);
		yc.setConnectTimeout(5000);
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
		Reddit_crawl_links test = new Reddit_crawl_links();
		test.directoryCheck("data");
		
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
		
		String inputData = "seed.txt";
		try {
			switch (args.length) {
			case 0:
				break;
			case 1:
				inputData = args[0];
				break;
			default:
				System.out.println("\n    Invalid input parameters!"
						+ "\n    Usage: java -jar reddit.jar             # data = seed.txt"
						+ "\n    Usage: java -jar reddit.jar <s>     # data = s\n");
				System.exit(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		FileReader fr = new FileReader(inputData);
		BufferedReader br = new BufferedReader(fr);
		List<String> total = new ArrayList<String>();
		String line;
		while ((line = br.readLine()) != null) {
			total.add(line + "?limit=500");
		}
		br.close();
		
		long startTime = System.currentTimeMillis();
		for (String linkSeed : total) {
			String[] targetSeparate = linkSeed.split("/");
			String dirName = "data/" + targetSeparate[4];
			test.directoryCheck(dirName);        // Create directory when reads file
			test.crawlPage(linkSeed);
			//Thread.sleep(DELAY);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("That took " + (endTime - startTime) + " milliseconds");
	}

}
