/*-
 * ============LICENSE_START=======================================================
 * ECOMP Policy Engine
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

package org.openecomp.policy.adapter;



import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class TermCollector {
	String serviceTypeId;
	String configName;
	DeployNowJson deploymentOption;
	String securityZoneId;
	
    protected Set<Object> serviceGroups;
    protected Set<Object> addressGroups;
    protected List<Term> firewallRuleList;
    
    private String primaryParentZoneId;
    
	
	//SecurityTypeId
    public String getServiceTypeId() {
        return serviceTypeId;
    }
    
    public void setServiceTypeId(String serviceTypeId) {
        this.serviceTypeId = serviceTypeId;
    }
    
    //ConfigName
    public String getConfigName() {
        return configName;
    }
    
    public void setConfigName(String configName) {
        this.configName = configName;
    }
    
    //DeploymentControl
    public DeployNowJson getDeploymentOption() {
        return deploymentOption;
    }
    
    public void setDeploymentOption(DeployNowJson deploymentOption) {
        this.deploymentOption = deploymentOption;
    }
    
    //SecurityZoneId
    public String getSecurityZoneId() {
        return securityZoneId;
    }
    public void setSecurityZoneId(String securityZoneId) {
        this.securityZoneId = securityZoneId;
    }
    

    //ServiceGroup
    public Set<Object> getServiceGroups() {
    	if(serviceGroups==null)
    	{
    		serviceGroups= new HashSet<Object>();
    	}
        return this.serviceGroups;
    }
    
    public void setServiceGroups(Set<Object> servListArray) {
		this.serviceGroups = servListArray;
	}

    //AddressGroup
    public Set<Object> getAddressGroups() {
    	if(addressGroups==null)
    	{
    		addressGroups= new HashSet<Object>();
    	}
        return this.addressGroups;
    }

    public void setAddressGroups(Set<Object> addressGroups) {
        this.addressGroups = addressGroups;
    }
    
    //FirewallRuleList
    public List<Term> getFirewallRuleList() {
    	
    	if(firewallRuleList==null)
    	{
    		firewallRuleList= new ArrayList<Term>();
    	}
        return this.firewallRuleList;
    }

    public void setFirewallRuleList(List<Term> firewallRuleList) {
        this.firewallRuleList = firewallRuleList;
    }
    
    
    //primaryParentZoneId
    public String getPrimaryParentZoneId() {
		return primaryParentZoneId;
	}

	public void setPrimaryParentZoneId(String primaryParentZoneId) {
		this.primaryParentZoneId = primaryParentZoneId;
	}

	

}
