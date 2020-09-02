package job_queue;

import java.util.Map;
import java.util.concurrent.Future;

public class StopJob implements ScanningJob{

	@Override
	public ScanType getType() {
		return ScanType.STOP;
	}

	@Override
	public String getQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<Map<String, Integer>> initiate() {
		// TODO Auto-generated method stub
		return null;
	}

}
