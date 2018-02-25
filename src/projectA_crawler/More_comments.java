package projectA_crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;

import java.net.URL;
import java.net.URLConnection;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;

import java.util.List;
import java.util.ArrayList;

public class More_comments {
	private List<Integer> morechildrenStart;
	private int morechildrenIdx;
	private List<String> morechildrenString;
	
	private List<Integer> continueThreadStart;
	private int continueThreadIdx;
	private List<String> continueThreadString;
	
	private String USER_AGENT;
	private String prefix = "https://www.reddit.com";
	private Writer writer;
	
	public More_comments(String content, Writer writer, String USER_AGENT) {
		morechildrenStart = new ArrayList<Integer>();
		morechildrenIdx = 0;
		morechildrenString = new ArrayList<String>();
		
		continueThreadStart = new ArrayList<Integer>();
		continueThreadIdx = 0;
		continueThreadString = new ArrayList<String>();
		
		this.USER_AGENT = USER_AGENT;
		this.writer = writer;
		findContinueThread(content);
		findChildren(content);
	}
	
	public void findContinueThread(String content) {
		int start = 0;
		int pre = content.indexOf("continue this thread", start);
		int post = 0;
		int store = pre + 1;
		while (pre != -1) {
			pre = content.lastIndexOf("a href=", pre);
			pre = content.indexOf("\"", pre);
			post = content.indexOf("\"", pre + 1);
			String continueUrl = content.substring(pre + 1, post);
			
			continueThreadStart.add(pre + 1);
			continueThreadString.add(prefix + continueUrl);
			pre = content.indexOf("continue this thread", store);
			store = pre + 1;
		}
	}
	
	public void findChildren(String content) {
		int start = 0;
		int pre = content.indexOf("load more comments", start);
		int post = 0;
		int store = pre + 1;
		while (pre != -1) {
			pre = content.lastIndexOf("return morechildren", pre);
			pre = content.indexOf("(", pre);
			post = content.indexOf(")", pre + 1);
			String loadChildren = content.substring(pre, post + 1);
			
			morechildrenStart.add(pre);
			morechildrenString.add(loadChildren);
			pre = content.indexOf("load more comments", store);
			store = pre + 1;
		}
	}
	
	// Almost the same as crawlPage function in crawler
	public void writeContinueThreadToFile(String fileName) {
		String link = continueThreadString.get(continueThreadIdx);
		try {
			String contentContinue = getPageFromUrl2(link);
			
			int pre = 0;
			for (int i = 0; i < 3; i++) {
				pre = contentContinue.indexOf("data-author=", pre + 1);
			}
			int post = pre;
			
			while (pre != -1) {
				// Get author
				pre = contentContinue.indexOf("\"", pre + 1);
				post = contentContinue.indexOf("\"", pre + 1);
				String author = contentContinue.substring(pre + 1, post);
				
				// Get time
				pre = contentContinue.indexOf("time title=", post + 1);
				pre = contentContinue.indexOf("\"", pre + 1);
				post = contentContinue.indexOf("\"", pre + 1);
				String date = contentContinue.substring(pre + 1, post);
				
				writer.write(author + "\n" + date);
				
				// Get content
				pre = contentContinue.indexOf("usertext-body may-blank-within md-container \" ><div class=\"md\">", post);
				writer.write("\n\n");
				pre = contentContinue.indexOf("p", pre + 1);
				post = contentContinue.indexOf("</div>", pre + 2);
				String comments = contentContinue.substring(pre + 2, post);
				
				comments = comments.replaceAll("\\n|</p>|</blockquote>", "");
				comments = comments.replaceAll("<p>", "\n");   // Mark paragraph
				
				// HTML Name substitution
				// https://www.ascii.cl/htmlcodes.htm
				comments = comments.replaceAll("&#39;", "'");
				comments = comments.replaceAll("&amp;", "&");
				comments = comments.replaceAll("&lt;", "<");
				comments = comments.replaceAll("&gt;", ">");
				
				writer.write(comments);
				writer.write("\n\n\n");
				
				pre = contentContinue.indexOf("data-author=", post);
			}
			continueThreadIdx++;
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
	
	public Integer getContinueThreadStart() {
		if (continueThreadStart.size() > continueThreadIdx)
			return continueThreadStart.get(continueThreadIdx);
		else
			return null;
	}
	
	public Integer getMorechildrenStart() {
		if (morechildrenStart.size() > morechildrenIdx)
			return morechildrenStart.get(morechildrenIdx);
		else
			return null;
	}
	
	private String getPageFromUrl2(String link) throws IOException {
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

}
