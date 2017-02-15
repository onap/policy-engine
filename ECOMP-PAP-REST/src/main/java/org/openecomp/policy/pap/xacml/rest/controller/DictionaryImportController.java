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

package org.openecomp.policy.pap.xacml.rest.controller;
 /*
  * 
  * 
  * */
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.openecomp.policy.rest.dao.ActionListDao;
import org.openecomp.policy.rest.dao.ActionPolicyDictDao;
import org.openecomp.policy.rest.dao.AddressGroupDao;
import org.openecomp.policy.rest.dao.AttributeDao;
import org.openecomp.policy.rest.dao.BRMSParamTemplateDao;
import org.openecomp.policy.rest.dao.DecisionPolicyDao;
import org.openecomp.policy.rest.dao.DescriptiveScopeDao;
import org.openecomp.policy.rest.dao.EcompNameDao;
import org.openecomp.policy.rest.dao.PEPOptionsDao;
import org.openecomp.policy.rest.dao.PortListDao;
import org.openecomp.policy.rest.dao.PrefixListDao;
import org.openecomp.policy.rest.dao.ProtocolListDao;
import org.openecomp.policy.rest.dao.SecurityZoneDao;
import org.openecomp.policy.rest.dao.ServiceDictionaryDao;
import org.openecomp.policy.rest.dao.ServiceGroupDao;
import org.openecomp.policy.rest.dao.ServiceListDao;
import org.openecomp.policy.rest.dao.SiteDictionaryDao;
import org.openecomp.policy.rest.dao.TermListDao;
import org.openecomp.policy.rest.dao.VNFTypeDao;
import org.openecomp.policy.rest.dao.VSCLActionDao;
import org.openecomp.policy.rest.dao.VarbindDictionaryDao;
import org.openecomp.policy.rest.dao.ZoneDao;
import org.openecomp.policy.rest.jpa.ActionList;
import org.openecomp.policy.rest.jpa.ActionPolicyDict;
import org.openecomp.policy.rest.jpa.AddressGroup;
import org.openecomp.policy.rest.jpa.Attribute;
import org.openecomp.policy.rest.jpa.BRMSParamTemplate;
import org.openecomp.policy.rest.jpa.Category;
import org.openecomp.policy.rest.jpa.Datatype;
import org.openecomp.policy.rest.jpa.DecisionSettings;
import org.openecomp.policy.rest.jpa.DescriptiveScope;
import org.openecomp.policy.rest.jpa.EcompName;
import org.openecomp.policy.rest.jpa.GroupServiceList;
import org.openecomp.policy.rest.jpa.PEPOptions;
import org.openecomp.policy.rest.jpa.PREFIXLIST;
import org.openecomp.policy.rest.jpa.ProtocolList;
import org.openecomp.policy.rest.jpa.SecurityZone;
import org.openecomp.policy.rest.jpa.ServiceList;
import org.openecomp.policy.rest.jpa.TermList;
import org.openecomp.policy.rest.jpa.UserInfo;
import org.openecomp.policy.rest.jpa.VNFType;
import org.openecomp.policy.rest.jpa.VSCLAction;
import org.openecomp.policy.rest.jpa.VarbindDictionary;
import org.openecomp.policy.rest.jpa.Zone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import au.com.bytecode.opencsv.CSVReader;


@Controller
public class DictionaryImportController {
	private String newFile;

	@Autowired
	AttributeDao attributeDao;

	@Autowired
	ActionPolicyDictDao  actionPolicyDictDao;

	@Autowired
	EcompNameDao ecompNameDao;

	@Autowired
	VNFTypeDao vnfTypeDao;

	@Autowired
	VSCLActionDao vsclActionDao;

	@Autowired
	PEPOptionsDao pepOptionsDao;

	@Autowired
	VarbindDictionaryDao varbindDao;

	@Autowired
	ServiceDictionaryDao closedLoopServiceDao;

	@Autowired
	SiteDictionaryDao closedLoopSiteDao;

