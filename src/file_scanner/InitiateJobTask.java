package file_scanner;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveTask;

import app.Main;
import job_queue.ScanType;
import job_queue.ScanningJob;
import job_queue.TextFileJob;
import job_queue.WebJob;

public class InitiateJobTask extends RecursiveTask<Map<String, Integer>>{

	private ScanningJob scanningJob;
	
	public InitiateJobTask(ScanningJob scanningJob) {
		this.scanningJob = scanningJob;
	}
	
	@Override
	protected Map<String, Integer> compute() {
		Map<String, Integer> map = null;
		
		if (scanningJob.getType() == ScanType.FILE) {
			TextFileJob job = (TextFileJob)scanningJob;
			Main.resultRetriever.addFileCorpusResult(job.getDir().getName(), scanningJob.initiate());
		} else {
		
			WebJob job = (WebJob)scanningJob;
			Main.resultRetriever.addWebCorpusResult(job.getUrl(), scanningJob.initiate());
		
		}
		
		return map;
	}

}
