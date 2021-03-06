/*-
 * ============LICENSE_START=======================================================
 * ONAP-PAP-REST
 * ================================================================================
 * Copyright (C) 2018-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.pap.xacml.rest.components;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.att.research.xacml.api.pap.PAPException;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceExpressionsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicyType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.policy.rest.adapter.PolicyRestAdapter;

public class OptimizationConfigPolicyTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testConstructor1() {
        thrown.expect(NullPointerException.class);
        OptimizationConfigPolicy policy = new OptimizationConfigPolicy();
        policy.getCorrectPolicyDataObject();
        fail("Expected an exception");
    }

    @Test
    public void testConstructor2() {
        PolicyRestAdapter policyAdapter = new PolicyRestAdapter();
        OptimizationConfigPolicy policy = new OptimizationConfigPolicy(policyAdapter);
        assertNull(policy.getCorrectPolicyDataObject());
    }

    @Test
    public void testSave() throws PAPException {
        PolicyType policyType = new PolicyType();
        PolicyRestAdapter policyAdapter = new PolicyRestAdapter();
        policyAdapter.setPolicyID("id");
        policyAdapter.setHighestVersion(1);
        policyAdapter.setPolicyType("Config");
        policyAdapter.setNewFileName("newfile");
        policyAdapter.setData(policyType);
        policyAdapter.setJsonBody("{\"key\":\"value\"}");
        policyAdapter.setServiceType("svcType");
        OptimizationConfigPolicy policy = new OptimizationConfigPolicy(policyAdapter);
        assertThatThrownBy(() -> policy.savePolicies()).isInstanceOf(Exception.class);
    }

    @Test
    public void testAdvice() {
        PolicyType policyType = new PolicyType();
        PolicyRestAdapter policyAdapter = new PolicyRestAdapter();
        policyAdapter.setPolicyID("id");
        policyAdapter.setHighestVersion(1);
        policyAdapter.setPolicyType("Config");
        policyAdapter.setNewFileName("newfile");
        policyAdapter.setData(policyType);
        policyAdapter.setJsonBody("{\"key\":\"value\"}");
        policyAdapter.setServiceType("svcType");

        OptimizationConfigPolicy policy = new OptimizationConfigPolicy(policyAdapter);
        assertThatThrownBy(() -> policy.savePolicies()).isInstanceOf(Exception.class);

        AdviceExpressionsType expType = policy.getAdviceExpressions(1, "filename");
        assertEquals(1, expType.getAdviceExpression().size());
    }
}
