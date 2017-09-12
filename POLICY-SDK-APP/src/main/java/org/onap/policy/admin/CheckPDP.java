/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Engine
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

package org.onap.policy.admin;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.onap.policy.common.logging.flexlogger.FlexLogger;
import org.onap.policy.common.logging.flexlogger.Logger;
import org.onap.policy.rest.XACMLRestProperties;
import org.onap.policy.xacml.api.XACMLErrorConstants;

import com.att.research.xacml.util.XACMLProperties;

public class CheckPDP {
	private static Path pdpPath = null;
	private static Long oldModified = null;
	private static HashMap<String, String> pdpMap = null;
	private static final Logger LOGGER = FlexLogger.getLogger(CheckPDP.class);
	
	private CheckPDP(){
		//default constructor
	}
	public static boolean validateID(String id) {
		// ReadFile
		try {
			readFile();
		} catch (Exception e) {
			LOGGER.error(XACMLErrorConstants.ERROR_SYSTEM_ERROR + e);
			return false;
		}
		// Check ID
		if(pdpMap.containsKey(id))
                  return true;
                else
                 return false;
	}

	private static void readFile(){
		String pdpFile = null;
		Long newModified = null;
		try{
			pdpFile = XACMLProperties.getProperty(XACMLRestProperties.PROP_PDP_IDFILE);	
		}catch (Exception e){
			LOGGER.error(XACMLErrorConstants.ERROR_DATA_ISSUE + "Cannot read the PDP ID File" + e);
			return;
		}
		if (pdpFile == null) {
			LOGGER.error(XACMLErrorConstants.ERROR_SYSTEM_ERROR + "PDP File name not Valid : " + pdpFile);
		}
		if (pdpPath == null) {
			pdpPath = Paths.get(pdpFile);
			if (!pdpPath.toFile().exists()) {
				LOGGER.error(XACMLErrorConstants.ERROR_SYSTEM_ERROR + "File doesn't exist in the specified Path : "	+ pdpPath.toString());

			} 
			if (pdpPath.toString().endsWith(".properties")) {
				readProps();
			} else {
				LOGGER.error(XACMLErrorConstants.ERROR_SYSTEM_ERROR + "Not a .properties file " + pdpFile);
			}
		}
		// Check if File is updated recently
		else {
			newModified = pdpPath.toFile().lastModified();
			if (!newModified.equals(oldModified)) {
				// File has been updated.
				readProps();
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void readProps() {
		Properties pdpProp = null;
		pdpProp = new Properties();
		try {
			InputStream in = new FileInputStream(pdpPath.toFile());
			oldModified = pdpPath.toFile().lastModified();
			pdpProp.load(in);
			// Read the Properties and Load the PDPs and encoding.
			pdpMap = new HashMap<>();
			// Check the Keys for PDP_URLs
			Collection<Object> unsorted = pdpProp.keySet();
			List<String> sorted = new ArrayList(unsorted);
			Collections.sort(sorted);
			for (String propKey : sorted) {
				loadPDPProperties(propKey, pdpProp);
			}
			if (pdpMap == null || pdpMap.isEmpty()) {
				LOGGER.debug(XACMLErrorConstants.ERROR_SYSTEM_ERROR + "Cannot Proceed without PDP_URLs");
			}
			in.close();
		} catch (IOException e) {
			LOGGER.error(XACMLErrorConstants.ERROR_SYSTEM_ERROR + e);
		}
	}
	
	private static void loadPDPProperties(String propKey, Properties pdpProp){
		if (propKey.startsWith("PDP_URL")) {
			String check_val = pdpProp.getProperty(propKey);
			if (check_val == null) {
				LOGGER.error("Properties file doesn't have the PDP_URL parameter");
			}
			if (check_val != null && check_val.contains(";")) {
				List<String> pdp_default = new ArrayList<>(Arrays.asList(check_val.split("\\s*;\\s*")));
				int pdpCount = 0;
				while (pdpCount < pdp_default.size()) {
					String pdpVal = pdp_default.get(pdpCount);
					readPDPParam(pdpVal);
					pdpCount++;
				}
			}
		}
	}

	private static void readPDPParam(String pdpVal){
		if(pdpVal.contains(",")){
			List<String> pdpValues = new ArrayList<>(Arrays.asList(pdpVal.split("\\s*,\\s*")));
			if(pdpValues.size()==3){
				// 1:2 will be UserID:Password
				String userID = pdpValues.get(1);
				String pass = pdpValues.get(2);
				Base64.Encoder encoder = Base64.getEncoder();
				// 0 - PDPURL
				pdpMap.put(pdpValues.get(0), encoder.encodeToString((userID+":"+pass).getBytes(StandardCharsets.UTF_8)));
			}else{
				LOGGER.error(XACMLErrorConstants.ERROR_PERMISSIONS + "No Credentials to send Request: " + pdpValues);
			}
		}else{
			LOGGER.error(XACMLErrorConstants.ERROR_PERMISSIONS + "No Credentials to send Request: " + pdpVal);
		}
	}
	
	public static String getEncoding(String pdpID){
		try {
			readFile();
		} catch (Exception e) {
			LOGGER.error(XACMLErrorConstants.ERROR_SYSTEM_ERROR + e);
		}
		String encoding = null;
		if(pdpMap!=null && (!pdpMap.isEmpty())){
			try{
				encoding = pdpMap.get(pdpID);
			} catch(Exception e){
				LOGGER.error(XACMLErrorConstants.ERROR_SYSTEM_ERROR + e);
			}
			return encoding;
		}else{
			return null;
		}
	}
}
