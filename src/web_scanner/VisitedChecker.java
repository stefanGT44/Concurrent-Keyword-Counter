package web_scanner;

import app.Main;

public class VisitedChecker implements Runnable{
	
	@Override
	public void run() {
		while (true) {
			long start = System.currentTimeMillis();
			while (System.currentTimeMillis() - start < Main.urlRefreshTime && Main.running) {
				//waiting
			}
			if (!Main.running)
				break;
			synchronized(WebScanner.visitedUrls) {
				WebScanner.visitedUrls.clear();
			}
			System.out.println("Visited url cache cleared");
		}
		System.out.println("Visited url cache checker stopped");
	}

}
