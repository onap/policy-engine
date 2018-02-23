/*-
 * ============LICENSE_START=======================================================
 * ONAP-PAP-REST
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
package org.onap.policy.pdp.rest.api.services;

import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.policy.api.PolicyException;
import org.onap.policy.api.PolicyParameters;

public class BRMSRawPolicyServiceTest {
	@Test
	public void testRaw() throws PolicyException  {
		String systemKey = "xacml.properties";
		String testVal = "testVal";
		PolicyParameters testParams = new PolicyParameters();
		
		// Set the system property temporarily
		String oldProperty = System.getProperty(systemKey);
		System.setProperty(systemKey, "xacml.pdp.properties");
		
		BRMSRawPolicyService service = new BRMSRawPolicyService(testVal, testVal, testParams, testVal);
		assertEquals(service.getValidation(), false);
		assertEquals(service.getMessage(), "PE300 - Data Issue:  No Rule Body given");
		//assertEquals(service.getResult(false), "PE300 - Data Issue: Unable to get valid response from PAP(s) [http://localhost:8070/pap/]");
		//assertEquals(service.getResult(true), "PE300 - Data Issue: Unable to get valid response from PAP(s) [http://localhost:8070/pap/]");
		
		// Restore the original system property
		if (oldProperty != null) {
			System.setProperty(systemKey, oldProperty);
		}
		else {
			System.clearProperty(systemKey);
		} 
	} 
}
