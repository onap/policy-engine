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
package org.openecomp.policy.controller;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.policy.common.logging.flexlogger.FlexLogger;
import org.openecomp.policy.common.logging.flexlogger.Logger;
import org.openecomp.policy.rest.dao.CommonClassDao;
import org.openecomp.policy.rest.jpa.GlobalRoleSettings;
import org.springframework.mock.web.MockHttpServletResponse;

public class AdminTabControllerTest {

	private static Logger logger = FlexLogger.getLogger(AdminTabControllerTest.class);
	private static CommonClassDao commonClassDao;
	private HttpServletRequest request;
	private MockHttpServletResponse response;
	
	@Before
	public void setUp() throws Exception {

		logger.info("setUp: Entering");
        commonClassDao = mock(CommonClassDao.class);
        
        request = mock(HttpServletRequest.class);       
		response =  new MockHttpServletResponse();
		
		AdminTabController.setCommonClassDao(commonClassDao);
		
        GlobalRoleSettings globalRole = new GlobalRoleSettings();
        globalRole.setLockdown(true);
        globalRole.setRole("super-admin");
        List<Object> globalRoles = new ArrayList<>();
        globalRoles.add(globalRole);
        when(commonClassDao.getData(GlobalRoleSettings.class)).thenReturn(globalRoles);
	} 
	
	@Test
	public void testGetAdminRole(){
		AdminTabController admin = new AdminTabController();
		try {
			admin.getAdminTabEntityData(request, response);
			assertTrue(response.getContentAsString() != null && response.getContentAsString().contains("lockdowndata"));
		} catch (UnsupportedEncodingException e) {
			logger.error("Exception Occured"+e);
			fail();
		}
	}
	
	@Test
	public void testSaveAdminRole() throws Exception{
		AdminTabController admin = new AdminTabController();
		String data = "{\"lockdowndata\":{\"lockdown\":true}}";
		BufferedReader reader = new BufferedReader(new StringReader(data));	
		try {
			when(request.getReader()).thenReturn(reader);
			admin.saveAdminTabLockdownValue(request, response);
			assertTrue(response.getContentAsString() != null && response.getContentAsString().contains("descriptiveScopeDictionaryDatas"));
		} catch (UnsupportedEncodingException e) {
			logger.error("Exception Occured"+e);
			fail();
		}
	}
	
}
