package directory_crawler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.Main;
import job_queue.TextFileJob;

public class DirectoryCrawler implements Runnable{

	private List<String> directories;
	private Map<String, Long> changes;
	private long waitTime;
	private String fileCorpusPrefix;
	
	public DirectoryCrawler(long waitTime, String fileCorpusPrefix) {
		directories = Collections.synchronizedList(new ArrayList<String>());
		this.waitTime = waitTime;
		this.fileCorpusPrefix = fileCorpusPrefix;
		changes = new HashMap<>();
	}
	
	@Override
	public void run() {
		while (true) {
			findCorpuses();
			
			//waiting for next cycle
			long startWait = System.currentTimeMillis();
			while (System.currentTimeMillis() - startWait < waitTime && Main.running) {
			}
			if (!Main.running)
				break;
		}
		System.out.println("DirectoryCrawler stopped");
	}
	
	private void findCorpuses() {
		for (String dirName:directories) {
			File file = new File(dirName);
			if (file.exists() && file.isDirectory()) {
				if (file.getName().startsWith(fileCorpusPrefix)) {
					createJob(file);
				} else {
					recursiveCalls(file);
				}
			} else {
				System.out.println("[DirectoryCrawler]: file "+ dirName +" does not exist or is not a directory");
			}
		}
	}
	
	private void recursiveCalls(File file) {
		if (!file.isDirectory()) return;
		File files[] = file.listFiles();
		for (File f:files) {
			if (f.isDirectory()) {
				if (f.getName().startsWith(fileCorpusPrefix))
					createJob(f);
				else
					recursiveCalls(f);
			}
		}
	}
	
	private void createJob(File file) {
		boolean changed = false;
		File files[] = file.listFiles();
		for (File fileToScan:files) {
			if (fileToScan.getName().endsWith(".txt")){
				Long lastModified = changes.get(fileToScan.getAbsolutePath());
				if (lastModified == null || lastModified != fileToScan.lastModified()) {
					changes.put(fileToScan.getAbsolutePath(), fileToScan.lastModified());
					changed = true;
				}
			}
		}
		if (changed) {
			//System.out.println("Creating job for " + file.getName());
			try {
				Main.jobQueue.put(new TextFileJob(file));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void addDirectory(String dirName) {
		File f = new File(dirName);
		if (f.exists()) {
			System.out.println("Adding dir " + f.getAbsolutePath());
			directories.add(dirName);
		} else {
			System.out.println("Directory " + dirName + "does not exist");
		}
	}
	
}
