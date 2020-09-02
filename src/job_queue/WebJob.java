package job_queue;

import java.util.Map;
import java.util.concurrent.Future;

import app.Main;
import web_scanner.WebScanningTask;

public class WebJob implements ScanningJob{
	
	private String url;
	private int hopCount;
	
	public WebJob(String url, int hopCount) {
		this.url = url;
		this.hopCount = hopCount;
		
		//System.out.println("WebJob: url" + url + " hopCount: " + hopCount);
	}

	@Override
	public ScanType getType() {
		return ScanType.WEB;
	}

	@Override
	public String getQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<Map<String, Integer>> initiate() {
		return Main.webScanner.submit(new WebScanningTask(url, hopCount));
	}
	
	public String getUrl() {
		return url;
	}

}
