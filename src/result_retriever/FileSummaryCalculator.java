package result_retriever;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class FileSummaryCalculator implements Callable<Map<String, Map<String, Integer>>>{

	private Map<String, Future<Map<String, Integer>>> fileResults;
	
	public FileSummaryCalculator(Map<String, Future<Map<String, Integer>>> fileResults) {
		this.fileResults = fileResults;
	}
	
	@Override
	public Map<String, Map<String, Integer>> call() throws Exception {
		Map<String, Map<String, Integer>> toReturn = new HashMap<>();
		for (String corpusName:fileResults.keySet()) {
			toReturn.put(corpusName, fileResults.get(corpusName).get());
		}
		return toReturn;
	}

}
