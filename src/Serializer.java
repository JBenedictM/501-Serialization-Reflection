import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;


public class Serializer {
	private String a = "test";
	private int b = 1;
	private HashSet<Integer> visited_objs;
	private HashMap<Integer, Object> recreated_obj;
	private List<Element> xml_children;

	
	public Document serialize(Object obj) {
		
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
		
		
		return output_doc;
	}
	
	private ArrayList<Element> obj_to_xmls(Object obj) {
		
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
		current_xml_obj.setAttribute("id", Integer.toString((obj_id)));
		
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
						
						String ref_address = Integer.toString(System.identityHashCode(array_element_obj));
						arr_element.setText(ref_address);
						
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
				// skip static fields
				if (Modifier.isStatic(aField.getModifiers())) continue;
				
				Element field_element = new Element("field");
				
				// set field accessible
				if (!aField.isAccessible()) aField.setAccessible(true);
				
				// set field attributes
				field_element.setAttribute("name", aField.getName());
				
				
				// get field value
				Element field_child = null;
				Object field_val = get_field_value(obj, aField);
				
				Class field_type = aField.getType();
				// get field declaring class
				field_element.setAttribute("declaringclass", field_val == null? field_type.getName() : field_val.getClass().getName());
				
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
						String ref_address = Integer.toString(System.identityHashCode(field_val));
						field_child.setText(ref_address);
						
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
	
	
	private Object get_field_value(Object obj, Field aField) {
		Object field_val = null;
		
		try {
			field_val = aField.get(obj);
		} catch (Exception e) {
			System.out.println("Failed to obtain field's value");
			field_val = null;
		}
		
		return field_val;
	}
	
	public Object deserialize(Document doc) {
		Element root = doc.getRootElement();
		xml_children = root.getChildren();
		recreated_obj = new HashMap<Integer, Object>();
		Object main_obj = null;
		
		try {
			main_obj = xml_to_obj(xml_children.get(0).getAttribute("id").getIntValue());

		} catch (Exception e) {
			System.out.println("Failed to get main object's id");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		
		return main_obj;
		
	}
	
	private Object xml_to_obj(int ref_id) {
		
		if (recreated_obj.containsKey(ref_id)) {
			return recreated_obj.get(ref_id);
		}
		
		Element instantiate_element = null;
		Object output_obj = null;
		for (Element aChild : xml_children) {
			String id_str = aChild.getAttributeValue("id");
			if (id_str == null) {
				System.out.println("Missing id attribute, incorrect format.");
				continue;
			}
			
			int id = Integer.parseInt(id_str);
			if (id == ref_id) {
				instantiate_element = aChild;
				break;
			}	
		}
		
		if (instantiate_element == null) {
			System.out.println("Reference id was not found, this should not happen");
			return null;
		}
		
		
		// check for object class name
		String class_name = instantiate_element.getAttributeValue("class");
		if (class_name == null) {
			System.out.println("Class name not found, incorrect formatting");
			return null;
		}
		
		// create element class
		try {
			Class element_class = Class.forName(class_name);
			
			if (element_class.isArray()) {
				// instaniate array object
				output_obj = instantiate_array_obj(element_class, instantiate_element);
				
			} else {
				output_obj = instantiate_regular_obj(element_class, instantiate_element);
				
			}
			
			if (output_obj == null) {
				System.out.printf("Failed to instantiate %s with id %d\n", class_name, ref_id);
			}

		} catch (Exception e) {
			System.out.print("Failed to recreate object due to: ");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		
		return output_obj;
	}
	
	// instantiates regular objects not including arrays
	private Object instantiate_regular_obj(Class obj_class, Element obj_element) {
		Object output_obj = null;
		
		try {
			// instantiate object with null constructor
			Constructor null_constr = obj_class.getConstructor();
			if (!null_constr.isAccessible()) null_constr.setAccessible(true);
			output_obj = null_constr.newInstance();
			
			// recreate field objects/primitives
			List<Element> element_fields = obj_element.getChildren();
			// loop through the field values
			for (Element field_element : element_fields) {
				
				// get field class
				String declaring_class_name = field_element.getAttributeValue("declaringclass");
				if (declaring_class_name == null) {
					System.out.println("Field has no declaring class, incorrect formatting");
					return null;
				}
				
				Class field_class = Class.forName(declaring_class_name);
				
				Object field_obj = null;
				if (field_element.getChildText("reference") == null) {
					// create the primitive object
					
					// get value as string
					String val_str = field_element.getChildText("value");
					if (val_str == null) {
						System.out.printf("%s has no Primitive Field has no value element, incorrect formatting\n", field_element.getAttributeValue("name"));
						return null;
					}
					
					// instaniate primitive object
					field_obj = create_primitive_object(field_class, val_str);
					
					
				} else {
					// instantiate object through recursion
					String ref_str = field_element.getChildText("reference");
					if (ref_str == null) {
						System.out.printf("%s has no reference element, incorrect formatting\n", field_element.getAttributeValue("name"));
						return null;
						
					} else {
						
						// check whether reference value is an id or null
						if (ref_str.equalsIgnoreCase("null")) {
							// reference is null
							field_obj = null;
							
						} else {
							// reference exists so therefore recreate object
							// check if object is already in the map
							Integer field_id = Integer.parseInt(ref_str);
							if (recreated_obj.containsKey(field_id)) {
								// get field object from map
								field_obj = recreated_obj.get(field_id);
							
							} else {	
								// instantiate field object
								field_obj = xml_to_obj(field_id);
								
								// add object to maps of instantiated objects
								recreated_obj.put(field_id, field_obj);
							}
						}
					}
					
//					System.out.printf("Created object %s of with id %s\n", obj_element.getAttributeValue("class"), obj_element.getAttributeValue("id"));
					
					
				}
				
				// get field reflection from class
				String field_name = field_element.getAttributeValue("name");
				if (field_name == null) {
					System.out.println("Field has has no name attribute, incorrect formatting");
					return null;
				}
				
				// set field value
				Field current_field = obj_class.getDeclaredField(field_name);
				
				if (!current_field.isAccessible()) current_field.setAccessible(true);
				
				current_field.set(output_obj, field_obj);
				
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		
		return output_obj;
		
	}
	
	private Object instantiate_array_obj(Class obj_class, Element obj_element) {
		Object output_obj = null;
		
		// get array len
		String arr_len_str = obj_element.getAttributeValue("length");
		if (arr_len_str == null) {
			System.out.println("Array object has no length attribute, incorrect formatting");
			return null;
		}
		// instaniate base array object
		int arr_len = Integer.parseInt(arr_len_str);
		output_obj = Array.newInstance(obj_class.getComponentType(), arr_len);
		
		List<Element> arr_elements = obj_element.getChildren();
		for (int i=0; i<arr_len; i++) {
			Element current_element = arr_elements.get(i);
			Object element_obj = null;
			
			if (current_element.getName().equalsIgnoreCase("value")) {
				// create the primitive object
				// get consuctor with string parameter
				Class prim_wrapper = Array.get(output_obj, 0).getClass();
				
				String val_str = current_element.getText();
				if (val_str == null || !current_element.getName().equalsIgnoreCase("value")) {
					System.out.println("Primitive array element has no value element, incorrect formatting");
					continue;
				}
				
				// instaniate primitive object
				element_obj = create_primitive_object(prim_wrapper, val_str);
				if (element_obj == null) {
					System.out.printf("Failed to create primitive object %s\n", prim_wrapper.toString());
					return null;
				}
				
			} else {
				
				// instantiate regular objects
				String ref_str = current_element.getText();
				if (ref_str == null || !current_element.getName().equalsIgnoreCase("reference")) {
					System.out.println("Reference object element has no reference value, incorrect formatting");
					return null;
				}
				
				// check whether reference value is an id or null
				if (ref_str.equalsIgnoreCase("null")) {
					// reference is null
					element_obj = null;
					
				} else {
					// reference exists so therefore recreate object
					// check if object is already in the map
					Integer element_id = Integer.parseInt(ref_str);
					if (recreated_obj.containsKey(element_id)) {
						// get field object from map
						element_obj = recreated_obj.get(element_id);
							
					} else {	
						// instantiate field object
						element_obj = xml_to_obj(element_id);
							
						// add object to maps of instantiated objects
						recreated_obj.put(element_id, element_obj);
						
					}
					
					System.out.printf("Created element object %s with value %s\n", current_element.getName(), current_element.getText());
				}			
			}
			
			Array.set(output_obj, i, element_obj);
		}
		
		return output_obj;
	}
	
	// does not check if given class is actually a primitive
	private Object create_primitive_object(Class primitive_wrapper, String str_val) {
		
		Object output_obj = null;
		
		try {
			if (primitive_wrapper == Character.class) {
				Constructor constr = primitive_wrapper.getConstructor(char.class);
				if (!constr.isAccessible()) constr.setAccessible(true);
				
				output_obj = constr.newInstance(str_val.charAt(0));
			} else {
				
				Constructor constr = primitive_wrapper.getConstructor(String.class);
				if (!constr.isAccessible()) constr.setAccessible(true);
				
				// instaniate primitive object
				output_obj = constr.newInstance(str_val);
				
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}	
		
		return output_obj;
	}
	
	
	public static void main(String[] args) {
		String testInput = "Hi";
		int[] testInput2 = {1,2,3};
		HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
		hm.put(1,1);
		String[] testInput3 = {"hi", "hello"};
		ArrayList<String> ar = new ArrayList<String>();
		ar.add("Hi");
		ar.add("Hello");
//		Socket sock = new Socket();
		Class c = Integer.class;
		Object obj = new Object();
	
//		obj.
		
		Serializer s = new Serializer();
		Document doc1 = s.serialize(obj);
		
		try {
//			SAXBuilder builder = new SAXBuilder();
//			Document test_doc = builder.build(System.in);
//			builder.build
			Object testOutput = s.deserialize(doc1);
			
			if (testOutput == null) {
				System.out.println("deserialized object is null");
				return;
			}
			
//			ArrayList<String> arOut = (ArrayList)testOutput;
//			System.out.println(arOut.get(0));
			
//			int[] realOut = (int[])testOutput;
//			for (int i=0; i<realOut.length ; i++) {
//				System.out.printf("%d\n", realOut[i]);
//			}
//
//			Document doc2 = s.serialize(testOutput);
//			
//
//			XMLOutputter xmlOut = new XMLOutputter();
//			xmlOut.setFormat(Format.getPrettyFormat());
//			xmlOut.output(doc2, new FileWriter("file2.xml"));
//			
//			System.out.println("File saved\n");
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
			
//		} catch (JDOMException jde) {
//			System.out.println(jde.getMessage());
//		
//		} catch (IOException ioe) {
//			System.out.println(ioe.getMessage());
//
//		}
	}
//
//		
//	}

}

