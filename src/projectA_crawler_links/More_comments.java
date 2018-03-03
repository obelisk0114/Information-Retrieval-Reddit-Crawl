package projectA_crawler_links;

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
import java.util.LinkedList;

public class More_comments {
	private List<Integer> morechildrenStart;   // The beginning index of "load more comments"
	private int morechildrenIdx;               // Next beginning index of "load more comments"
	private List<String> morechildrenString;   // The information array of "load more comments"
	private LinkedList<String> childrenLinks;  // The links of all comments in "load more comments"
	
	private List<Integer> continueThreadStart; // The beginning index of "continue this thread"
	private int continueThreadIdx;             // Next beginning index of "continue this thread"
	private List<String> continueThreadString; // The link of "continue this thread"
	
	private String USER_AGENT;
	//private String prefix = "https://www.reddit.com";
	private String postLink;
	private Writer writer;
	
	public More_comments(String link, String content, Writer writer, String USER_AGENT) {
		morechildrenStart = new ArrayList<Integer>();
		morechildrenIdx = 0;
		morechildrenString = new ArrayList<String>();
		childrenLinks = new LinkedList<String>();
		
		continueThreadStart = new ArrayList<Integer>();
		continueThreadIdx = 0;
		continueThreadString = new ArrayList<String>();
		
		this.postLink = link;
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
			String[] sepContinue = continueUrl.split("/");
			
			continueThreadStart.add(pre + 1);
			continueThreadString.add(postLink + sepContinue[sepContinue.length - 1] + "/");
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
	
	public void retrieveUrl() {
		String[] children = morechildrenString.get(morechildrenIdx).split(", ");
		String idCollect = children[3];
		String[] idArray = (idCollect.substring(1, idCollect.length() - 1)).split(",");
		for (String s : idArray) {
			childrenLinks.add(postLink + s + "/");
		}
	}
	
	public void moreCommentsBranch(String fileName, String commentType) {
		int n;
		if (commentType.equals("continue this thread")) {
			n = 3;
			String link = continueThreadString.get(continueThreadIdx);
			writeMoreCommentsToFile(fileName, link, n);
			continueThreadIdx++;
		}
		else if (commentType.equals("load more comments")) {
			n = 2;
			retrieveUrl();
			for (String link : childrenLinks) {
				writeMoreCommentsToFile(fileName, link, n);
			}
			childrenLinks.clear();
			morechildrenIdx++;
		}
	}
	
	// Almost the same as crawlPage function in crawler
	public void writeMoreCommentsToFile(String fileName, String link, int n) {
		try {
			String contentContinue = getPageFromUrl2(link);
			More_comments nextLoad = new More_comments(postLink, contentContinue, writer, USER_AGENT);
			
			int pre = 0;
			for (int i = 0; i < n; i++) {
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
				pre = contentContinue.indexOf("usertext-body may-blank-within md-container", post);
				writer.write("\n\n");
				pre = contentContinue.indexOf("p>", pre + 1);
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
				
				// continue this thread & load more comments
				if (nextLoad.getContinueThreadStart() != -1 &&
						nextLoad.getMorechildrenStart() != -1) {
					if (nextLoad.getContinueThreadStart() > nextLoad.getMorechildrenStart()) {
						if (pre > nextLoad.getMorechildrenStart()) {
							nextLoad.moreCommentsBranch(fileName, "load more comments");
						}
					}
					else {  // getContinueThreadStart() < getMorechildrenStart()
						if (pre > nextLoad.getContinueThreadStart()) {
							nextLoad.moreCommentsBranch(fileName, "continue this thread");
						}
					}
				}
				else if (nextLoad.getContinueThreadStart() != -1 &&
						pre > nextLoad.getContinueThreadStart()) {
					nextLoad.moreCommentsBranch(fileName, "continue this thread");
				}
				else if (nextLoad.getMorechildrenStart() != -1 &&
						pre > nextLoad.getMorechildrenStart()) {
					nextLoad.moreCommentsBranch(fileName, "load more comments");
				}
			}
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
	
	public int getContinueThreadStart() {
		if (continueThreadStart.size() > continueThreadIdx)
			return continueThreadStart.get(continueThreadIdx);
		else
			return -1;
	}
	
	public int getMorechildrenStart() {
		if (morechildrenStart.size() > morechildrenIdx)
			return morechildrenStart.get(morechildrenIdx);
		else
			return -1;
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
