import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import org.jdom2.Document;

public class Client {
	
	
	public static void main(String[] args) {
		
		if (args.length <= 0) {
			System.out.println("Chose from [bankaccount / bank / floatarr / stringarr / arrliststr] [args...]");
			return;
		}
		
		Object input_obj = null;
		// parse input
		if (args[0].equals("bankaccount")) {
			
			if (args.length != 4) {
				System.out.println("bankaccount [double] [int] [double]");
				return;
			}
			
			try {
				double start_balance = Double.parseDouble(args[1]);
				int acc_num = Integer.parseInt(args[2]);
				double int_rate = Double.parseDouble(args[3]);
				
				BankAccount ba = new BankAccount(start_balance, acc_num, int_rate);
				input_obj = ba;
				
			} catch (Exception e) {
				System.out.println("Please use the right arguments");
				System.out.println("bankaccount [double] [int] [double]");
				return;

			}
			
			
		} else if (args[0].equals("bank")) {
			
			if (args.length != 4) {
				System.out.println("bank [string] [int] [double]");
				return;
			}
			
			try {
				String bank_name = args[1];
				int num_of_accounts = Integer.parseInt(args[2]);
				double int_rate = Double.parseDouble(args[3]);
				
				Bank ba = new Bank(bank_name, num_of_accounts, int_rate);
				input_obj = ba;
				
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Please use the right arguments");
				System.out.println("bank [string] [int] [double]");
				return;


			}
		
		
		} else if (args[0].equals("floatarr")) {
			int arr_size = args.length-1;
			float[] float_arr = new float[arr_size];
			
			try {
				
				for (int i=0; i<arr_size; i++) {
					float_arr[i] = Float.parseFloat(args[i+1]);
				}
				
			} catch (Exception e) {
				System.out.println("Please only enter float arguments");
				System.out.println("floatarr [float1] [float2] .... [floatn]");
				return;

			}
			
			input_obj = float_arr;
			
		} else if (args[0].equals("stringarr")) {
			int arr_size = args.length-1;
			String[] str_arr = new String[arr_size];
			
			for (int i=0; i<arr_size; i++) {
				str_arr[i] = args[i+1];
			}
			
			input_obj = str_arr;
				
			
		} else if (args[0].equals("arrliststr")) {
			ArrayList<String> arrlist_str = new ArrayList<String>();
			
			for (int i=0; i<args.length-1; i++) {
				arrlist_str.add(args[i+1]);
			}
			
			input_obj = arrlist_str;
			
		} else {
			System.out.println("Chose from [bankaccount / bank / floatarr / stringarr / arrliststr] [args...]");
		}
		
		
		
		String serverAddress = "localhost";
		int serverPort = 8888;
		try {
			Socket socket = new Socket(serverAddress, serverPort);
			ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
			
			// send the specified object
			Serializer ser = new Serializer();
			Document doc = ser.serialize(input_obj);
			
			outputStream.writeObject(doc);
			
			outputStream.flush();
			socket.close();
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
	}
}
