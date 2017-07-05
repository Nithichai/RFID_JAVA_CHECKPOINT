

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Logfile extends Thread {
	
	private Logger logger = Logger.getLogger("MyLog");
	
	public void run() {
	    try {
	    	FileHandler fh = new FileHandler(System.getProperty("user.dir") + "/info.log");  
	        logger.addHandler(fh);
	        SimpleFormatter formatter = new SimpleFormatter();
	        fh.setFormatter(formatter);
	        fh.close();
	    } catch (SecurityException e) {  
	        e.printStackTrace();  
	    } catch (IOException e) {  
	        e.printStackTrace();
	    }
	}
	
	public void info(String data) {
		logger.info(data);
	}
	
	public void warning(String data) {
		logger.warning(data);
	}
}
