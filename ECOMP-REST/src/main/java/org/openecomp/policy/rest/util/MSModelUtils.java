/*-
 * ============LICENSE_START=======================================================
 * ECOMP-REST
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.policy.rest.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EAttributeImpl;
import org.eclipse.emf.ecore.impl.EEnumImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.json.JSONObject;
import org.openecomp.policy.rest.XACMLRestProperties;

import com.att.research.xacml.util.XACMLProperties;
import com.google.gson.Gson;


public class MSModelUtils {

	private static final Log logger	= LogFactory.getLog(MSModelUtils.class);

	private HashMap<String,MSAttributeObject > classMap = new HashMap<String,MSAttributeObject>();
	private HashMap<String, String> enumMap = new HashMap<String, String>();
	private HashMap<String, String> matchingClass = new HashMap<String, String>();
	private String configuration = "configuration";
	private String dictionary = "dictionary";
	private String ecomp = "";
	private String policy = "";
	private String eProxyURI = "eProxyURI:";
	
	public MSModelUtils(String ecomp, String policy){
		this.ecomp = ecomp;
		this.policy = policy;
	}

	private enum ANNOTATION_TYPE{
		MATCHING, VALIDATION, DICTIONARY
	};

	public enum MODEL_TYPE {
		XMI
	};


	public HashMap<String, MSAttributeObject> processEpackage(String file, MODEL_TYPE model){
		if (model == MODEL_TYPE.XMI ){
			processXMIEpackage(file);
		}
		return classMap;

	} 

	private void processXMIEpackage(String xmiFile){
		EPackage root = getEpackage(xmiFile);
		TreeIterator<EObject> treeItr = root.eAllContents();
		String className = null;
		String returnValue = null;

		//    Pulling out dependency from file
		while (treeItr.hasNext()) {	    
			EObject obj = (EObject) treeItr.next();
			if (obj instanceof EClassifier) {
				EClassifier eClassifier = (EClassifier) obj;
				className = eClassifier.getName();

				if (obj instanceof EEnum) {
					enumMap.putAll(getEEnum(obj));
				}else if (obj instanceof EClass) {
					String temp = getDependencyList(eClassifier, className).toString();
					returnValue = StringUtils.replaceEach(temp, new String[]{"[", "]"}, new String[]{"", ""});
					getAttributes(className, returnValue, root);
				}        		   		
			}
		}

		if (!enumMap.isEmpty()){
			addEnumClassMap();
		}
		if (!matchingClass.isEmpty()){
			CheckForMatchingClass();
		}
	}

	private void CheckForMatchingClass() {
		HashMap<String, String> tempAttribute = new HashMap<String, String>();

		for (Entry<String, String> set : matchingClass.entrySet()){
			String key = set.getKey();
			if (classMap.containsKey(key)){
				Map<String, String> listAttributes = classMap.get(key).getAttribute();
				Map<String, String> listRef = classMap.get(key).getRefAttribute();
				for (  Entry<String, String> eSet : listAttributes.entrySet()){
					String key2 = eSet.getKey();
					tempAttribute.put(key2, "matching-true");
				}
				for (  Entry<String, String> eSet : listRef.entrySet()){
					String key3 = eSet.getKey();
					tempAttribute.put(key3, "matching-true");
				}

			}
			UpdateMatching(tempAttribute, key);
		}

	}



	private void UpdateMatching(HashMap<String, String> tempAttribute, String key) {
		Map<String, MSAttributeObject> newClass = null;

		newClass = classMap;

		for (Entry<String, MSAttributeObject> updateClass :  newClass.entrySet()){
			HashMap<String, String> valueMap = updateClass.getValue().getMatchingSet();
			String keymap = updateClass.getKey();
			if (valueMap.containsKey(key)){
				HashMap<String, String> modifyMap = classMap.get(keymap).getMatchingSet();
				modifyMap.remove(key);
				modifyMap.putAll(tempAttribute);
				classMap.get(keymap).setMatchingSet(modifyMap);
			}

		}
	}

	private void addEnumClassMap() {
		for (Entry<String, MSAttributeObject> value :classMap.entrySet()){
			value.getValue().setEnumType(enumMap);
		}
	}

	private EPackage getEpackage(String xmiFile) {
		ResourceSet resSet = new ResourceSetImpl();
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("xmi", new XMIResourceFactoryImpl());
		Resource resource = resSet.getResource(URI.createFileURI(xmiFile), true);
		try {
			resource.load(Collections.EMPTY_MAP);
		} catch (IOException e) {
			logger.error("Error loading Encore Resource for new Model");
		}

		EPackage root = (EPackage) resource.getContents().get(0);

		return root;
	}

	private HashMap<String, String> getEEnum(EObject obj) {
		List<String> valueList = new ArrayList<>();
		HashMap<String, String> returnMap = new HashMap<String, String>();
		EEnum eenum = (EEnum)obj;

		String name = eenum.getName();
		for (EEnumLiteral eEnumLiteral : eenum.getELiterals())
		{
			Enumerator instance = eEnumLiteral.getInstance();
			String value = instance.getLiteral();
			valueList.add(value);
		}
		returnMap.put(name, valueList.toString());
		return returnMap;
	}

	public void getAttributes(String className, String dependency, EPackage root) {
		List<String> dpendList = null;
		if (dependency!=null){
			dpendList = new ArrayList<String>(Arrays.asList(dependency.split(",")));
		}
		MSAttributeObject msAttributeObject = new MSAttributeObject();
		msAttributeObject.setClassName(className);
		String extendClass = getSubTypes(root, className);
		HashMap<String, String> returnRefList = getRefAttributeList(root, className, extendClass);
		HashMap<String, String> returnAttributeList = getAttributeList(root, className, extendClass);
		HashMap<String, String> returnSubList = getSubAttributeList(root, className, extendClass);
		HashMap<String, String> returnAnnotation = getAnnotation(root, className, extendClass);
		msAttributeObject.setAttribute(returnAttributeList);
		msAttributeObject.setRefAttribute(returnRefList);
		msAttributeObject.setSubClass(returnSubList);
		msAttributeObject.setDependency(dpendList.toString());
		msAttributeObject.addMatchingSet(returnAnnotation);
		msAttributeObject.setPolicyTempalate(isPolicyTemplate(root, className));

		this.classMap.put(className, msAttributeObject);	
	}

	private HashMap<String, String> getAnnotation(EPackage root, String className, String extendClass) {
		TreeIterator<EObject> treeItr = root.eAllContents();
		boolean requiredAttribute = false; 
		boolean requiredMatchAttribute = false;
		HashMap<String, String> annotationSet = new HashMap<String, String>();
		String  matching  = null;
		String range   = null;
		String dictionary = null;

		//    Pulling out dependency from file
		while (treeItr.hasNext()) {	    
			EObject obj = treeItr.next();
			if (obj instanceof EClassifier) {
				requiredAttribute = isRequiredAttribute(obj,  className );
				requiredMatchAttribute = isRequiredAttribute(obj,  extendClass );
			}

			if (requiredAttribute){
				if (obj instanceof EStructuralFeature) {
					EStructuralFeature eStrucClassifier = (EStructuralFeature) obj;
					if (eStrucClassifier.getEAnnotations().size() != 0) {
						matching  = annotationValue(eStrucClassifier, ANNOTATION_TYPE.MATCHING, policy);
						if (matching!=null){
							annotationSet.put(eStrucClassifier.getName(), matching);
						}
						range  = annotationValue(eStrucClassifier, ANNOTATION_TYPE.VALIDATION, policy);
						if (range!=null){
							annotationSet.put(eStrucClassifier.getName(), range);
						}
						dictionary = annotationValue(eStrucClassifier, ANNOTATION_TYPE.DICTIONARY, policy);
						if (dictionary!=null){
							annotationSet.put(eStrucClassifier.getName(), dictionary);
						}
					}
				}
			} else if (requiredMatchAttribute){
				if (obj instanceof EStructuralFeature) {
					EStructuralFeature eStrucClassifier = (EStructuralFeature) obj;
					if (eStrucClassifier.getEAnnotations().size() != 0) {
						matching  = annotationValue(eStrucClassifier, ANNOTATION_TYPE.MATCHING, policy);
						if (matching!=null){
							if (obj instanceof EReference){
								EClass refType = ((EReference) obj).getEReferenceType();
								annotationSet.put(refType.getName(), matching);
								matchingClass.put(refType.getName(), matching);
							}else{
								annotationSet.put(eStrucClassifier.getName(), matching);
							}
						}
					}
				}
			}
		}
		return annotationSet;
	}

	private HashMap<String, String> getSubAttributeList(EPackage root, String className , String superClass) {
		TreeIterator<EObject> treeItr = root.eAllContents();
		boolean requiredAttribute = false; 
		HashMap<String, String> subAttribute = new HashMap<String, String>();
		int rollingCount = 0;
		int processClass = 0;
		boolean annotation = false;

		//    Pulling out dependency from file
		while (treeItr.hasNext() && rollingCount < 2) {	 

			EObject obj = treeItr.next();
			if (obj instanceof EClassifier) {
				if (isRequiredAttribute(obj,  className ) || isRequiredAttribute(obj,  superClass )){
					requiredAttribute = true;
				}else {
					requiredAttribute = false;
				}
				if (requiredAttribute){
					processClass++;
				}
				rollingCount = rollingCount+processClass;
			}

			if (requiredAttribute)   {
				if (obj instanceof EStructuralFeature) {
					EStructuralFeature eStrucClassifier = (EStructuralFeature) obj;
					if (eStrucClassifier.getEAnnotations().size() != 0) {
						annotation = annotationTest(eStrucClassifier, configuration, ecomp);
						if (annotation &&  obj instanceof EReference) {
							EClass refType = ((EReference) obj).getEReferenceType();
							if(!refType.toString().contains(eProxyURI)){
								subAttribute.put(eStrucClassifier.getName(), refType.getName());						
							}
						}	
					}
				}
			}
		}
		return subAttribute;
	}

	public String checkDefultValue(String defultValue) {
		if (defultValue!=null){
			return ":defaultValue-"+ defultValue;
		}
		return ":defaultValue-NA";

	}

	public String checkRequiredPattern(int upper, int lower) {	

		String pattern = XACMLProperties.getProperty(XACMLRestProperties.PROP_XCORE_REQUIRED_PATTERN);

		if (pattern!=null){
			if (upper == Integer.parseInt(pattern.split(",")[1]) && lower==Integer.parseInt(pattern.split(",")[0])){
				return ":required-true";
			}
		}

		return ":required-false";
	}

	public JSONObject buildJavaObject(HashMap<String, String> map, String attributeType){

		JSONObject returnValue = new JSONObject(map);

		return returnValue;

	}

	public HashMap<String, String> getRefAttributeList(EPackage root, String className, String superClass){

		TreeIterator<EObject> treeItr = root.eAllContents();
		boolean requiredAttribute = false; 
		HashMap<String, String> refAttribute = new HashMap<String, String>();
		int rollingCount = 0;
		int processClass = 0;
		boolean annotation = false;
		//    Pulling out dependency from file
		while (treeItr.hasNext()) {	    
			EObject obj = treeItr.next();
			if (obj instanceof EClassifier) {
				if (isRequiredAttribute(obj,  className ) || isRequiredAttribute(obj,  superClass )){
					requiredAttribute = true;
				}else {
					requiredAttribute = false;
				}	
				if (requiredAttribute){
					processClass++;
				}
				rollingCount = rollingCount+processClass;
			}

			if (requiredAttribute)   {
				if (obj instanceof EStructuralFeature) {
					EStructuralFeature eStrucClassifier = (EStructuralFeature) obj;
					if (eStrucClassifier.getEAnnotations().size() != 0) {
						annotation = annotationTest(eStrucClassifier, configuration, ecomp);
						if ( annotation &&  obj instanceof EReference) {
							EClass refType = ((EReference) obj).getEReferenceType();
							if(refType.toString().contains(eProxyURI)){
								String one = refType.toString().split(eProxyURI)[1];
								String refValue = StringUtils.replaceEach(one.split("#")[1], new String[]{"//", ")"}, new String[]{"", ""});							
								refAttribute.put(eStrucClassifier.getName(), refValue);							
							} else {
								String array = arrayCheck(((EStructuralFeature) obj).getUpperBound());
								refAttribute.put(eStrucClassifier.getName(), refType.getName() + array);
							}
						} else if (annotation &&  obj instanceof EAttributeImpl){
							EClassifier refType = ((EAttributeImpl) obj).getEType();
							if (refType instanceof EEnumImpl){
								String array = arrayCheck(((EStructuralFeature) obj).getUpperBound());
								refAttribute.put(eStrucClassifier.getName(), refType.getName() + array);							}
						}	
					}
				}
			}
		}
		return refAttribute;
	}

	private boolean annotationTest(EStructuralFeature eStrucClassifier, String annotation, String type) {
		String annotationType = null;
		EAnnotation eAnnotation = null;
		String ecompType = null;
		String ecompValue = null;

		EList<EAnnotation> value = eStrucClassifier.getEAnnotations();

		for (int i = 0; i < value.size(); i++){
			annotationType = value.get(i).getSource();
			eAnnotation = eStrucClassifier.getEAnnotations().get(i);
			ecompType = eAnnotation.getDetails().get(0).getValue();
			ecompValue = eAnnotation.getDetails().get(0).getKey();
			if (annotationType.contains(type) && ecompType.contains(annotation)){
				return true;
			} else if (annotationType.contains(type) && ecompValue.contains(annotation)){
				return true;
			}
		}

		return false;
	}


	private String annotationValue(EStructuralFeature eStrucClassifier, ANNOTATION_TYPE annotation, String type) {
		String annotationType = null;
		EAnnotation eAnnotation = null;
		String ecompType = null;
		String ecompValue = null;

		EList<EAnnotation> value = eStrucClassifier.getEAnnotations();

		for (int i = 0; i < value.size(); i++){
			annotationType = value.get(i).getSource();
			eAnnotation = eStrucClassifier.getEAnnotations().get(i);
			ecompType = eAnnotation.getDetails().get(0).getKey();
			if (annotationType.contains(type) && ecompType.compareToIgnoreCase(annotation.toString())==0){
				ecompValue = eAnnotation.getDetails().get(0).getValue();
				if (annotation == ANNOTATION_TYPE.VALIDATION){
					return ecompValue;
				} else {
					return ecompType + "-" + ecompValue;
				}
			}
		}

		return ecompValue;
	}
	public boolean isRequiredAttribute(EObject obj, String className){
		EClassifier eClassifier = (EClassifier) obj;
		String workingClass = eClassifier.getName();
		workingClass.trim();
		if (workingClass.equalsIgnoreCase(className)){
			return  true;
		}

		return false;
	} 

	private boolean isPolicyTemplate(EPackage root, String className){

		for (EClassifier classifier : root.getEClassifiers()){ 
			if (classifier instanceof EClass) { 
				EClass eClass = (EClass)classifier; 
				if (eClass.getName().contentEquals(className)){
					EList<EAnnotation> value = eClass.getEAnnotations();
					for (EAnnotation workingValue : value){
						EMap<String, String> keyMap = workingValue.getDetails();	
						if (keyMap.containsKey("policyTemplate")){
							return true;
						}
					}	
				}
			}
		}
		return false;
	}
	private String getSubTypes(EPackage root, String className) {
		String returnSubTypes = null;
		for (EClassifier classifier : root.getEClassifiers()){ 
			if (classifier instanceof EClass) { 
				EClass eClass = (EClass)classifier; 

				for (EClass eSuperType : eClass.getEAllSuperTypes()) 
				{ 
					if (eClass.getName().contentEquals(className)){
						returnSubTypes = eSuperType.getName();
					}
				} 
			} 
		} 
		return returnSubTypes;
	} 

	public HashMap<String, String> getAttributeList(EPackage root, String className, String superClass){

		TreeIterator<EObject> treeItr = root.eAllContents();
		boolean requiredAttribute = false; 
		HashMap<String, String> refAttribute = new HashMap<String, String>();
		boolean annotation = false;
		boolean dictionaryTest = false;
		String defaultValue = null;
		String eType = null;

		//    Pulling out dependency from file
		while (treeItr.hasNext()) {	    
			EObject obj = treeItr.next();
			if (obj instanceof EClassifier) {
				if (isRequiredAttribute(obj,  className ) || isRequiredAttribute(obj,  superClass )){
					requiredAttribute = true;
				}else {
					requiredAttribute = false;
				}

			}

			if (requiredAttribute){
				if (obj instanceof EStructuralFeature) {
					EStructuralFeature eStrucClassifier = (EStructuralFeature) obj;
					if (eStrucClassifier.getEAnnotations().size() != 0) {
						annotation = annotationTest(eStrucClassifier, configuration, ecomp);
						dictionaryTest = annotationTest(eStrucClassifier, dictionary, policy);
						EClassifier refType = ((EStructuralFeature) obj).getEType();
						if (annotation && !(obj instanceof EReference) && !(refType instanceof EEnumImpl)) {
							String name = eStrucClassifier.getName();
							if (dictionaryTest){
								eType = annotationValue(eStrucClassifier, ANNOTATION_TYPE.DICTIONARY, policy);
							}else {
								eType = eStrucClassifier.getEType().getInstanceClassName();
							}
							defaultValue = checkDefultValue(((EStructuralFeature) obj).getDefaultValueLiteral());

							String array = arrayCheck(((EStructuralFeature) obj).getUpperBound());
							String required = checkRequiredPattern(((EStructuralFeature) obj).getUpperBound(), ((EStructuralFeature) obj).getLowerBound());
							String attributeValue =  eType + defaultValue + required + array;
							refAttribute.put(name, attributeValue);	
						}
					}
				}
			}
		}
		return refAttribute;

	}

	public String arrayCheck(int upperBound) {

		if (upperBound == -1){
			return ":MANY-true";
		}

		return ":MANY-false";
	}

	public List<String> getDependencyList(EClassifier eClassifier, String className){
		List<String> returnValue = new ArrayList<>();;
		EList<EClass> somelist = ((EClass) eClassifier).getEAllSuperTypes();
		if (somelist.isEmpty()){
			return returnValue;
		}
		for(EClass depend: somelist){
			if (depend.toString().contains(eProxyURI)){
				String one = depend.toString().split(eProxyURI)[1];
				String value = StringUtils.replaceEach(one.split("#")[1], new String[]{"//", ")"}, new String[]{"", ""});
				returnValue.add(value);
			}
		}

		return returnValue;
	}

	public Map<String, String> buildSubList(HashMap<String, String> subClassAttributes, HashMap<String, MSAttributeObject> classMap, String className){
		Map<String, String> missingValues = new HashMap<String, String>();
		Map<String, String> workingMap = new HashMap<String, String>();
		boolean enumType;

		for ( Entry<String, String> map : classMap.get(className).getRefAttribute().entrySet()){
			String value = map.getValue().split(":")[0];
			if (value!=null){
				classMap.get(className).getEnumType();
				enumType = classMap.get(className).getEnumType().containsKey(value);
				if (!enumType){
					workingMap =  classMap.get(value).getRefAttribute();
					for ( Entry<String, String> subMab : workingMap.entrySet()){
						String value2 = subMab.getValue().split(":")[0];
						if (!subClassAttributes.containsValue(value2)){
							missingValues.put(subMab.getKey(), subMab.getValue());
						}
					}

				}
			}
		}

		return missingValues;
	}

	public Map<String, HashMap<String, String>> recursiveReference(HashMap<String, MSAttributeObject> classMap, String className){

		Map<String, HashMap<String, String>> returnObject = new HashMap<String, HashMap<String, String>>();
		HashMap<String, String> returnClass = getRefclass(classMap, className);
		returnObject.put(className, returnClass);
		for (Entry<String, String> reAttribute :returnClass.entrySet()){
			if (reAttribute.getValue().split(":")[1].contains("MANY")){
				if (classMap.get(reAttribute.getValue().split(":")[0]) != null){
					returnObject.putAll(recursiveReference(classMap, reAttribute.getValue().split(":")[0]));
				}
			}

		}

		return returnObject;

	}

	public String createJson(HashMap<String, String> subClassAttributes, HashMap<String, MSAttributeObject> classMap, String className) { 
		boolean enumType;
		Map<String, HashMap<String, String>> myObject = new HashMap<String, HashMap<String, String>>();
		for ( Entry<String, String> map : classMap.get(className).getRefAttribute().entrySet()){
			String value = map.getValue().split(":")[0];
			if (value!=null){
				enumType = classMap.get(className).getEnumType().containsKey(value);
				if (!enumType){
					if (map.getValue().split(":")[1].contains("MANY")){
						Map<String, HashMap<String, String>> testRecursive = recursiveReference(classMap, map.getValue().split(":")[0] );
						myObject.putAll(testRecursive);
					}
				}
			}
		}

		Gson gson = new Gson(); 
		String json = gson.toJson(myObject); 

		return json;		
	}

	public HashMap<String, String> getRefclass(HashMap<String, MSAttributeObject> classMap, String className){
		HashMap<String, String> missingValues = new HashMap<String, String>();

		if (classMap.get(className).getAttribute()!=null || !classMap.get(className).getAttribute().isEmpty()){
			missingValues.putAll(classMap.get(className).getAttribute());
		}

		if (classMap.get(className).getRefAttribute()!=null || !classMap.get(className).getRefAttribute().isEmpty()){
			missingValues.putAll(classMap.get(className).getRefAttribute());
		}

		return missingValues;	
	}

	public String createSubAttributes(ArrayList<String> dependency, HashMap<String, MSAttributeObject> classMap, String modelName) {

		HashMap <String,  String>  workingMap = new HashMap<String,String>();
		MSAttributeObject tempObject = new MSAttributeObject();
		if (dependency!=null){
			if (dependency.size()==0){
				return "{}";
			}	
			dependency.add(modelName);
			for (String element: dependency){
				tempObject = classMap.get(element);
				if (tempObject!=null){
					workingMap.putAll(classMap.get(element).getSubClass());
				}
			}
		}

		String returnValue = createJson(workingMap, classMap, modelName);			
		return returnValue;
	}

	public ArrayList<String> getFullDependencyList(ArrayList<String> dependency, HashMap<String,MSAttributeObject > classMap) {
		ArrayList<String> returnList = new ArrayList<String>();
		ArrayList<String> workingList = new ArrayList<String>();
		returnList.addAll(dependency);
		for (String element : dependency ){
			if (classMap.containsKey(element)){
				MSAttributeObject value = classMap.get(element);
				String rawValue = StringUtils.replaceEach(value.getDependency(), new String[]{"[", "]"}, new String[]{"", ""});
				workingList = new ArrayList<String>(Arrays.asList(rawValue.split(",")));	
				for(String depend : workingList){
					if (!returnList.contains(depend) && !depend.isEmpty()){
						returnList.add(depend.trim());
					}
				}
			}
		}

		return returnList;
	}
}
