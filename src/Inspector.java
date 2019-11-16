import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Class Explorer through Reflection
 * @author Benedict Mendoza
 *
 */
public class Inspector {
	
	/**
	 * Prints out fairly detailed Class information on the specified Object
	 * 
	 * @param obj : instance of an object to be examined
	 * @param recursive : determines whether to recursively inspect the Fields of the object 
	 */
	public void inspect(Object obj, boolean recursive) {
		
		inspect_with_tab_level(obj, obj.getClass(), recursive, 0);
			
	}
	
	/**
	 * Underlying function of inspect()
	 * 
	 * @param obj : actual object instance
	 * @param obj_class : Class of the instance
	 * @param recursive : dictates whether to explore the Class' fields recursively
	 * @param tab_level : used to maintain how many tabs should be used for the particular recursion
	 */
	private void inspect_with_tab_level(Object obj, Class obj_class, boolean recursive, int tab_level) {
		
		// Base case: we have reached past the Object Class
		if (obj == null || obj_class == null) {
			return;
		}
	
		// indentation for our current recursion level
		String indent = new String(new char[tab_level]).replace("\0", "\t");
		
		// print object name
		String class_name = obj_class.getName();
		String object_title = obj_class.isInterface()? "Interface Name: " : "Class Name: ";
		System.out.print(indent + object_title + class_name + "\n\n");
		
		// recurse on super class
		System.out.println(indent + "Super-Class (" + class_name + ")");
		Class superclass = obj_class.getSuperclass();
		if (superclass != null) {
			inspect_with_tab_level(obj, superclass, recursive, tab_level+1);
		} else {
			System.out.print(indent + "None\n\n");
		}
		
		// recurse on interfaces
		System.out.println(indent + "Interfaces (" + class_name + ")");
		Class[] interfaces = obj_class.getInterfaces();
		if (interfaces.length > 0) {
			for (Class anInterface : interfaces) {
				inspect_with_tab_level(anInterface, anInterface, recursive, tab_level+1);
			}	
		} else {
			System.out.print(indent + "None\n\n");
		}
		
		// print out constructors and methods information
		String basic_class_info = basic_inspect(obj, obj_class);
		basic_class_info = basic_class_info.replaceAll("(?m)^", indent); 		// adds in the indentation
		System.out.print(basic_class_info);
		
		// print out fields
		// recurse if needed
		Field[] obj_fields = obj_class.getDeclaredFields();
		System.out.println(indent + "Fields (" + class_name + ")");
	
		if (obj_fields.length > 0) {
			for (Field aField : obj_fields) {
				String field_info = get_field_info(obj, aField);
				field_info = field_info.replaceAll("(?m)^", indent); 		// adds in the indentation
				System.out.print(field_info);
				
				// recurse if needed
				// inspect the field objects
				if (recursive && !aField.getType().isPrimitive()) {
					
					Object field_value = null;
					try {
						field_value = aField.get(obj);
						
					} catch (Exception e) {
						System.out.println("Retrieving Field value exception - THIS SHOULD NOT HAPPEN");
					}
						
					if (field_value != null ) {
						
						Class field_value_class = null;
						if (field_value instanceof Class) {
							field_value_class = (Class)field_value;;
						} else {
							field_value_class = field_value.getClass();
						}
						inspect_with_tab_level(field_value, field_value_class, recursive, tab_level+1);
					} 
				}
			}
		} else {
			System.out.print(indent + "None\n\n");
		}
		
		
		// recurse on arrays if object is an array
		if (obj_class.isArray()) {
			System.out.println(indent + "Array Info (" + class_name + ")");
			String array_info = get_array_info(obj);
			array_info = array_info.replaceAll("(?m)^", indent); 		// adds in the indentation
			System.out.print(array_info);
			
			// loop through the array
			for (int i=0; i<Array.getLength(obj); i++) {
				Object array_obj = Array.get(obj, i);
				if (array_obj != null) {
					
					Class component_class = array_obj.getClass();
					
					if (component_class == char.class || component_class == Character.class || component_class == Integer.class || component_class == Long.class ||
							component_class == Float.class || component_class == Double.class || component_class == Boolean.class) {
						System.out.printf(indent + "\t" + "Primitive at index: %d (%s)\n", i, class_name);
						System.out.printf(indent + "\t" + "%s\n\n", array_obj);

						
					} else {
						System.out.printf(indent + "\t" + "Object at index: %d (%s)\n", i, class_name);
						inspect_with_tab_level(array_obj, array_obj.getClass(), recursive, tab_level+1);

					}
				} else {
					System.out.printf(indent + "\t" + "Object at index: %d (%s)\n", i, class_name);
					System.out.printf(indent + "\t" + "null\n\n");
				}
			}
		} 
	}
	
