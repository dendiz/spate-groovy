package util;

class Log {
	static conf = new ConfigSlurper().parse(new File("botpit.properties").toURL())
	static info(msg) {
		_log("info",msg)
	}
	
	static debug(msg) {
		_log("debug", msg)
	}
	
	static _log(level, msg) {
		if (conf.logging == true)
			println new Date().toString() + " ${level}\t${msg}"
	}
}