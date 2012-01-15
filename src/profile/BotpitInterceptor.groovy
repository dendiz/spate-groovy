package profile;

class BotpitInterceptor implements Interceptor {
	long t
	Object beforeInvoke(Object object, String methodName, Object[] args) {
		t = System.currentTimeMillis()
		null
	}
	
	Object afterInvoke(Object object, String methodName, Object[] args, Object result) {
		def delta = System.currentTimeMillis() - t
		if (delta > 5)
		println "$methodName: ${delta}"
		
		result
	}
	
	boolean doInvoke() { true }
}

