package result_retriever;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class DomenResultCalculator implements Callable<Map<String, Map<String, Integer>>>{

	private String query;
	private Map<String, Future<Map<String, Integer>>> webResults;
	
	public DomenResultCalculator(String query, Map<String, Future<Map<String, Integer>>> webResults) {
		this.query = query;
		this.webResults = webResults;
	}
	
	@Override
	public Map<String, Map<String, Integer>> call() throws Exception {
		Map<String, Map<String, Integer>> temp = new HashMap<>();
		for (String corpusName:webResults.keySet()) {
			if (corpusName.startsWith(query)) {
				temp.put(corpusName, webResults.get(corpusName).get());
			}
		}
		
		Map<String, Map<String, Integer>> toReturn = new HashMap<>();
		
		if (temp.isEmpty()) {
			System.out.println("Url not recognized.");
			toReturn.put("map", null);
			return toReturn;
		}
		
		Map<String, Integer> map = new HashMap<>();
		for (String corpusName:temp.keySet()) {
			for (Entry e:temp.get(corpusName).entrySet()) {
				if (map.putIfAbsent((String)e.getKey(), (Integer)e.getValue()) != null) {
					map.put((String)e.getKey(), map.get((String)e.getKey()) + (Integer)e.getValue());
				}
			}
		}
		toReturn.put("map", map);
		return toReturn;
	}

}
