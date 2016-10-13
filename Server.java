import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.protocol.TBinaryProtocol;
import Service.Service;

public class Server {
	public static Handler handler;
	public static Service.Processor processor;
	
	public static void main(String [] args) {
		try {
			handler = new Handler();
			processor = new Service.Processor(handler);
			Runnable simple = new Runnable() {
				public void run() {
					simple(processor);
				}
			};

			new Thread(simple).start();
		}

		catch (Exception x) {
			x.printStackTrace();
		}
	}
			
	public static void simple(Service.Processor processor) {
		try {
			TServerTransport serverTransport = new TServerSocket(9090);
			TBinaryProtocol.Factory proFactory = new TBinaryProtocol.Factory();
			TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor).protocolFactory(proFactory));
			System.out.println("Starting the server...");
			server.serve();
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}
}