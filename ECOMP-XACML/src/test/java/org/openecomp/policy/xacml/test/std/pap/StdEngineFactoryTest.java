/*-
 * ============LICENSE_START=======================================================
 * ECOMP-XACML
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
package org.openecomp.policy.xacml.test.std.pap;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Properties;

import org.junit.Test;
import org.openecomp.policy.xacml.std.pap.StdEngineFactory;

import com.att.research.xacml.api.pap.PAPException;
import com.att.research.xacml.util.FactoryException;

public class StdEngineFactoryTest {


	@Test
	public void testStdEngineFactory() throws FactoryException, PAPException, IOException{
		
		StdEngineFactory stdFactory = new StdEngineFactory();
		System.setProperty("xacml.pap.pdps", "src/test/resources/pdps");
		assertTrue(stdFactory.newEngine() != null);
		Properties properties = new Properties();
		properties.setProperty("xacml.pap.pdps", "src/test/resources/pdps");
		assertTrue(stdFactory.newEngine(properties) != null);
		
		StdEngineFactory stdFactoryNew = new StdEngineFactory();
		System.setProperty("xacml.pap.pdps", "src/test/resources/pdpstest");
		assertTrue(stdFactoryNew.newEngine() != null);

	}
}
