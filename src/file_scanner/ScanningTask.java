package file_scanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import app.Main;

public class ScanningTask extends RecursiveTask<Map<String, Integer>>{

	private List<List<File>> threadJobs;
	
	public ScanningTask(List<List<File>> threadJobs) {
		this.threadJobs = threadJobs;
	}
	
	@Override
	protected Map<String, Integer> compute() {
		Map<String, Integer> toReturn = new HashMap<>();
		if (threadJobs.size() == 1) {
			List<File> files = threadJobs.get(0);
			for (File file:files) {
				try {
					Scanner s = new Scanner(file);
					while (s.hasNext()) {
						String token = s.next();
						if (Main.keywords.contains(token)) {
							if (toReturn.putIfAbsent(token, 1) != null) {
								toReturn.put(token, toReturn.get(token) + 1);
							}
						}
					}
					s.close();
				} catch (FileNotFoundException e) {
					System.out.println("Can't open file: " + file.getName());
				}
			}
			
		} else {
			List<List<File>> firstHalf = threadJobs.subList(0, threadJobs.size()/2);
			List<List<File>> secondHalf = threadJobs.subList(threadJobs.size()/2, threadJobs.size());
			
			
			ForkJoinTask<Map<String, Integer>> forkTask = new ScanningTask(firstHalf);
			forkTask.fork();
			
			ScanningTask callTask = new ScanningTask(secondHalf);
			Map<String, Integer> forkResult = callTask.compute();
			
			Map<String, Integer> callResult = forkTask.join();
			
			toReturn = forkResult;
			for (String keyword:callResult.keySet()) {
				if (toReturn.putIfAbsent(keyword, callResult.get(keyword)) != null){
					toReturn.put(keyword, toReturn.get(keyword) + callResult.get(keyword));
				}
			}
		}
		return toReturn;
	}

}
