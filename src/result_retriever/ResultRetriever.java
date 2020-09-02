package result_retriever;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import job_queue.ScanType;

public class ResultRetriever implements IResultRetriever {

	private Map<String, Future<Map<String, Integer>>> fileResults;
	private Map<String, Future<Map<String, Integer>>> fileResultsCopy;

	private Map<String, Future<Map<String, Integer>>> webResults;
	private Map<String, Future<Map<String, Integer>>> webResultsCopy;

	private Map<String, Map<String, Integer>> domenResults;

	private int fileResultsHash = 0;
	private int webResultsHash = 0;

	private Map<String, Map<String, Integer>> fileSummary;
	private Map<String, Map<String, Integer>> webSummary;

	public ExecutorService threadPool;
	private CompletionService<Map<String, Map<String, Integer>>> service;

	public ResultRetriever() {
		fileResults = Collections.synchronizedMap(new HashMap<String, Future<Map<String, Integer>>>());
		fileResultsCopy = new HashMap<>();

		webResults = Collections.synchronizedMap(new HashMap<String, Future<Map<String, Integer>>>());
		webResultsCopy = new HashMap<>();

		domenResults = new HashMap<>();

		this.threadPool = Executors.newCachedThreadPool();
		service = new ExecutorCompletionService<>(threadPool);
	}

	@Override
	public Map<String, Integer> getFileResult(String query) {
		try {
			if (!fileResults.containsKey(query)) {
				System.out.println("Corpus " + query + "doesn't exist");
				return null;
			}
			return fileResults.get(query).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Map<String, Integer> getWebResult(String query) {
		if (domenResults.containsKey(query)) {
			return domenResults.get(query);
		}
		synchronized (webResults) {
			webResultsCopy.putAll(webResults);
		}
		try {
			Map<String, Integer> toReturn = service.submit(new DomenResultCalculator(query, webResultsCopy)).get()
					.get("map");
			
			if (toReturn == null)
				return null;
			
			domenResults.put(query, toReturn);
			return toReturn;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Map<String, Integer> queryFileResult(String query) {
		if (!fileResults.containsKey(query)) {
			System.out.println("Corpus " + query + " doesn't exist");
			return null;
		}
		if (fileResults.get(query).isDone())
			try {
				return fileResults.get(query).get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		return null;
	}

	@Override
	public Map<String, Integer> queryWebResult(String query) {
		if (domenResults.containsKey(query)) {
			return domenResults.get(query);
		}
		synchronized (webResults) {
			webResultsCopy.putAll(webResults);
		}
		for (String url : webResultsCopy.keySet()) {
			if (url.startsWith(query))
				if (!webResultsCopy.get(url).isDone()) {
					System.out.println("Job for " + query + " is still in progress");
					return null;
				}
		}
		Map<String, Integer> toReturn = null;
		try {
			toReturn = service.submit(new DomenResultCalculator(query, webResultsCopy)).get().get("map");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		domenResults.put(query, toReturn);
		return toReturn;
	}
	
	@Override
	public void deleteWebDomen(String query) {
		if (domenResults.containsKey(query)) {
			domenResults.remove(query);
			System.out.println("Domain deleted");
		} else {
			System.out.println("Domain doesn't exist");
		}
	}

	@Override
	public void clearSummary(ScanType summaryType) {
		if (summaryType == ScanType.FILE) {
			if (fileSummary != null) {
				fileSummary = null;
				fileResultsHash = 0;
				System.out.println("File summary deleted.");
			} else {
				System.out.println("Summary already empty");
			}
		} else {
			if (webSummary != null) {
				webSummary = null;
				webResultsHash = 0;
				System.out.println("Web summary deleted.");
			} else {
				System.out.println("Summary already empty");
			}
		}
	}

	@Override
	public Map<String, Map<String, Integer>> getSummary(ScanType summaryType) {
		if (summaryType == ScanType.FILE) {
			try {
				boolean test = false;
				Future<Map<String, Map<String, Integer>>> job = null;
				fileResultsCopy.clear();
				synchronized (fileResults) {
					if (fileSummary == null || fileResultsHash != fileResults.hashCode()) {
						test = true;
						fileResultsHash = fileResults.hashCode();
						fileResultsCopy.putAll(fileResults);
					}
				}
				if (test) {
					job = service.submit(new FileSummaryCalculator(fileResultsCopy));
					fileSummary = job.get();
				}
				return fileSummary;
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		} else {
			boolean test = false;
			Future<Map<String, Map<String, Integer>>> job = null;
			webResultsCopy.clear();
			synchronized (webResults) {
				if (webSummary == null || webResultsHash != webResults.hashCode()) {
					test = true;
					webResultsHash = webResults.hashCode();
					webResultsCopy.putAll(webResults);
				}
			}
			if (test) {
				job = service.submit(new WebSummaryCalculator(webResultsCopy));
				try {
					webSummary = job.get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			return webSummary;
		}
		return null;
	}

	@Override
	public Map<String, Map<String, Integer>> querySummary(ScanType summaryType) {
		if (summaryType == ScanType.FILE) {
			fileResultsCopy.clear();
			int tempHashCode = 0;
			synchronized (fileResults) {
				tempHashCode = fileResults.hashCode();
				if (fileSummary != null && tempHashCode == fileResultsHash)
					return fileSummary;
				for (String corpusName : fileResults.keySet()) {
					fileResultsCopy.put(corpusName, fileResults.get(corpusName));
					if (!fileResults.get(corpusName).isDone()) {
						System.out.println("Summary is not ready yet");
						return null;
					}
				}
			}
			if (fileSummary == null || fileResultsHash != tempHashCode) {
				fileResultsHash = tempHashCode;
				try {
					fileSummary = service.submit(new FileSummaryCalculator(fileResultsCopy)).get();
					return fileSummary;
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		} else {
			webResultsCopy.clear();
			int tempHashCode = 0;
			synchronized (webResults) {
				tempHashCode = webResults.hashCode();
				if (webSummary != null && tempHashCode == webResultsHash)
					return webSummary;
				for (String corpusName : webResults.keySet()) {
					webResultsCopy.put(corpusName, webResults.get(corpusName));
					if (!webResults.get(corpusName).isDone()) {
						System.out.println("Summary is not ready yet");
						return null;
					}
				}
			}
			if (webSummary == null || webResultsHash != tempHashCode) {
				webResultsHash = tempHashCode;
				try {
					webSummary = service.submit(new WebSummaryCalculator(webResultsCopy)).get();
					return webSummary;
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@Override
	public void addFileCorpusResult(String corpusName, Future<Map<String, Integer>> corpusResult) {
		fileResults.put(corpusName, corpusResult);
	}

	@Override
	public void addWebCorpusResult(String corpusName, Future<Map<String, Integer>> corpusResult) {
		webResults.put(corpusName, corpusResult);
	}

}
