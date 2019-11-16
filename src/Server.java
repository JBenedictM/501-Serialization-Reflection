import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.jdom2.Document;

public class Server {
	
	public static void main(String[] args) {
		
		try {
			
			int port = 8888;
			ServerSocket serverSocket = new ServerSocket(port);
			Socket socket = null;
			while (true) {
				
				socket = serverSocket.accept();
				ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
			
				Object obj = inputStream.readObject();
			
				Document received_obj = (Document)obj;
				Serializer s = new Serializer();
				Object deserialized_obj = s.deserialize(received_obj);
			
				// show object reflection info
				Inspector ins = new Inspector();
				ins.inspect(deserialized_obj, true);
				
			}
			
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
