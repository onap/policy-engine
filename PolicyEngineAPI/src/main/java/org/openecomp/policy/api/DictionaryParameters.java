/*-
 * ============LICENSE_START=======================================================
 * PolicyEngineAPI
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

package org.openecomp.policy.api;

import java.util.Map;
import java.util.UUID;

public class DictionaryParameters {
	
	private DictionaryType dictionaryType;
	private String dictionary;
	private Map<AttributeType, Map<String,String>> dictionaryFields;
	private UUID requestID;
	
	
	/**
	 * @return the dictionaryType
	 */
	public DictionaryType getDictionaryType() {
		return dictionaryType;
	}
	/**
	 * @param dictionaryType the dictionaryType to set
	 */
	public void setDictionaryType(DictionaryType dictionaryType) {
		this.dictionaryType = dictionaryType;
	}
	/**
	 * @return the dictionary
	 */
	public String getDictionary() {
		return dictionary;
	}
	/**
	 * @param dictionary the dictionary to set
	 */
	public void setDictionary(String dictionary) {
		this.dictionary = dictionary;
	}
	/**
	 * @return the dictionaryFields
	 */
	public Map<AttributeType, Map<String,String>> getDictionaryFields() {
		return dictionaryFields;
	}
	/**
	 * @param dictionaryFields the dictionaryFields to set
	 */
	public void setDictionaryFields(Map<AttributeType, Map<String,String>> dictionaryFields) {
		this.dictionaryFields = dictionaryFields;
	}
	/**
	 * @return the requestID
	 */
	public UUID getRequestID() {
		return requestID;
	}
	/**
	 * @param requestID the requestID to set
	 */
	public void setRequestID(UUID requestID) {
		this.requestID = requestID;
	}

}
