/*-
 * ============LICENSE_START=======================================================
 * ECOMP-PAP-REST
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

package org.openecomp.policy.pap.xacml.rest.components;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.Matcher; 

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressionsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOfType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOfType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentExpressionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeDesignatorType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.MatchType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RuleType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.TargetType;

import org.apache.commons.io.FilenameUtils;
import org.openecomp.policy.pap.xacml.rest.adapters.PolicyRestAdapter;
import org.openecomp.policy.rest.XACMLRestProperties;

import com.att.research.xacml.std.IdentifierImpl;
import com.att.research.xacml.util.XACMLProperties;

import org.openecomp.policy.common.logging.eelf.MessageCodes;
import org.openecomp.policy.common.logging.eelf.PolicyLogger;
import org.openecomp.policy.common.logging.flexlogger.FlexLogger; 
import org.openecomp.policy.common.logging.flexlogger.Logger; 

public class CreateBrmsParamPolicy extends Policy {
	/**
	 * Config Fields
	 */
	private static final Logger logger = FlexLogger
			.getLogger(CreateBrmsParamPolicy.class);

	/*
	 * These are the parameters needed for DB access from the PAP
	 */
	private static String papDbDriver = null;
	private static String papDbUrl = null;
	private static String papDbUser = null;
	private static String papDbPassword = null;

	public CreateBrmsParamPolicy() {
		super();
	}

	public CreateBrmsParamPolicy(PolicyRestAdapter policyAdapter) {
		this.policyAdapter = policyAdapter;
		this.policyAdapter.setConfigType(policyAdapter.getConfigType());

	}
	
	public String expandConfigBody(String ruleContents,  
										Map<String, String> brmsParamBody
										  ) { 
			 
			Set<String> keySet= new HashSet<String>();
			
			Map<String,String> copyMap=new HashMap<>();
			copyMap.putAll(brmsParamBody);
			copyMap.put("policyName", policyAdapter.getPolicyName());
			copyMap.put("policyScope", policyAdapter.getPolicyScope());
			copyMap.put("policyVersion",policyAdapter.getHighestVersion().toString());
			
			//Finding all the keys in the Map data-structure.
			keySet= copyMap.keySet();
			Iterator<String> iterator = keySet.iterator(); 
			Pattern p;
			Matcher m;
			while(iterator.hasNext()) {
				//Converting the first character of the key into a lower case. 
				String input= iterator.next();
				String output  = Character.toLowerCase(input.charAt(0)) +
		                   (input.length() > 1 ? input.substring(1) : "");
				//Searching for a pattern in the String using the key. 
				p=Pattern.compile("\\$\\{"+output+"\\}");	
				m=p.matcher(ruleContents);
				//Replacing the value with the inputs provided by the user in the editor. 
				String finalInput = copyMap.get(input);
				if(finalInput.contains("$")){
					finalInput = finalInput.replace("$", "\\$");
				}
				ruleContents=m.replaceAll(finalInput);
			}
			System.out.println(ruleContents);
			return ruleContents; 
	} 
			 
	// Saving the Configurations file at server location for config policy.
	protected void saveConfigurations(String policyName, String prevPolicyName,
			String ruleBody) {
		final Path gitPath = Paths.get(policyAdapter.getUserGitPath()
				.toString());
		String policyDir = policyAdapter.getParentPath().toString();
		int startIndex = policyDir.indexOf(gitPath.toString())
				+ gitPath.toString().length() + 1;
		policyDir = policyDir.substring(startIndex, policyDir.length());
		logger.info("print the main domain value" + policyDir);
		String path = policyDir.replace('\\', '.');
		if (path.contains("/")) {
			path = policyDir.replace('/', '.');
			logger.info("print the path:" + path);
		}

		
			String configFileName = getConfigFile(policyName);
        try{
			// Getting the previous policy Config Json file to be used for
			// updating the dictionary tables
			if (policyAdapter.isEditPolicy()) {

				String prevConfigFileName = getConfigFile(prevPolicyName);

				File oldFile;
				if (CONFIG_HOME.contains("\\")) {
					oldFile = new File(CONFIG_HOME + "\\" + path + "."
							+ prevConfigFileName);
				} else {
					oldFile = new File(CONFIG_HOME + "/" + path + "."
							+ prevConfigFileName);
				}

				String filepath = oldFile.toString();

				String prevJsonBody = readFile(filepath, StandardCharsets.UTF_8);
				policyAdapter.setPrevJsonBody(prevJsonBody);
			}

			File configHomeDir = new File(CONFIG_HOME);
			File[] listOfFiles = configHomeDir.listFiles();
			if (listOfFiles != null) {
				for (File eachFile : listOfFiles) {
					if (eachFile.isFile()) {
						String fileNameWithoutExtension = FilenameUtils
								.removeExtension(eachFile.getName());
						String configFileNameWithoutExtension = FilenameUtils
								.removeExtension(configFileName);
						if (fileNameWithoutExtension
								.equals(configFileNameWithoutExtension)) {
							// delete the file
							eachFile.delete();
						}
					}
				}
			}
        }
        catch(IOException e){
        	
        }
			try {
				
				if (policyName.endsWith(".xml")) {
					policyName = policyName.substring(0,
							policyName.lastIndexOf(".xml"));
				}
				PrintWriter out = new PrintWriter(CONFIG_HOME + File.separator
						+ path + "." + policyName + ".txt");
				String expandedBody=expandConfigBody(ruleBody,policyAdapter.getBrmsParamBody());
				out.println(expandedBody);
				out.close();

			} catch (Exception e) {
				//TODO:EELF Cleanup - Remove logger
				//logger.error(XACMLErrorConstants.ERROR_PROCESS_FLOW + e);
				PolicyLogger.error(MessageCodes.ERROR_PROCESS_FLOW, e, "CreateBrmsParamPolicy", "Exception saving configuration file");
			}
	}

	// Utility to read json data from the existing file to a string
	static String readFile(String path, Charset encoding) throws IOException {

		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);

	}

	// Here we are adding the extension for the configurations file based on the
	// config type selection for saving.
	private String getConfigFile(String filename) {
		filename = FilenameUtils.removeExtension(filename);
		if (filename.endsWith(".txt")) {
			filename = filename.substring(0, filename.length() - 3);
		}

		filename = filename + ".txt";
		return filename;
	}

	// Validations for Config form
	public boolean validateConfigForm() {

		// Validating mandatory Fields.
		isValidForm = true;
		return isValidForm;

	}

	@Override
	public Map<String, String> savePolicies() throws Exception {
		
		Map<String, String> successMap = new HashMap<String,String>();
		if(isPolicyExists()){
			successMap.put("EXISTS", "This Policy already exist on the PAP");
			return successMap;
		}
		
		if (!isPreparedToSave()) {
			prepareToSave();
		}
		// Until here we prepared the data and here calling the method to create
		// xml.
		Path newPolicyPath = null;
		newPolicyPath = Paths.get(policyAdapter.getParentPath().toString(),
				policyName);
		
		Boolean dbIsUpdated = true;

		successMap = new HashMap<String, String>();
		if (dbIsUpdated) {
			successMap = createPolicy(newPolicyPath,
					getCorrectPolicyDataObject());
		} else {
			//TODO:EELF Cleanup - Remove logger
			//logger.error("Failed to Update the Database Dictionary Tables.");
			PolicyLogger.error("Failed to Update the Database Dictionary Tables.");

			// remove the new json file
			String jsonBody = policyAdapter.getPrevJsonBody();
			saveConfigurations(policyName, "", jsonBody);
			successMap.put("error", "DB UPDATE");
		}

		if (successMap.containsKey("success")) {
			Path finalPolicyPath = getFinalPolicyPath();
			policyAdapter.setFinalPolicyPath(finalPolicyPath.toString());
		}
		return successMap;
	}
	
	private String getValueFromDictionary(String templateName){
		
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		
		/*
		 * Retrieve the property values for db access from the xacml.pap.properties
		 */
		papDbDriver = XACMLProperties.getProperty(XACMLRestProperties.PROP_PAP_DB_DRIVER);
		papDbUrl = XACMLProperties.getProperty(XACMLRestProperties.PROP_PAP_DB_URL);
		papDbUser = XACMLProperties.getProperty(XACMLRestProperties.PROP_PAP_DB_USER);
		papDbPassword = XACMLProperties.getProperty(XACMLRestProperties.PROP_PAP_DB_PASSWORD);
		
		String ruleTemplate=null;
		
		try {
			//Get DB Connection
			Class.forName(papDbDriver);
			con = DriverManager.getConnection(papDbUrl,papDbUser,papDbPassword);
			st = con.createStatement();
			
			String queryString="select rule from BRMSParamTemplate where param_template_name=\"";
			queryString=queryString+templateName+"\"";
			
			rs = st.executeQuery(queryString);
			if(rs.next()){
				ruleTemplate=rs.getString("rule");
			}
			rs.close();
		}catch (ClassNotFoundException e) {
			//TODO:EELF Cleanup - Remove logger
			//logger.error(e.getMessage());
			PolicyLogger.error(MessageCodes.EXCEPTION_ERROR, e, "CreateBrmsParamPolicy", "Exception querying BRMSParamTemplate");
			System.out.println(e.getMessage());

		} catch (SQLException e) {
			//TODO:EELF Cleanup - Remove logger
			//logger.error(e.getMessage());
			PolicyLogger.error(MessageCodes.EXCEPTION_ERROR, e, "CreateBrmsParamPolicy", "Exception querying BRMSParamTemplate");
			System.out.println(e.getMessage());
		} finally {
			try{
				if (con!=null) con.close();
				if (rs!=null) rs.close();
				if (st!=null) st.close();
			} catch (Exception ex){}
		}
		return ruleTemplate;
		
	}
	
	protected Map<String, String> findType(String rule) {
		Map<String, String> mapFieldType= new HashMap<String,String>();
		if(rule!=null){
			try {
				String params = "";
				Boolean flag = false;
				Boolean comment = false;
				String lines[] = rule.split("\n");
				for(String line : lines){
					if (line.isEmpty() || line.startsWith("//")) {
						continue;
					}
					if (line.startsWith("/*")) {
						comment = true;
						continue;
					}
					if (line.contains("//")) {
						if(!(line.contains("http://") || line.contains("https://"))){
							line = line.split("\\/\\/")[0];
						}
					}
					if (line.contains("/*")) {
						comment = true;
						if (line.contains("*/")) {
							try {
								comment = false;
								line = line.split("\\/\\*")[0]
										+ line.split("\\*\\/")[1].replace("*/", "");
							} catch (Exception e) {
								line = line.split("\\/\\*")[0];
							}
						} else {
							line = line.split("\\/\\*")[0];
						}
					}
					if (line.contains("*/")) {
						comment = false;
						try {
							line = line.split("\\*\\/")[1].replace("*/", "");
						} catch (Exception e) {
							line = "";
						}
					}
					if (comment) {
						continue;
					}
					if (flag) {
						params = params + line;
					}
					if (line.contains("declare Params")) {
						params = params + line;
						flag = true;
					}
					if (line.contains("end") && flag) {
						break;
					}
				}
				params = params.replace("declare Params", "").replace("end", "")
						.replaceAll("\\s+", "");
				String[] components = params.split(":");
				String caption = "";
				for (int i = 0; i < components.length; i++) {
					String type = "";
					if (i == 0) {
						caption = components[i];
					}
					if(caption.equals("")){
						break;
					}
					String nextComponent = "";
					try {
						nextComponent = components[i + 1];
					} catch (Exception e) {
						nextComponent = components[i];
					}
					//If the type is of type String then we add the UI Item and type to the map. 
					if (nextComponent.startsWith("String")) {
						type = "String";
						mapFieldType.put(caption, type);
						caption = nextComponent.replace("String", "");
					} else if (nextComponent.startsWith("int")) {
						type = "int";
						mapFieldType.put(caption, type);
						caption = nextComponent.replace("int", "");
					}
				}
			} catch (Exception e) {
				//TODO:EELF Cleanup - Remove logger
				//logger.error(XACMLErrorConstants.ERROR_SYSTEM_ERROR + e);
				PolicyLogger.error(MessageCodes.ERROR_SYSTEM_ERROR, e, "CreateBrmsParamPolicy", "Exception parsing file in findType");
			}
		}
		return mapFieldType;
	}
	
	// This is the method for preparing the policy for saving. We have broken it
	// out
	// separately because the fully configured policy is used for multiple
	// things
	@Override
	public boolean prepareToSave() throws Exception {
		
		if (isPreparedToSave()) {
			// we have already done this
			return true;
		}

		int version = 0;
		String policyID = policyAdapter.getPolicyID();

		if (policyAdapter.isEditPolicy()) {
			// version = Integer.parseInt(policyAdapter.getVersion()) + 1;
			version = policyAdapter.getHighestVersion() + 1;
		} else {
			version = 1;
		}

		// Create the Instance for pojo, PolicyType object is used in
		// marshalling.
		if (policyAdapter.getPolicyType().equals("Config")) {
			PolicyType policyConfig = new PolicyType();

			policyConfig.setVersion(Integer.toString(version));
			policyConfig.setPolicyId(policyID);
			policyConfig.setTarget(new TargetType());
			policyAdapter.setData(policyConfig);
		}

		if (policyAdapter.getData() != null) {

			// Save off everything
			// making ready all the required elements to generate the action
			// policy xml.
			// Get the uniqueness for policy name.
			String prevPolicyName = null;
			if (policyAdapter.isEditPolicy()) {
				prevPolicyName = "Config_BRMS_Param_" + policyAdapter.getPolicyName()
						+ "." + policyAdapter.getHighestVersion() + ".xml";
			}

			Path newFile = getNextFilename(
					Paths.get(policyAdapter.getParentPath().toString()),
					(policyAdapter.getPolicyType() + "_BRMS_Param"),
					policyAdapter.getPolicyName(), version);

			if (newFile == null) {
				//TODO:EELF Cleanup - Remove logger
				//logger.error("Policy already Exists, cannot create the policy.");
				PolicyLogger.error("Policy already Exists, cannot create the policy.");
				setPolicyExists(true);
				return false;			
			}
			policyName = newFile.getFileName().toString();
			
				
			Map<String,String> ruleAndUIValue= policyAdapter.getBrmsParamBody();
			String tempateValue= ruleAndUIValue.get("templateName");
			String valueFromDictionary= getValueFromDictionary(tempateValue);
			
			//Get the type of the UI Fields. 
			Map<String,String> typeOfUIField=findType(valueFromDictionary);
			String generatedRule=null;
			String body = "";
			
			try {
				
				try {
					body = "/* Autogenerated Code Please Don't change/remove this comment section. This is for the UI purpose. \n\t " +
								"<$%BRMSParamTemplate=" + tempateValue + "%$> \n */ \n";
					body = body +  valueFromDictionary + "\n";
					generatedRule = "rule \"Params\" \n\tsalience 1000 \n\twhen\n\tthen\n\t\tParams params = new Params();";
					
					//We first read the map data structure(ruleAndUIValue) received from the PAP-ADMIN
					//We ignore if the key is "templateName as we are interested only in the UI fields and its value. 
					//We have one more map data structure(typeOfUIField) created by parsing the Drools rule. 
					//From the type of the UI field(String/int) we structure whether to put the "" or not. 
					for (Map.Entry<String, String> entry : ruleAndUIValue.entrySet()) {
						if(entry.getKey()!="templateName")
						{
							for(Map.Entry<String, String> fieldType:typeOfUIField.entrySet())
							{
								if(fieldType.getKey().equalsIgnoreCase(entry.getKey()))
								{
									String key = entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1); 
									if(fieldType.getValue()=="String")
									{
										//Type is String
										generatedRule = generatedRule + "\n\t\tparams.set"
												+ key + "(\""
												+ entry.getValue() + "\");";
									}
									else{
										generatedRule = generatedRule + "\n\t\tparams.set"
												+ key  + "("
												+  entry.getValue() + ");";
									}
								}
							}
						}
					}
					
					generatedRule = generatedRule
							+ "\n\t\tinsert(params);\nend";
					logger.info("New rule generated with :" + generatedRule);
					body = body + generatedRule;
				} catch (Exception e) {
					//TODO:EELF Cleanup - Remove logger
					//logger.error(XACMLErrorConstants.ERROR_PROCESS_FLOW + e);
					PolicyLogger.error(MessageCodes.ERROR_PROCESS_FLOW, e, "CreateBrmsParamPolicy", "Exception saving policy");
				}
			}
			catch (Exception e) {
				//TODO:EELF Cleanup - Remove logger
				//logger.error(XACMLErrorConstants.ERROR_PROCESS_FLOW + e);
				PolicyLogger.error(MessageCodes.ERROR_PROCESS_FLOW, e, "CreateBrmsParamPolicy", "Exception saving policy");
			}
			
			saveConfigurations(policyName,prevPolicyName,body);

			// Make sure the filename ends with an extension
			if (policyName.endsWith(".xml") == false) {
				policyName = policyName + ".xml";
			}

			PolicyType configPolicy = (PolicyType) policyAdapter.getData();

			configPolicy.setDescription(policyAdapter.getPolicyDescription());

			configPolicy.setRuleCombiningAlgId(policyAdapter
					.getRuleCombiningAlgId());

			AllOfType allOfOne = new AllOfType();
			File policyFilePath = new File(policyAdapter.getParentPath()
					.toString(), policyName);
			String policyDir = policyFilePath.getParentFile().getName();
			String fileName = FilenameUtils.removeExtension(policyName);
			fileName = policyDir + "." + fileName + ".xml";
			String name = fileName.substring(fileName.lastIndexOf("\\") + 1,
					fileName.length());
			if ((name == null) || (name.equals(""))) {
				name = fileName.substring(fileName.lastIndexOf("/") + 1,
						fileName.length());
			}
			allOfOne.getMatch().add(createMatch("PolicyName", name));
			
			
			AllOfType allOf = new AllOfType();

			// Match for ECOMPName
			allOf.getMatch().add(
					createMatch("ECOMPName", policyAdapter.getEcompName()));
			allOf.getMatch().add(
					createMatch("ConfigName", policyAdapter.getConfigName()));
			// Match for riskType
			allOf.getMatch().add(
					createDynamicMatch("RiskType", policyAdapter.getRiskType()));
			// Match for riskLevel
			allOf.getMatch().add(
					createDynamicMatch("RiskLevel", String.valueOf(policyAdapter.getRiskLevel())));
			// Match for riskguard
			allOf.getMatch().add(
					createDynamicMatch("guard", policyAdapter.getGuard()));
			// Match for ttlDate
			allOf.getMatch().add(
					createDynamicMatch("TTLDate", policyAdapter.getTtlDate()));
			AnyOfType anyOf = new AnyOfType();
			anyOf.getAllOf().add(allOfOne);
			anyOf.getAllOf().add(allOf);

			TargetType target = new TargetType();
			((TargetType) target).getAnyOf().add(anyOf);

			// Adding the target to the policy element
			configPolicy.setTarget((TargetType) target);

			RuleType rule = new RuleType();
			rule.setRuleId(policyAdapter.getRuleID());

			rule.setEffect(EffectType.PERMIT);

			// Create Target in Rule
			AllOfType allOfInRule = new AllOfType();

			// Creating match for ACCESS in rule target
			MatchType accessMatch = new MatchType();
			AttributeValueType accessAttributeValue = new AttributeValueType();
			accessAttributeValue.setDataType(STRING_DATATYPE);
			accessAttributeValue.getContent().add("ACCESS");
			accessMatch.setAttributeValue(accessAttributeValue);
			AttributeDesignatorType accessAttributeDesignator = new AttributeDesignatorType();
			URI accessURI = null;
			try {
				accessURI = new URI(ACTION_ID);
			} catch (URISyntaxException e) {
				//TODO:EELF Cleanup - Remove logger
				//logger.error(XACMLErrorConstants.ERROR_DATA_ISSUE
						//+ e.getStackTrace());
				PolicyLogger.error(MessageCodes.ERROR_DATA_ISSUE, e, "CreateBrmsParamPolicy", "Exception creating ACCESS URI");
			}
			accessAttributeDesignator.setCategory(CATEGORY_ACTION);
			accessAttributeDesignator.setDataType(STRING_DATATYPE);
			accessAttributeDesignator.setAttributeId(new IdentifierImpl(
					accessURI).stringValue());
			accessMatch.setAttributeDesignator(accessAttributeDesignator);
			accessMatch.setMatchId(FUNCTION_STRING_EQUAL_IGNORE);

			// Creating Config Match in rule Target
			MatchType configMatch = new MatchType();
			AttributeValueType configAttributeValue = new AttributeValueType();
			configAttributeValue.setDataType(STRING_DATATYPE);

			configAttributeValue.getContent().add("Config");

			configMatch.setAttributeValue(configAttributeValue);
			AttributeDesignatorType configAttributeDesignator = new AttributeDesignatorType();
			URI configURI = null;
			try {
				configURI = new URI(RESOURCE_ID);
			} catch (URISyntaxException e) {
				//TODO:EELF Cleanup - Remove logger
				//logger.error(XACMLErrorConstants.ERROR_DATA_ISSUE
						//+ e.getStackTrace());
				PolicyLogger.error(MessageCodes.ERROR_DATA_ISSUE, e, "CreateBrmsParamPolicy", "Exception creating Config URI");
			}

			configAttributeDesignator.setCategory(CATEGORY_RESOURCE);
			configAttributeDesignator.setDataType(STRING_DATATYPE);
			configAttributeDesignator.setAttributeId(new IdentifierImpl(
					configURI).stringValue());
			configMatch.setAttributeDesignator(configAttributeDesignator);
			configMatch.setMatchId(FUNCTION_STRING_EQUAL_IGNORE);

			allOfInRule.getMatch().add(accessMatch);
			allOfInRule.getMatch().add(configMatch);

			AnyOfType anyOfInRule = new AnyOfType();
			anyOfInRule.getAllOf().add(allOfInRule);

			TargetType targetInRule = new TargetType();
			targetInRule.getAnyOf().add(anyOfInRule);

			rule.setTarget(targetInRule);
			rule.setAdviceExpressions(getAdviceExpressions(version, policyName));

			configPolicy
					.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition()
					.add(rule);
			policyAdapter.setPolicyData(configPolicy);

		} else {
			//TODO:EELF Cleanup - Remove logger
			//logger.error("Unsupported data object."
					//+ policyAdapter.getData().getClass().getCanonicalName());
			PolicyLogger.error("Unsupported data object."
					+ policyAdapter.getData().getClass().getCanonicalName());
		}
		setPreparedToSave(true);
		return true;
	}

	// Data required for Advice part is setting here.
	private AdviceExpressionsType getAdviceExpressions(int version,
			String fileName) {

		//Policy Config ID Assignment
		AdviceExpressionsType advices = new AdviceExpressionsType();
		AdviceExpressionType advice = new AdviceExpressionType();
		advice.setAdviceId("BRMSPARAMID");
		advice.setAppliesTo(EffectType.PERMIT);
		// For Configuration
		AttributeAssignmentExpressionType assignment1 = new AttributeAssignmentExpressionType();
		assignment1.setAttributeId("type");
		assignment1.setCategory(CATEGORY_RESOURCE);
		assignment1.setIssuer("");
		AttributeValueType configNameAttributeValue = new AttributeValueType();
		configNameAttributeValue.setDataType(STRING_DATATYPE);
		configNameAttributeValue.getContent().add("Configuration");
		assignment1.setExpression(new ObjectFactory()
				.createAttributeValue(configNameAttributeValue));
		advice.getAttributeAssignmentExpression().add(assignment1);

		// For Config file Url if configurations are provided.
		// URL ID Assignment
		final Path gitPath = Paths.get(policyAdapter.getUserGitPath()
				.toString());
		AttributeAssignmentExpressionType assignment2 = new AttributeAssignmentExpressionType();
		assignment2.setAttributeId("URLID");
		assignment2.setCategory(CATEGORY_RESOURCE);
		assignment2.setIssuer("");
		AttributeValueType AttributeValue = new AttributeValueType();
		AttributeValue.setDataType(URI_DATATYPE);
		String policyDir1 = policyAdapter.getParentPath().toString();
		int startIndex1 = policyDir1.indexOf(gitPath.toString())
				+ gitPath.toString().length() + 1;
		policyDir1 = policyDir1.substring(startIndex1, policyDir1.length());
		logger.info("print the main domain value" + policyDir1);
		String path = policyDir1.replace('\\', '.');
		if (path.contains("/")) {
			path = policyDir1.replace('/', '.');
			logger.info("print the path:" + path);
		}
		String content = CONFIG_URL + "/Config/" + path + "."
				+ getConfigFile(policyName);

		AttributeValue.getContent().add(content);
		assignment2.setExpression(new ObjectFactory()
				.createAttributeValue(AttributeValue));
		advice.getAttributeAssignmentExpression().add(assignment2);

		// Policy Name Assignment
		AttributeAssignmentExpressionType assignment3 = new AttributeAssignmentExpressionType();
		assignment3.setAttributeId("PolicyName");
		assignment3.setCategory(CATEGORY_RESOURCE);
		assignment3.setIssuer("");
		AttributeValueType attributeValue3 = new AttributeValueType();
		attributeValue3.setDataType(STRING_DATATYPE);
		String policyDir = policyAdapter.getParentPath().toString();
		int startIndex = policyDir.indexOf(gitPath.toString())
				+ gitPath.toString().length() + 1;
		policyDir = policyDir.substring(startIndex, policyDir.length());
		StringTokenizer tokenizer = null;
		StringBuffer buffer = new StringBuffer();
		if (policyDir.contains("\\")) {
			tokenizer = new StringTokenizer(policyDir, "\\");
		} else {
			tokenizer = new StringTokenizer(policyDir, "/");
		}
		if (tokenizer != null) {
			while (tokenizer.hasMoreElements()) {
				String value = tokenizer.nextToken();
				buffer.append(value);
				buffer.append(".");
			}
		}
		fileName = FilenameUtils.removeExtension(fileName);
		fileName = buffer.toString() + fileName + ".xml";
		System.out.println(fileName);
		String name = fileName.substring(fileName.lastIndexOf("\\") + 1,
				fileName.length());
		if ((name == null) || (name.equals(""))) {
			name = fileName.substring(fileName.lastIndexOf("/") + 1,
					fileName.length());
		}
		System.out.println(name);
		attributeValue3.getContent().add(name);
		assignment3.setExpression(new ObjectFactory()
				.createAttributeValue(attributeValue3));
		advice.getAttributeAssignmentExpression().add(assignment3);

		// Version Number Assignment
		AttributeAssignmentExpressionType assignment4 = new AttributeAssignmentExpressionType();
		assignment4.setAttributeId("VersionNumber");
		assignment4.setCategory(CATEGORY_RESOURCE);
		assignment4.setIssuer("");
		AttributeValueType configNameAttributeValue4 = new AttributeValueType();
		configNameAttributeValue4.setDataType(STRING_DATATYPE);
		configNameAttributeValue4.getContent().add(Integer.toString(version));
		assignment4.setExpression(new ObjectFactory()
				.createAttributeValue(configNameAttributeValue4));
		advice.getAttributeAssignmentExpression().add(assignment4);

		// Ecomp Name Assignment
		AttributeAssignmentExpressionType assignment5 = new AttributeAssignmentExpressionType();
		assignment5.setAttributeId("matching:" + this.ECOMPID);
		assignment5.setCategory(CATEGORY_RESOURCE);
		assignment5.setIssuer("");
		AttributeValueType configNameAttributeValue5 = new AttributeValueType();
		configNameAttributeValue5.setDataType(STRING_DATATYPE);
		configNameAttributeValue5.getContent().add(policyAdapter.getEcompName());
		assignment5.setExpression(new ObjectFactory()
				.createAttributeValue(configNameAttributeValue5));
		advice.getAttributeAssignmentExpression().add(assignment5);
		
		
		//Config Name Assignment
		AttributeAssignmentExpressionType assignment6 = new AttributeAssignmentExpressionType();
		assignment6.setAttributeId("matching:" + this.CONFIGID);
		assignment6.setCategory(CATEGORY_RESOURCE);
		assignment6.setIssuer("");
		AttributeValueType configNameAttributeValue6 = new AttributeValueType();
		configNameAttributeValue6.setDataType(STRING_DATATYPE);
		configNameAttributeValue6.getContent().add(policyAdapter.getConfigName());
		assignment6.setExpression(new ObjectFactory().createAttributeValue(configNameAttributeValue6));
		advice.getAttributeAssignmentExpression().add(assignment6);
		
		Map<String, String> dynamicFieldConfigAttributes = policyAdapter.getDynamicFieldConfigAttributes();
		for (String keyField : dynamicFieldConfigAttributes.keySet()) {
			String key = keyField;
			String value = dynamicFieldConfigAttributes.get(key);
			AttributeAssignmentExpressionType assignment7 = new AttributeAssignmentExpressionType();
			assignment7.setAttributeId("key:" + key);
			assignment7.setCategory(CATEGORY_RESOURCE);
			assignment7.setIssuer("");

			AttributeValueType configNameAttributeValue7 = new AttributeValueType();
			configNameAttributeValue7.setDataType(STRING_DATATYPE);
			configNameAttributeValue7.getContent().add(value);
			assignment7.setExpression(new ObjectFactory().createAttributeValue(configNameAttributeValue7));

			advice.getAttributeAssignmentExpression().add(assignment7);
		}
		
		//Risk Attributes
		AttributeAssignmentExpressionType assignment8 = new AttributeAssignmentExpressionType();
		assignment8.setAttributeId("RiskType");
		assignment8.setCategory(CATEGORY_RESOURCE);
		assignment8.setIssuer("");

		AttributeValueType configNameAttributeValue8 = new AttributeValueType();
		configNameAttributeValue8.setDataType(STRING_DATATYPE);
		configNameAttributeValue8.getContent().add(policyAdapter.getRiskType());
		assignment8.setExpression(new ObjectFactory().createAttributeValue(configNameAttributeValue8));

		advice.getAttributeAssignmentExpression().add(assignment8);
		
		AttributeAssignmentExpressionType assignment9 = new AttributeAssignmentExpressionType();
		assignment9.setAttributeId("RiskLevel");
		assignment9.setCategory(CATEGORY_RESOURCE);
		assignment9.setIssuer("");

		AttributeValueType configNameAttributeValue9 = new AttributeValueType();
		configNameAttributeValue9.setDataType(STRING_DATATYPE);
		configNameAttributeValue9.getContent().add(policyAdapter.getRiskLevel());
		assignment9.setExpression(new ObjectFactory().createAttributeValue(configNameAttributeValue9));

		advice.getAttributeAssignmentExpression().add(assignment9);	

		AttributeAssignmentExpressionType assignment10 = new AttributeAssignmentExpressionType();
		assignment10.setAttributeId("guard");
		assignment10.setCategory(CATEGORY_RESOURCE);
		assignment10.setIssuer("");

		AttributeValueType configNameAttributeValue10 = new AttributeValueType();
		configNameAttributeValue10.setDataType(STRING_DATATYPE);
		configNameAttributeValue10.getContent().add(policyAdapter.getGuard());
		assignment10.setExpression(new ObjectFactory().createAttributeValue(configNameAttributeValue10));

		advice.getAttributeAssignmentExpression().add(assignment10);

		AttributeAssignmentExpressionType assignment11 = new AttributeAssignmentExpressionType();
		assignment11.setAttributeId("TTLDate");
		assignment11.setCategory(CATEGORY_RESOURCE);
		assignment11.setIssuer("");

		AttributeValueType configNameAttributeValue11 = new AttributeValueType();
		configNameAttributeValue11.setDataType(STRING_DATATYPE);
		configNameAttributeValue11.getContent().add(policyAdapter.getTtlDate());
		assignment11.setExpression(new ObjectFactory().createAttributeValue(configNameAttributeValue11));

		advice.getAttributeAssignmentExpression().add(assignment11);

		advices.getAdviceExpression().add(advice);
		return advices;
	}

	@Override
	public Object getCorrectPolicyDataObject() {		
		return policyAdapter.getData();
	}
}
