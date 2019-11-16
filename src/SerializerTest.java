import static org.junit.Assert.*;

import java.util.ArrayList;

import org.jdom2.Document;
import org.junit.Test;

public class SerializerTest {
	
	private Serializer ser = new Serializer();
	
	@Test
	public void test_ser_int_arr() {
		int[] int_arr = {1, 2, 3};
		Document ser_obj = ser.serialize(int_arr);
		
		int[] des_obj = (int[])ser.deserialize(ser_obj);
		
		assertArrayEquals(int_arr, des_obj);
	}
	
	@Test
	public void test_ser_str_arr() {
		String[] str_arr = {"abc", "def", "ghi"};
		Document ser_obj = ser.serialize(str_arr);
		
		String[] des_obj = (String[])ser.deserialize(ser_obj);
		
		assertArrayEquals(str_arr, des_obj);
	}
	
	@Test
	public void test_ser_str() {
		String str = "test string";
		Document ser_obj = ser.serialize(str);
		
		String des_obj = (String)ser.deserialize(ser_obj);
		
		assertEquals(str, des_obj);
	}
	
	
	
	@Test
	public void test_ser_str_arrayList() {
		ArrayList<String> arr_list = new ArrayList<String>();
		arr_list.add("test1");
		arr_list.add("test2");
		
		Document ser_obj = ser.serialize(arr_list);
		
		ArrayList<String> des_obj = (ArrayList<String>)ser.deserialize(ser_obj);
		
		for (int i=0; i<arr_list.size(); i++) {
			assertEquals(arr_list.get(i), des_obj.get(i));
		}
		
	}
	
	@Test
	public void test_ser_Bank() {
		Bank bk = new Bank("Generic Bank", 10, 0.1);
		Document ser_obj = ser.serialize(bk);
		
		Bank des_obj = (Bank)ser.deserialize(ser_obj);
		
		// compare fields
		assertEquals(bk.get_bank_name(), des_obj.get_bank_name());
		assertSame(bk.get_number_of_accounts(), des_obj.get_number_of_accounts());
		assertEquals(bk.get_interest_rate(), des_obj.get_interest_rate(), 0.01);
		
	}
	
	@Test
	public void test_ser_BankAccount() {
		BankAccount ba = new BankAccount(100, 69, 0.1);
		Document ser_obj = ser.serialize(ba);
		
		BankAccount des_obj = (BankAccount)ser.deserialize(ser_obj);
		
		// compare fields
		assertSame(ba.getAccountNumber(), des_obj.getAccountNumber());
		assertEquals(ba.getBalance(), des_obj.getBalance(), 0.01);
		assertEquals(ba.get_interest_rate(), des_obj.get_interest_rate(), 0.01);
		
	}
	
	

}
