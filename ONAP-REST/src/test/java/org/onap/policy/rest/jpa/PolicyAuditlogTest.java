/*-
 * ============LICENSE_START=======================================================
 * ONAP-REST
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.rest.jpa;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.junit.Test;

public class PolicyAuditlogTest {

    @Test
    public void test() throws ParseException {
        PolicyAuditlog auditLog;
        auditLog = new PolicyAuditlog();
        String value = "testData1";
        auditLog.setId(1);
        auditLog.setPolicyName(value);
        auditLog.setUserName(value);
        auditLog.setActions(value);

        // Test gets
        assertEquals(1, auditLog.getId());
        assertEquals(value, auditLog.getPolicyName());
        assertEquals(value, auditLog.getUserName());
        assertEquals(value, auditLog.getActions());
    }
}
