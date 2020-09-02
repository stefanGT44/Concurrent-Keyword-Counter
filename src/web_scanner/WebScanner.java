package web_scanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import file_scanner.InitiateJobTask;
import job_queue.ScanningJob;

public class WebScanner extends ForkJoinPool{
	
	public static List<String> visitedUrls = Collections.synchronizedList(new ArrayList<>());

	public WebScanner() {
		new Thread(new VisitedChecker()).start();
	}
	
	//synchronized
	public void submitJob(ScanningJob job) {
		this.submit(new InitiateJobTask(job));
		//System.out.println("[WebScanner]: JobSubmited");
	}
	
}
