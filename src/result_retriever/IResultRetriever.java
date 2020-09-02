package result_retriever;

import java.util.Map;
import java.util.concurrent.Future;

import job_queue.ScanType;

public interface IResultRetriever {
	
	public Map<String, Integer> getFileResult(String query);
	
	public Map<String, Integer> getWebResult(String query);
	
	public Map<String, Integer> queryFileResult(String query);
	
	public Map<String, Integer> queryWebResult(String query);
	
	public void deleteWebDomen(String query);
	
	public void clearSummary(ScanType summaryType);
	
	public Map<String, Map<String, Integer>> getSummary(ScanType summaryType);
	
	public Map<String, Map<String, Integer>> querySummary(ScanType summaryType);
	
	public void addFileCorpusResult(String corpusName, Future<Map<String, Integer>> corpusResult);
	
	public void addWebCorpusResult(String corpusName, Future<Map<String, Integer>> corpusResult);

}
