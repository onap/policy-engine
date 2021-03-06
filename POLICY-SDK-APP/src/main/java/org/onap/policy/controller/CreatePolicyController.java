/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Engine
 * ================================================================================
 * Copyright (C) 2017, 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AllOfType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOfType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.MatchType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RuleType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.TargetType;

import org.onap.policy.common.logging.flexlogger.FlexLogger;
import org.onap.policy.common.logging.flexlogger.Logger;
import org.onap.policy.rest.adapter.PolicyRestAdapter;
import org.onap.policy.rest.jpa.PolicyEntity;
import org.onap.portalsdk.core.controller.RestrictedBaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class CreatePolicyController extends RestrictedBaseController {
    private static Logger policyLogger = FlexLogger.getLogger(CreatePolicyController.class);
    protected PolicyRestAdapter policyAdapter = null;
    private ArrayList<Object> attributeList;
    boolean isValidForm = false;

    /**
     * prePopulateBaseConfigPolicyData.
     *
     * @param policyAdapter PolicyRestAdapter
     * @param entity PolicyEntity
     */
    public void prePopulateBaseConfigPolicyData(PolicyRestAdapter policyAdapter, PolicyEntity entity) {
        attributeList = new ArrayList<>();
        if (! (policyAdapter.getPolicyData() instanceof PolicyType)) {
            return;
        }
        Object policyData = policyAdapter.getPolicyData();
        PolicyType policy = (PolicyType) policyData;
        policyAdapter.setOldPolicyFileName(policyAdapter.getPolicyName());
        policyAdapter.setConfigType(entity.getConfigurationData().getConfigType());
        policyAdapter.setConfigBodyData(entity.getConfigurationData().getConfigBody());
        String policyNameValue =
                policyAdapter.getPolicyName().substring(policyAdapter.getPolicyName().indexOf('_') + 1);
        policyAdapter.setPolicyName(policyNameValue);
        String description = "";
        try {
            description = policy.getDescription().substring(0, policy.getDescription().indexOf("@CreatedBy:"));
        } catch (Exception e) {
            policyLogger.error("Error while collecting the desciption tag in ActionPolicy " + policyNameValue, e);
            description = policy.getDescription();
        }
        policyAdapter.setPolicyDescription(description);
        // Get the target data under policy.
        TargetType target = policy.getTarget();
        //
        // NOTE: target.getAnyOf() will NEVER return null
        //
        if (target != null) {
            // Under target we have AnyOFType
            for (AnyOfType anyOf : target.getAnyOf()) {
                // Under AnyOFType we have AllOFType
                //
                // NOTE: anyOf.getAllOf() will NEVER return null
                //
                int index = 0;
                for (AllOfType allOf : anyOf.getAllOf()) {
                    // Under AllOFType we have Match
                    // NOTE: allOf.getMatch() will NEVER be NULL
                    //
                    for (MatchType match : allOf.getMatch()) {
                        //
                        // Under the match we have attribute value and
                        // attributeDesignator. So,finally down to the actual attribute.
                        //
                        String value = (String) match.getAttributeValue().getContent().get(0);
                        String attributeId = match.getAttributeDesignator().getAttributeId();
                        // First match in the target is OnapName, so set that value.
                        policyAdapter.setupUsingAttribute(attributeId, value);
                        // After Onap and Config it is optional to have attributes, so
                        // check weather dynamic values or there or not.
                        if (index >= 7) {
                            Map<String, String> attribute = new HashMap<>();
                            attribute.put("key", attributeId);
                            attribute.put("value", value);
                            attributeList.add(attribute);
                        }
                        index++;
                    }
                }
            }
            policyAdapter.setAttributes(attributeList);
        }
        List<Object> ruleList = policy.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition();
        for (Object object : ruleList) {
            if (object instanceof RuleType) {
                // get the condition data under the rule for rule Algorithms.
                policyAdapter.setRuleID(((RuleType) object).getRuleId());
            }
        }
    }
}
