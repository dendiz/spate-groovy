package profile;
import core.Botpit
class ProfileRunner {
	public static void main(args) {
		def proxy = ProxyMetaClass.getInstance(Botpit)
		proxy.interceptor = new BotpitInterceptor()
		proxy.use {
			def bp = new Botpit()
			bp.run()
		}
	}
}