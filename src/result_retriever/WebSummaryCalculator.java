package result_retriever;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import app.Main;

public class WebSummaryCalculator implements Callable<Map<String, Map<String, Integer>>>{

	private Map<String, Future<Map<String, Integer>>> webResults;
	private List<String> checked;
	
	public WebSummaryCalculator(Map<String, Future<Map<String, Integer>>> webResults) {
		this.webResults = webResults;
		checked = new ArrayList<>();
	}
	
	@Override
	public Map<String, Map<String, Integer>> call() throws Exception {
		Map<String, Map<String, Integer>> toReturn = new HashMap<>();
		for (String url:webResults.keySet()) {
			if (checked.contains(url))
				continue;
			
			checked.add(url);
			
			String arr[];
			String baseUrl = null, domain = null;
			
			try {
				arr = url.split("/");
				baseUrl = arr[0] + "//" + arr[2];
				domain = arr[2];
			} catch (Exception e) {
				checked.remove(url);
				continue;
			}
			
			Map<String, Integer> map = webResults.get(url).get();
			
			if (map == null)
				continue;
			
			Map<String, Integer> result = new HashMap<>();
			for (String keyword:Main.keywords)
				result.put(keyword, 0);
			result.putAll(map);
			
			for (String sameDomain:webResults.keySet()) {
				if (!checked.contains(sameDomain)) {
					
					try {
						if (!sameDomain.split("/")[2].equals(domain))
							continue;
					} catch (Exception e) {
						continue;
					}
					
					checked.add(sameDomain);
					
					Map<String, Integer> map2 = webResults.get(sameDomain).get();
					
					if  (map2 == null || map2.isEmpty())
						continue;
					
					for (Entry e:map2.entrySet()) {
						if (result.putIfAbsent((String)e.getKey(), (Integer)e.getValue()) != null) {
							result.put((String)e.getKey(), result.get((String)e.getKey()) + (Integer)e.getValue());
						}
					}
	
				}
			}
			toReturn.put(domain, result);
		}
		
		for (String url:webResults.keySet()) {
			if (!checked.contains(url) && webResults.get(url).get() != null) {
				toReturn.put(url, webResults.get(url).get());
			}
		}

		return toReturn;
	}

}