	/**
	 * 
	 * @param obj : actual object instance
	 * @param obj_class : Class object of the instance
	 * @return outputs String containing info about the Class' constructors and methods
	 */
	public static String basic_inspect(Object obj, Class obj_class) {
		String class_name = obj_class.getName();
		StringBuilder sb = new StringBuilder();
		
		// insert constructors 
		sb.append("Constructors (" + class_name + ")\n");
		String constructors_info = get_constructors_info(obj_class);
		if (!constructors_info.isEmpty()) {
			sb.append(constructors_info);
		} else {
			sb.append("None\n\n");
		}
		
		// insert methods 
		sb.append("Methods (" + class_name + ")\n");
		String methods_info = get_declared_methods_info(obj_class);
		if (!methods_info.isEmpty()) {
			sb.append(methods_info);
		} else {
			sb.append("None\n\n");
		}
		
		return sb.toString();
	}
	
	/**
	 * 
	 * @param array_obj : Array Object
	 * @return ouput string containing array type and length
	 */
	public static String get_array_info(Object array_obj) {
		
		// ensure arrray object is an actual array
		Class array_class = array_obj.getClass();
		if (!array_class.isArray()) return "";
		
		StringBuilder sb = new StringBuilder();
		int arr_length = Array.getLength(array_obj);
		sb.append(String.format("Array Length: %d\n", arr_length));
		
		String array_type = array_class.getComponentType().getTypeName();
		sb.append(String.format("Array Type: %s\n", array_type));
		
		
		return sb.toString();
	}
	
	
	/**
	 * 
	 * @param obj : actual object instance
	 * @param aField : field found in the object instance
	 * @return : output string containing information about the given field
	 */
	public static String get_field_info(Object obj, Field aField) {
		StringBuilder sb = new StringBuilder();
		
		if (!aField.isAccessible()) {
			aField.setAccessible(true);
		}
		
		// print name
		String field_name = aField.getName();
		sb.append(String.format("Field Name: %s\n", field_name));
					
		// print type
		Class field_type = aField.getType();
		sb.append(String.format("Field Type : %s\n", field_type.getTypeName()));
		
		// print modifiers
		String field_modifiers = get_modifiers(aField);
		sb.append(String.format("Field Modifiers: %s\n", field_modifiers));
						
		// print current value
		String error_string = null;
		Object field_value = null;
		
		try {
			field_value = aField.get(obj);
			
		} catch (IllegalAccessException iae) {
			error_string = "IllegalAccessException thrown";
		
		} catch (IllegalArgumentException iae) {
			error_string = "IllegalArgumentException thrown";
			
		} catch (NullPointerException npe) {
			error_string = "NullPointerException thrown";
		}
		
		if (field_value != null) {
			// create string of object with address or simply show the primitive value
			if (!field_type.isPrimitive()) {
				String obj_address = Integer.toHexString(System.identityHashCode(field_value));
				sb.append(String.format("Current Value: %s\n\n", field_value.getClass().getName() + "@" + obj_address));
			} else {
				sb.append(String.format("Current Value: %s\n\n", field_value));
			}
		} else {
			// failed to obtain value therefore report exception
			sb.append(String.format("Current Value: %s\n\n", error_string));	
		}
			
		return sb.toString();
	}
	
	
	/**
	 * prints the constructors of a class obj
	 * @param class_obj : Class object containing the constructors
	 */
	public static String get_constructors_info(Class class_obj) {
		Constructor[] class_constructors = class_obj.getDeclaredConstructors();
		StringBuilder sb = new StringBuilder();
		
		for (Constructor aConstructor : class_constructors) {
			// set accessible if constructor is not accessible
			if (!aConstructor.isAccessible()) {	
				aConstructor.setAccessible(true);
			}
			
			// print constructor name
			String construc_name = aConstructor.getName();
			sb.append(String.format("Constructor Name: %s\n",  construc_name));

			// print parameter types
			String parameter_types = classArrayToString(aConstructor.getParameterTypes(), ", ");
			if (parameter_types.isEmpty()) {
				parameter_types = "None";
			} 
			sb.append(String.format("Parameter Types: %s\n", parameter_types));
			
			// print possible modifiers
			String modifiers = get_modifiers(aConstructor);
			sb.append(String.format("Modifers: %s\n\n", modifiers));
			
		}
		
		return sb.toString();
		
	}
	