	@Autowired
	DescriptiveScopeDao descriptiveScopeDao;

	@Autowired
	PrefixListDao prefixListDao;

	@Autowired
	PortListDao portListDao;

	@Autowired
	ProtocolListDao protocolListDao;

	@Autowired
	AddressGroupDao addressGroupDao;

	@Autowired
	ActionListDao actionListDao;

	@Autowired
	SecurityZoneDao securityZoneDao;

	@Autowired
	ServiceGroupDao serviceGroupDao;

	@Autowired
	ServiceListDao serviceListDao;

	@Autowired
	TermListDao termListDao;

	@Autowired
	ZoneDao zoneDao;

	@Autowired
	DecisionPolicyDao decisionSettingsDao;

	@Autowired
	BRMSParamTemplateDao brmsParamTemplateDao;

	@RequestMapping(value={"/dictionary/import_dictionary.htm/*"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
	public void ImportDictionaryData(HttpServletRequest request, HttpServletResponse response) throws Exception{
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		//JsonNode root = mapper.readTree(request.getReader());
		String userId = request.getPathInfo().substring(request.getPathInfo().lastIndexOf("/")+1);
		List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
		for (FileItem item : items) {
			File file = new File(item.getName());
			OutputStream outputStream = new FileOutputStream(file);
			IOUtils.copy(item.getInputStream(), outputStream);
			outputStream.close();
			this.newFile = file.toString();
			CSVReader csvReader = new CSVReader(new FileReader(this.newFile));
			List<String[]> dictSheet = csvReader.readAll();
			if(item.getName().startsWith("Attribute")){
				for(int i = 1; i< dictSheet.size(); i++){
					Attribute attribute = new Attribute("",  userId);
					UserInfo userinfo = new UserInfo();
					userinfo.setUserLoginId(userId);
					attribute.setUserCreatedBy(userinfo);
					attribute.setUserModifiedBy(userinfo);
					String[] rows = dictSheet.get(i);
					for (int j=0 ; j<rows.length; j++ ){
						if(dictSheet.get(0)[j].equalsIgnoreCase("xacml_id") || dictSheet.get(0)[j].equalsIgnoreCase("Attribute ID")){
							attribute.setXacmlId(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("description")){
							attribute.setDescription(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("priority")){
							attribute.setPriority(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("datatype") || dictSheet.get(0)[j].equalsIgnoreCase("Data Type")){
							Datatype dataType = new Datatype();
							if(rows[j].equalsIgnoreCase("string")){
								dataType.setId(26);
							}else if(rows[j].equalsIgnoreCase("integer")){
								dataType.setId(12);
							}else if(rows[j].equalsIgnoreCase("double")){
								dataType.setId(25);
							}else if(rows[j].equalsIgnoreCase("boolean")){
								dataType.setId(18);
							}else if(rows[j].equalsIgnoreCase("user")){
								dataType.setId(29);
							}
							attribute.setDatatypeBean(dataType);
							Category category = new Category();
							category.setId(5);
							attribute.setCategoryBean(category);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("attribute_value") || dictSheet.get(0)[j].equalsIgnoreCase("Attribute Value")){
							attribute.setAttributeValue(rows[j]);
						}
					}
					attributeDao.Save(attribute);
				}
			}
			if(item.getName().startsWith("ActionPolicyDictionary")){
				for(int i = 1; i< dictSheet.size(); i++){
					ActionPolicyDict attribute = new ActionPolicyDict("",  userId);
					UserInfo userinfo = new UserInfo();
					userinfo.setUserLoginId(userId);
					attribute.setUserCreatedBy(userinfo);
					attribute.setUserModifiedBy(userinfo);
					String[] rows = dictSheet.get(i);
					for (int j=0 ; j<rows.length; j++ ){
						if(dictSheet.get(0)[j].equalsIgnoreCase("attribute_name") || dictSheet.get(0)[j].equalsIgnoreCase("Attribute Name")){
							attribute.setAttributeName(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("body")){
							attribute.setBody(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("description")){
							attribute.setDescription(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("headers")){
							attribute.setHeader(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("method")){
							attribute.setMethod(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("type")){
							attribute.setMethod(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("url")){
							attribute.setMethod(rows[j]);
						}
					}
					actionPolicyDictDao.Save(attribute);
				}
			}
			if(item.getName().startsWith("EcompName")){
				for(int i = 1; i< dictSheet.size(); i++){
					EcompName attribute = new EcompName("",  userId);
					UserInfo userinfo = new UserInfo();
					userinfo.setUserLoginId(userId);
					attribute.setUserCreatedBy(userinfo);
					attribute.setUserModifiedBy(userinfo);
					String[] rows = dictSheet.get(i);
					for (int j=0 ; j<rows.length; j++ ){
						if(dictSheet.get(0)[j].equalsIgnoreCase("ecomp_name") || dictSheet.get(0)[j].equalsIgnoreCase("Ecomp Name")){
							attribute.setEcompName(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("description")){
							attribute.setDescription(rows[j]);
						}
					}
					ecompNameDao.Save(attribute);
				}
			}
			if(item.getName().startsWith("VNFType")){
				for(int i = 1; i< dictSheet.size(); i++){
					VNFType attribute = new VNFType("",  userId);
					UserInfo userinfo = new UserInfo();
					userinfo.setUserLoginId(userId);
					attribute.setUserCreatedBy(userinfo);
					attribute.setUserModifiedBy(userinfo);
					String[] rows = dictSheet.get(i);
					for (int j=0 ; j<rows.length; j++ ){
						if(dictSheet.get(0)[j].equalsIgnoreCase("vnf_type") || dictSheet.get(0)[j].equalsIgnoreCase("VNF Type")){
							attribute.setVnftype(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("description")){
							attribute.setDescription(rows[j]);
						}
					}
					vnfTypeDao.Save(attribute);
				}
			}
			if(item.getName().startsWith("VSCLAction")){
				for(int i = 1; i< dictSheet.size(); i++){
					VSCLAction attribute = new VSCLAction("",  userId);
					UserInfo userinfo = new UserInfo();
					userinfo.setUserLoginId(userId);
					attribute.setUserCreatedBy(userinfo);
					attribute.setUserModifiedBy(userinfo);
					String[] rows = dictSheet.get(i);
					for (int j=0 ; j<rows.length; j++ ){
						if(dictSheet.get(0)[j].equalsIgnoreCase("vscl_action") || dictSheet.get(0)[j].equalsIgnoreCase("VSCL Action")){
							attribute.setVsclaction(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("description")){
							attribute.setDescription(rows[j]);
						}
					}
					vsclActionDao.Save(attribute);
				}
			}
			if(item.getName().startsWith("PEPOptions")){
				for(int i = 1; i< dictSheet.size(); i++){
					PEPOptions attribute = new PEPOptions("",  userId);
					UserInfo userinfo = new UserInfo();
					userinfo.setUserLoginId(userId);
					attribute.setUserCreatedBy(userinfo);
					attribute.setUserModifiedBy(userinfo);
					String[] rows = dictSheet.get(i);
					for (int j=0 ; j<rows.length; j++ ){
						if(dictSheet.get(0)[j].equalsIgnoreCase("PEP_NAME") || dictSheet.get(0)[j].equalsIgnoreCase("PEP Name")){
							attribute.setPepName(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("description")){
							attribute.setDescription(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("Actions")){
							attribute.setActions(rows[j]);
						}
					}
					pepOptionsDao.Save(attribute);
				}
			}
			if(item.getName().startsWith("VarbindDictionary")){
				for(int i = 1; i< dictSheet.size(); i++){
					VarbindDictionary attribute = new VarbindDictionary("",  userId);
					UserInfo userinfo = new UserInfo();
					userinfo.setUserLoginId(userId);
					attribute.setUserCreatedBy(userinfo);
					attribute.setUserModifiedBy(userinfo);
					String[] rows = dictSheet.get(i);
					for (int j=0 ; j<rows.length; j++ ){
						if(dictSheet.get(0)[j].equalsIgnoreCase("varbind_Name") || dictSheet.get(0)[j].equalsIgnoreCase("Varbind Name")){
							attribute.setVarbindName(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("varbind_Description") || dictSheet.get(0)[j].equalsIgnoreCase("Varbind Description")){
							attribute.setVarbindDescription(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("varbind_oid") || dictSheet.get(0)[j].equalsIgnoreCase("Varbind OID")){
							attribute.setVarbindOID(rows[j]);
						}
					}
					varbindDao.Save(attribute);
				}
			}
			if(item.getName().startsWith("BRMSParamDictionary")){
				for(int i = 1; i< dictSheet.size(); i++){
					BRMSParamTemplate attribute = new BRMSParamTemplate();
					UserInfo userinfo = new UserInfo();
					userinfo.setUserLoginId(userId);
					attribute.setUserCreatedBy(userinfo);
					String[] rows = dictSheet.get(i);
					for (int j=0 ; j<rows.length; j++ ){
						if(dictSheet.get(0)[j].equalsIgnoreCase("param_template_name") || dictSheet.get(0)[j].equalsIgnoreCase("Rule Name")){
							attribute.setRuleName(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("DESCRIPTION") || dictSheet.get(0)[j].equalsIgnoreCase("Description")){
							attribute.setDescription(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("rule")){
							attribute.setRule(rows[j]);
						}
					}
					brmsParamTemplateDao.Save(attribute);
				}
			}
			if(item.getName().startsWith("Settings")){
				for(int i = 1; i< dictSheet.size(); i++){
					DecisionSettings attribute = new DecisionSettings("",  userId);
					UserInfo userinfo = new UserInfo();
					userinfo.setUserLoginId(userId);
					attribute.setUserCreatedBy(userinfo);
					attribute.setUserModifiedBy(userinfo);
					String[] rows = dictSheet.get(i);
					for (int j=0 ; j<rows.length; j++ ){
						if(dictSheet.get(0)[j].equalsIgnoreCase("xacml_id") || dictSheet.get(0)[j].equalsIgnoreCase("Settings ID")){
							attribute.setXacmlId(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("description")){
							attribute.setDescription(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("priority")){
							attribute.setPriority(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("datatype") || dictSheet.get(0)[j].equalsIgnoreCase("Data Type")){
							Datatype dataType = new Datatype();
							if(rows[j].equalsIgnoreCase("string")){
								dataType.setId(26);
							}else if(rows[j].equalsIgnoreCase("integer")){
								dataType.setId(12);
							}else if(rows[j].equalsIgnoreCase("double")){
								dataType.setId(25);
							}else if(rows[j].equalsIgnoreCase("boolean")){
								dataType.setId(18);
							}else if(rows[j].equalsIgnoreCase("user")){
								dataType.setId(29);
							}
							attribute.setDatatypeBean(dataType);
						}
					}
					decisionSettingsDao.Save(attribute);
				}
			}
			if(item.getName().startsWith("PrefixList")){
				for(int i = 1; i< dictSheet.size(); i++){
					PREFIXLIST attribute = new PREFIXLIST("",  userId);
					String[] rows = dictSheet.get(i);
					for (int j=0 ; j<rows.length; j++ ){
						if(dictSheet.get(0)[j].equalsIgnoreCase("prefixListName") || dictSheet.get(0)[j].equalsIgnoreCase("PrefixList Name")){
							attribute.setPrefixListName(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("description")){
							attribute.setPrefixListValue(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("prefixListValue") || dictSheet.get(0)[j].equalsIgnoreCase("PrefixList Value")){
							attribute.setDescription(rows[j]);
						}
					}
					prefixListDao.Save(attribute);
				}
			}
			if(item.getName().startsWith("SecurityZone")){
				for(int i = 1; i< dictSheet.size(); i++){
					SecurityZone attribute = new SecurityZone("",  userId);
					String[] rows = dictSheet.get(i);
					for (int j=0 ; j<rows.length; j++ ){
						if(dictSheet.get(0)[j].equalsIgnoreCase("zoneName") || dictSheet.get(0)[j].equalsIgnoreCase("Zone Name")){
							attribute.setZoneName(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("zoneValue")  || dictSheet.get(0)[j].equalsIgnoreCase("Zone Value")){
							attribute.setZoneValue(rows[j]);
						}
					}
					securityZoneDao.Save(attribute);
				}
			}
			if(item.getName().startsWith("Zone")){
				for(int i = 1; i< dictSheet.size(); i++){
					Zone attribute = new Zone("",  userId);
					String[] rows = dictSheet.get(i);
					for (int j=0 ; j<rows.length; j++ ){
						if(dictSheet.get(0)[j].equalsIgnoreCase("zoneName") || dictSheet.get(0)[j].equalsIgnoreCase("Zone Name")){
							attribute.setZoneName(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("zoneValue")  || dictSheet.get(0)[j].equalsIgnoreCase("Zone Value")){
							attribute.setZoneValue(rows[j]);
						}
					}
					zoneDao.Save(attribute);
				}
			}
			if(item.getName().startsWith("ServiceList")){
				for(int i = 1; i< dictSheet.size(); i++){
					ServiceList attribute = new ServiceList("",  userId);
					String[] rows = dictSheet.get(i);
					for (int j=0 ; j<rows.length; j++ ){
						if(dictSheet.get(0)[j].equalsIgnoreCase("serviceName") || dictSheet.get(0)[j].equalsIgnoreCase("Service Name")){
							attribute.setServiceName(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("serviceDesc")  || dictSheet.get(0)[j].equalsIgnoreCase("Description")){
							attribute.setServiceDescription(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("serviceType")  || dictSheet.get(0)[j].equalsIgnoreCase("Service Type")){
							attribute.setServiceType(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("serviceTrasProtocol")  || dictSheet.get(0)[j].equalsIgnoreCase("Transport Protocol")){
							attribute.setServiceTransProtocol(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("serviceAppProtocol")  || dictSheet.get(0)[j].equalsIgnoreCase("APP Protocol")){
							attribute.setServiceAppProtocol(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("servicePorts")  || dictSheet.get(0)[j].equalsIgnoreCase("Ports")){
							attribute.setServicePorts(rows[j]);
						}
					}
					serviceListDao.Save(attribute);
				}
			}
			if(item.getName().startsWith("ServiceGroup")){
				for(int i = 1; i< dictSheet.size(); i++){
					GroupServiceList attribute = new GroupServiceList("",  userId);
					String[] rows = dictSheet.get(i);
					for (int j=0 ; j<rows.length; j++ ){
						if(dictSheet.get(0)[j].equalsIgnoreCase("name") || dictSheet.get(0)[j].equalsIgnoreCase("Group Name")){
							attribute.setGroupName(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("serviceList")  || dictSheet.get(0)[j].equalsIgnoreCase("Service List")){
							attribute.setServiceList(rows[j]);
						}
					}
					serviceGroupDao.Save(attribute);
				}
			}
			if(item.getName().startsWith("AddressGroup")){
				for(int i = 1; i< dictSheet.size(); i++){
					AddressGroup attribute = new AddressGroup("",  userId);
					String[] rows = dictSheet.get(i);
					for (int j=0 ; j<rows.length; j++ ){
						if(dictSheet.get(0)[j].equalsIgnoreCase("name") || dictSheet.get(0)[j].equalsIgnoreCase("Group Name")){
							attribute.setGroupName(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("serviceList")  || dictSheet.get(0)[j].equalsIgnoreCase("Prefix List")){
							attribute.setServiceList(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("description")){
							attribute.setDescription(rows[j]);
						}
					}
					addressGroupDao.Save(attribute);
				}
			}
			if(item.getName().startsWith("ProtocolList")){
				for(int i = 1; i< dictSheet.size(); i++){
					ProtocolList attribute = new ProtocolList("",  userId);
					String[] rows = dictSheet.get(i);
					for (int j=0 ; j<rows.length; j++ ){
						if(dictSheet.get(0)[j].equalsIgnoreCase("protocolName") || dictSheet.get(0)[j].equalsIgnoreCase("Protocol Name")){
							attribute.setProtocolName(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("description")){
							attribute.setDescription(rows[j]);
						}
					}
					protocolListDao.Save(attribute);
				}
			}
			if(item.getName().startsWith("ActionList")){
				for(int i = 1; i< dictSheet.size(); i++){
					ActionList attribute = new ActionList("",  userId);
					String[] rows = dictSheet.get(i);
					for (int j=0 ; j<rows.length; j++ ){
						if(dictSheet.get(0)[j].equalsIgnoreCase("actionName") || dictSheet.get(0)[j].equalsIgnoreCase("Action Name")){
							attribute.setActionName(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("description")){
							attribute.setDescription(rows[j]);
						}
					}
					actionListDao.Save(attribute);
				}
			}
			if(item.getName().startsWith("TermList")){
				for(int i = 1; i< dictSheet.size(); i++){
					TermList attribute = new TermList("",  userId);
					UserInfo userinfo = new UserInfo();
					userinfo.setUserLoginId(userId);
					attribute.setUserCreatedBy(userinfo);
					attribute.setUserModifiedBy(userinfo);
					String[] rows = dictSheet.get(i);
					for (int j=0 ; j<rows.length; j++ ){
						if(dictSheet.get(0)[j].equalsIgnoreCase("termName") || dictSheet.get(0)[j].equalsIgnoreCase("Term-Name")){
							attribute.setTermName(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("Term-Description") || dictSheet.get(0)[j].equalsIgnoreCase("termDescription")){
							attribute.setDescription(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("fromZone")  || dictSheet.get(0)[j].equalsIgnoreCase("From Zone")){
							attribute.setFromZones(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("toZone") || dictSheet.get(0)[j].equalsIgnoreCase("To Zone")){
							attribute.setToZones(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("srcIPList") || dictSheet.get(0)[j].equalsIgnoreCase("Source-IP-List")){
							attribute.setSrcIPList(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("destIPList") || dictSheet.get(0)[j].equalsIgnoreCase("Destination-IP-List")){
							attribute.setDestIPList(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("srcPortList") || dictSheet.get(0)[j].equalsIgnoreCase("Source-Port-List")){
							attribute.setSrcPortList(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("destPortList") || dictSheet.get(0)[j].equalsIgnoreCase("Destination-Port-List")){
							attribute.setDestPortList(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("action") || dictSheet.get(0)[j].equalsIgnoreCase("Action List")){
							attribute.setAction(rows[j]);
						}
					}
					termListDao.Save(attribute);
				}
			}
			if(item.getName().startsWith("SearchCriteria")){
				for(int i = 1; i< dictSheet.size(); i++){
					DescriptiveScope attribute = new DescriptiveScope("",  userId);
					UserInfo userinfo = new UserInfo();
					userinfo.setUserLoginId(userId);
					attribute.setUserCreatedBy(userinfo);
					attribute.setUserModifiedBy(userinfo);
					String[] rows = dictSheet.get(i);
					for (int j=0 ; j<rows.length; j++ ){
						if(dictSheet.get(0)[j].equalsIgnoreCase("descriptiveScopeName") || dictSheet.get(0)[j].equalsIgnoreCase("Descriptive ScopeName")){
							attribute.setScopeName(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("description")){
							attribute.setDescription(rows[j]);
						}
						if(dictSheet.get(0)[j].equalsIgnoreCase("search") || dictSheet.get(0)[j].equalsIgnoreCase("Search Criteria")){
							attribute.setSearch(rows[j]);
						}
					}
					descriptiveScopeDao.Save(attribute);
				}
			}
			csvReader.close();
		}
	}
}
