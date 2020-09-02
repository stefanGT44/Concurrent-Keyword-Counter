package file_scanner;

import java.util.concurrent.ForkJoinPool;

import job_queue.ScanningJob;

public class FileScanner extends ForkJoinPool{

	public FileScanner() {
	
	}
	
	public void submitJob(ScanningJob job) {
		this.submit(new InitiateJobTask(job));
	}
	
}
