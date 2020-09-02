package job_queue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import app.Main;
import file_scanner.ScanningTask;

public class TextFileJob implements ScanningJob {

	private File dir;

	public TextFileJob(File dir) {
		this.dir = dir;
	}

	@Override
	public ScanType getType() {
		return ScanType.FILE;
	}

	@Override
	public String getQuery() {
		return dir.getName();
	}

	@Override
	public Future<Map<String, Integer>> initiate() {

		// sortiranje niza fajlova od najveceg ka najmanjem
		File files[] = dir.listFiles();
		Arrays.sort(files, new Comparator<File>() {

			@Override
			public int compare(File file1, File file2) {
				if (file1.length() > file2.length())
					return -1;
				else if (file1.length() < file2.length())
					return 1;
				return 0;
			}

		});

		List<List<File>> threadJobs = new ArrayList<>();
		
		//System.out.println("job minimum size limit = " + Main.fileScanningSizeLimit);
		int jobCount = 1;
		
		int count = 0;
		while (count < files.length) {
			int tempSize = 0;
			List<File> fileBatch = new ArrayList<>();
			while (tempSize < Main.fileScanningSizeLimit && count < files.length) {
				//System.out.println(files[count].getName() + " size = " + files[count].length()+"bytes");
				fileBatch.add(files[count]);
				tempSize += files[count].length();
				count++;
			}
			//System.out.println(jobCount + ". job size = " + tempSize);
			threadJobs.add(fileBatch);
		}
		System.out.println("Starting file scan for file|" + dir.getName());
		return Main.fileScanner.submit(new ScanningTask(threadJobs));
	}
	
	
	public File getDir() {
		return dir;
	}

}
