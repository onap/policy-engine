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

package org.onap.brmsgw.test;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.onap.policy.brmsInterface.ControllerPOJO;
import org.onap.policy.brmsInterface.NotificationPOJO;

public class NotificationPOJOTest {
	@Test
	public void testPojo() {
		String testVal = "testVal";
		ControllerPOJO ctrlPojo = new ControllerPOJO();
		List<ControllerPOJO> controllers = new ArrayList<ControllerPOJO>();
		controllers.add(ctrlPojo);
		NotificationPOJO pojo = new NotificationPOJO();
		
		pojo.setRequestID(testVal);
		assertEquals(pojo.getRequestID(), testVal);
		pojo.setEntity(testVal);
		assertEquals(pojo.getEntity(), testVal);
		pojo.setControllers(controllers);
		assertEquals(pojo.getControllers(), controllers);
	}
}
