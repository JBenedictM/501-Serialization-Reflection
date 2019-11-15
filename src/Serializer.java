import java.io.FileWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class Serializer {
	private String a = "test";
	private int b = 1;
	private HashSet<Integer> visited_objs;
	
	
	public org.jdom2.Document serialize(Object obj) {
		
		Element root = new Element("serialized");
		Document output_doc = new Document(root);
//		output_doc.setRootElement(root);
		
		visited_objs = new HashSet<Integer>();
		ArrayList<Element> xml_objs = obj_to_xmls(obj);
		
		
		for (int i=0; i<xml_objs.size(); i++) {
//			root.addContent(xml_objs.get(i));
			output_doc.getRootElement().addContent(xml_objs.get(i));
			
			//			System.out.println("Element Name: " + xml_objs.get(i).getAttributeValue("class"));
		}
		
		
		try {
//			new XMLOutputter().output(output_doc, System.out);
			XMLOutputter xmlOut = new XMLOutputter();
			xmlOut.setFormat(Format.getPrettyFormat());
			xmlOut.output(output_doc, new FileWriter("file.xml"));
			
			System.out.println("File saved\n");
			
		} catch(Exception e) {
			System.out.println("XML outputtter error\n");
		}
		

		return output_doc;
	}
	
	public ArrayList<Element> obj_to_xmls(Object obj) {
		
		// init arraylist return
		ArrayList<Element> xml_objs = new ArrayList<Element>();
		// check for base case
		Integer obj_id = System.identityHashCode(obj);
		if (visited_objs.contains(obj_id)) {
			return xml_objs;
		}
		visited_objs.add(obj_id);
			
		Element current_xml_obj = new Element("object");
		Class obj_class = obj.getClass();
//		System.out.printf("Current obj xml: %s\n", obj_class.getName());

		xml_objs.add(current_xml_obj);
		
		// parse id and object name
		current_xml_obj.setAttribute("class", obj_class.getName());
		current_xml_obj.setAttribute("id", "0x"+Integer.toHexString(obj_id));
		
		// check if array
		if (obj_class.isArray()) {
			// explore array elements
			int arr_len = Array.getLength(obj);
			current_xml_obj.setAttribute("length", Integer.toString(arr_len));
			
			for (int i=0; i<arr_len; i++) {
				Element arr_element;
				Object array_element_obj = Array.get(obj, i);
				
				if (obj_class.getComponentType().isPrimitive()) {
					arr_element = new Element("value");
					arr_element.setText(array_element_obj.toString());
					
				} else {
					arr_element = new Element("reference");
					if (array_element_obj == null) {
						arr_element.setText("null");
						
					} else {
						
						String ref_address = Integer.toHexString(System.identityHashCode(array_element_obj));
						arr_element.setText("0x"+ref_address);
						
						// recurse
//						System.out.printf("Current field class %s\n", field_val.getClass().getName());
						ArrayList<Element> array_objs = obj_to_xmls(array_element_obj);

						xml_objs.addAll(array_objs);
					}	
				}
				
				current_xml_obj.addContent(arr_element);					
			}
			
			
		} else {
			// explore field values/references
			// parse object fields
			Field[] obj_fields = obj_class.getDeclaredFields();
			for (Field aField : obj_fields) {
				Element field_element = new Element("field");
				
				// set field accessible
				if (!aField.isAccessible()) aField.setAccessible(true);
				
				// set field attributes
				field_element.setAttribute("name", aField.getName());
				Class field_type = aField.getType();
				field_element.setAttribute("declaringclass", field_type.getName());
				
				// get field value
				Element field_child = null;
				Object field_val = get_field_value(obj, aField);
				
				
				
				if (field_type.isPrimitive()) {
					// create value element
					field_child = new Element("value");
					field_child.setText(field_val.toString());

				} else {
					
					field_child = new Element("reference");

					if (field_val == null) {
						field_child.setText("null");

					} else {
						
						// create reference element
						String ref_address = Integer.toHexString(System.identityHashCode(field_val));
						field_child.setText("0x"+ref_address);
						
						// recurse
//						System.out.printf("Current field class %s\n", field_val.getClass().getName());
						ArrayList<Element> field_objs = obj_to_xmls(field_val);

						xml_objs.addAll(field_objs);
					} 
					
					
				}
				
				field_element.addContent(field_child);
				current_xml_obj.addContent(field_element);
				
			}
			
		}
		
		
		
		
		return xml_objs;
	}
	
	
	public Object get_field_value(Object obj, Field aField) {
		Object field_val = null;
		
		try {
			field_val = aField.get(obj);
		} catch (Exception e) {
			System.out.println("Failed to obtain field's value");
			field_val = null;
		}
		
		return field_val;
	}
	
	public Object deserialize(org.jdom2.Document doc) {
		
		
		
	}
	
	
	public static void main(String[] args) {
		String testInput = "Hi";
		Serializer s = new Serializer();
		s.serialize(s);
		
	}

}












