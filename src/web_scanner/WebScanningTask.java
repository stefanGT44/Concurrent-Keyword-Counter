package web_scanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.RecursiveTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import app.Main;
import job_queue.WebJob;

public class WebScanningTask extends RecursiveTask<Map<String, Integer>> {

	private int hopCount;
	private String url;

	public WebScanningTask(String url, int hopCount) {
		this.url = url;
		this.hopCount = hopCount;
	}

	@Override
	protected Map<String, Integer> compute() {
		System.out.println("Starting web scan for web|"+url);
		Map<String, Integer> toReturn = new HashMap<>();

		Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (Exception e) {
			System.out.println("Unable to connect to:");
			System.out.println(url);
			return null;
		}
		
		if (doc.body() == null)
			return toReturn;
		
		StringTokenizer text = new StringTokenizer(doc.body().text());
		while (text.hasMoreTokens()) {
			String token = text.nextToken();
			if (Main.keywords.contains(token)) {
				if (toReturn.putIfAbsent(token, 1) != null) {
					toReturn.put(token, toReturn.get(token) + 1);
				}
			}
		}

		if (hopCount > 0) {
			Elements links = doc.select("a[href]");
			for (Element el : links) {
				String link = el.attr("abs:href");
				if (link.trim().equals("")) continue;
				boolean visited = false;
				synchronized(WebScanner.visitedUrls) {
					if (WebScanner.visitedUrls.contains(link))
						visited = true;
					else 
						WebScanner.visitedUrls.add(link);
				}
				if (!visited)
					Main.jobQueue.add(new WebJob(el.attr("abs:href"), hopCount - 1));
			}
		}
		return toReturn;
	}

}
