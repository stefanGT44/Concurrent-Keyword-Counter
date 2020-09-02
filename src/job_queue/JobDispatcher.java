package job_queue;

import app.Main;
import file_scanner.FileScanner;
import web_scanner.WebScanner;

public class JobDispatcher implements Runnable{
	
	private FileScanner fileScanner;
	private WebScanner webScanner;
	
	public JobDispatcher(FileScanner fileScanner, WebScanner webScanner) {
		this.fileScanner = fileScanner;
		this.webScanner = webScanner;
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				ScanningJob job = Main.jobQueue.take();
				//System.out.println("[JobDispatcher]: submitting job");
				if (job.getType().equals(ScanType.FILE))
					fileScanner.submitJob(job);
				else if (job.getType().equals(ScanType.WEB))
					webScanner.submitJob(job);
				else
					break;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("JobDispatcher stopped");
	}

}
