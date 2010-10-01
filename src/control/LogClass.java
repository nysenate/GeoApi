package control;

import org.apache.log4j.Logger;

public class LogClass {
	private static org.apache.log4j.Logger log = Logger.getLogger(LogClass.class);
	
	public static void main(String[] args) {
		log.trace("Trace");
		log.debug("Debug");
		log.info("info");
		log.warn("warn");
		log.error("Error");
		log.fatal("Fatal");
	}
}
