package app;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import directory_crawler.DirectoryCrawler;
import file_scanner.FileScanner;
import job_queue.JobDispatcher;
import job_queue.ScanType;
import job_queue.ScanningJob;
import job_queue.StopJob;
import job_queue.WebJob;
import result_retriever.ResultRetriever;
import web_scanner.WebScanner;

public class Main {
	
	//property file parametri
	public static List<String> keywords = Collections.synchronizedList(new ArrayList<String>());
	public static String fileCorpusPrefix = "";
	public static int dirCrawlerSleepTime;
	public static int fileScanningSizeLimit;
	public static int hopCount;
	public static long urlRefreshTime;
	
	//komponente
	private static DirectoryCrawler directoryCrawler;
	public static LinkedBlockingQueue<ScanningJob> jobQueue;
	private static JobDispatcher jobDispatcher;
	public static FileScanner fileScanner;
	public static WebScanner webScanner;
	public static ResultRetriever resultRetriever;
	
	//za debagovanje
	public static AtomicInteger threadCounter = new AtomicInteger(0);
	public static AtomicInteger webThreadCounter = new AtomicInteger(0);
	
	public static volatile boolean running;
	
	public static void main(String[] args) {
		running = true;
		readPropertiesFile();
		jobQueue = new LinkedBlockingQueue<>();
		fileScanner = new FileScanner();
		webScanner = new WebScanner();
		resultRetriever = new ResultRetriever();
		startJobDispatcher();
		startDirectoryCrawler();
		cli();
	}
	
	private static void cli() {
		Scanner s = new Scanner(System.in);
		String line = "";
		System.out.println("Command line interface started");
		System.out.println("Enter a command");
		while (true) {
			line = s.nextLine();
			
			if(line.startsWith("ad ")) {
				String dirName = line.substring(3, line.length());
				directoryCrawler.addDirectory(dirName);
				continue;
			}
			
			if (line.startsWith("aw ")) {
				String url = line.substring(3, line.length());
				try {
					jobQueue.put(new WebJob(url, hopCount));
					continue;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if (line.startsWith("get ")) {
				line = line.substring(4);
				if (line.startsWith("file|")) {
					line = line.substring(5);
					if (line.equals("summary")) {
						printSummary(resultRetriever.getSummary(ScanType.FILE));
					} else
						printMap(resultRetriever.getFileResult(line));
				} else if (line.startsWith("web|")) {
					line = line.substring(4);
					if (line.trim().equals(""))
						System.out.println("Invalid command");
					else if (line.equals("summary")) {
						printSummary(resultRetriever.getSummary(ScanType.WEB));
					} else
						printMap(resultRetriever.getWebResult(line));
				} else {
					System.out.println("Invalid command");
				}
				continue;
			}
			
			if (line.startsWith("query ")) {
				line = line.substring(6);
				if (line.startsWith("file|")) {
					line = line.substring(5);
					if (line.equals("summary")) {
						printSummary(resultRetriever.querySummary(ScanType.FILE));
					} else
						printMap(resultRetriever.queryFileResult(line));
				} else if (line.startsWith("web|")){
					line = line.substring(4);
					if (line.trim().equals(""))
						System.out.println("Invalid command");
					else if (line.equals("summary")) {
						printSummary(resultRetriever.querySummary(ScanType.WEB));
					} else
						printMap(resultRetriever.queryWebResult(line));
				} else {
					System.out.println("Invalid command");
				}
				continue;
			}
			
			if (line.startsWith("delete web|")) {
				line = line.substring(11);
				resultRetriever.deleteWebDomen(line);
				continue;
			}
			
			if (line.equals("cws")) {
				resultRetriever.clearSummary(ScanType.WEB);
				continue;
			}
			
			if (line.equals("cfs")) {
				resultRetriever.clearSummary(ScanType.FILE);
				continue;
			}
			
			if (line.equals("stop")) {
				System.out.println("Stopping...");
				try {
					jobQueue.put(new StopJob());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				fileScanner.shutdown();
				webScanner.shutdown();
				resultRetriever.threadPool.shutdown();
				running = false;
				break;
			}
			
			System.out.println("Invalid command");
		}
		System.out.println("Cli stopped");
	}
	
	private static void printSummary(Map<String, Map<String, Integer>> map) {
		if (map == null) return;
		if (map.isEmpty()) {
			System.out.println("No corpuses added.");
		}
		for (String corpusName:map.keySet()) {
			System.out.print(corpusName + ": ");
			printMap(map.get(corpusName));
		}
	}
	
	private static void printMap(Map<String, Integer> map) {
		if (map == null) return;
		StringBuilder sb = new StringBuilder("{") ;
		for (String keyword:map.keySet()) {
			sb.append(keyword + "=" + map.get(keyword) + ", ");
		}
		try {
			sb.delete(sb.length()-2, sb.length());
			sb.append("}");
			System.out.println(sb.toString());
		} catch (Exception e) {
			System.out.println("No words found!");
		}
	}
	
	private static void startDirectoryCrawler() {
		directoryCrawler = new DirectoryCrawler(dirCrawlerSleepTime, fileCorpusPrefix);
		Thread thread = new Thread(directoryCrawler);
		thread.start();
		System.out.println("DirectoryCrawler started");
	}
	
	private static void startJobDispatcher() {
		jobDispatcher = new JobDispatcher(fileScanner, webScanner);
		Thread thread = new Thread(jobDispatcher);
		thread.start();
		System.out.println("JobDispatcher started");
	}
	
	private static void readPropertiesFile() {
		try {
			Scanner s = new Scanner(new File("app.properties.txt"));
			String line = s.nextLine();
			line = line.substring(9, line.length());
			keywords.addAll(Arrays.asList(line.split(", ")));
			
			line = s.nextLine();
			fileCorpusPrefix = line.substring(19, line.length());
			
			line = s.nextLine();
			dirCrawlerSleepTime = Integer.parseInt(line.split("=")[1]);
			
			line = s.nextLine();
			fileScanningSizeLimit = Integer.parseInt(line.split("=")[1]);
			
			line = s.nextLine();
			hopCount = Integer.parseInt(line.split("=")[1]);
			
			line = s.nextLine();
			urlRefreshTime = Long.parseLong(line.split("=")[1]);
			
			s.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
