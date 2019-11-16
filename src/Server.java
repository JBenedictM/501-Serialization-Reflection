import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class Server {
	
	public static void main(String[] args) {
		
		try {
			
			int port = 4444;
			ServerSocket serverSocket = new ServerSocket(port);
			Socket socket = null;
			while (true) {
				
				socket = serverSocket.accept();
				ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
			
				Object obj = inputStream.readObject();
			
				Document received_obj = (Document)obj;
				// save as an xml document
				XMLOutputter xmlOutput = new XMLOutputter();
	            xmlOutput.setFormat(Format.getPrettyFormat());
	            String server_file = "Server_File.xml";
	            xmlOutput.output(received_obj, new FileWriter(server_file));
	            
				Serializer s = new Serializer();
				Object deserialized_obj = s.deserialize(received_obj);
			
				// show object reflection info
				Inspector ins = new Inspector();
				ins.inspect(deserialized_obj, false);
				
			}
			
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