	/**
	 * 
	 * @param class_obj : Class object
	 * @return : output String containing declared methods and their respective information
	 */
	public static String get_declared_methods_info(Class class_obj) {
		Method[] class_methods = class_obj.getDeclaredMethods();
		StringBuilder sb = new StringBuilder();
		
		for (Method aMethod : class_methods) {
			// set accessible if method is not accessible
			if (!aMethod.isAccessible()) {
				aMethod.setAccessible(true);
			}
			
			// print method name
			String method_name = aMethod.getName();
			sb.append(String.format("Method Name: %s\n",  method_name));
			
			
			// print exceptions thrown
			String exceptions = classArrayToString(aMethod.getExceptionTypes(), ", ");
			if (exceptions.isEmpty()) {
				sb.append("Exceptions Thrown: None\n");
			} else {
				sb.append(String.format("Exceptions Thrown: %s\n", exceptions));
			}
			
			// print parameter types
			String parameters = classArrayToString(aMethod.getParameterTypes(), ", ");
			if (parameters.isEmpty()) {
				sb.append("Parameter Types: None\n");
			} else {
				sb.append(String.format("Parameter Types: %s\n", parameters));
			}
			
			// print return types
			String return_type = aMethod.getReturnType().getTypeName();
			if (return_type.isEmpty()) {
				sb.append("Return Type: Void\n");
			} else {
				sb.append(String.format("Return Type: %s\n", return_type));
			}
			
			// print modifiers
			String modifiers = get_modifiers(aMethod);
			sb.append(String.format("Modifiers: %s\n\n", modifiers));
			
		}
		
		return sb.toString();
	}
	
	/**
	 * 
	 * @param class_array : array of Class objects
	 * @param separator : separator string used to divide the the Class objects
	 * @return : string output looks like: Class.name1 "seperator" Class.name2 "separator" .... Class.nameN
	 */
	public static String classArrayToString(Class[] class_array, String separator) {
		StringBuilder sb = new StringBuilder();
		
		if (class_array.length > 0) {
			
			sb.append(class_array[0].getTypeName());
			
			for (int i=1; i<class_array.length; i++) {
				sb.append(separator);
				sb.append(class_array[i].getTypeName());
			}
		}
		
		return sb.toString();
	}
	
	
	/**
	 * 
	 * @param obj : must be of type Method, Constructor, Field
	 * @return : modifiers in string like so "modifier1, modifier2, ..., modifierN"
	 * 
	 */
	public static String get_modifiers(Object obj) {
		
		int modifiers = -1;
		
		if (obj instanceof Constructor) {
			modifiers = ((Constructor)obj).getModifiers();
			
		} else if (obj instanceof Method) {
			modifiers = ((Method)obj).getModifiers();
			
		} else if (obj instanceof Field) {
			modifiers = ((Field)obj).getModifiers();
			
		} else {
			return "None Found";
		}
		
		StringBuffer modifiers_sb =  new StringBuffer();
		
		if (Modifier.isPublic(modifiers))
			modifiers_sb.append("public, ");
		
		if (Modifier.isPrivate(modifiers))
			modifiers_sb.append("private, ");
		
		if (Modifier.isProtected(modifiers)) 
			modifiers_sb.append("protected, ");
		
		if (Modifier.isStrict(modifiers)) 
			modifiers_sb.append("strict, ");
		
		if (Modifier.isStatic(modifiers)) 
			modifiers_sb.append("static, ");
		
		if (Modifier.isFinal(modifiers)) 
			modifiers_sb.append("final, ");
		
		if (Modifier.isAbstract(modifiers)) 
			modifiers_sb.append("abstract, ");
		
		if (Modifier.isInterface(modifiers)) 
			modifiers_sb.append("interface, ");
			
		if (Modifier.isNative(modifiers)) 
			modifiers_sb.append("native, ");
		
		if (Modifier.isSynchronized(modifiers)) 
			modifiers_sb.append("synchronized, ");
		
		if (Modifier.isTransient(modifiers)) 
			modifiers_sb.append("transient, ");
		
		if (Modifier.isVolatile(modifiers)) 
			modifiers_sb.append("volatile, ");
		
		// remove the ", " from the string buffer
		int buff_length = modifiers_sb.length();
		if (buff_length != 0) 
			modifiers_sb.delete(buff_length-2, buff_length);
		
		
		return modifiers_sb.toString();
	}
}