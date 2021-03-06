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

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import org.onap.policy.common.logging.flexlogger.FlexLogger;
import org.onap.policy.common.logging.flexlogger.Logger;
import org.onap.policy.rest.dao.CommonClassDao;
import org.onap.policy.rest.jpa.PolicyEditorScopes;
import org.onap.policy.rest.jpa.PolicyRoles;
import org.onap.policy.rest.jpa.UserInfo;
import org.onap.policy.utils.PolicyUtils;
import org.onap.portalsdk.core.controller.RestrictedBaseController;
import org.onap.portalsdk.core.web.support.JsonMessage;
import org.onap.portalsdk.core.web.support.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/")
public class PolicyRolesController extends RestrictedBaseController {

    private static final Logger LOGGER = FlexLogger.getLogger(PolicyRolesController.class);

    @Autowired
    CommonClassDao commonClassDao;

    public void setCommonClassDao(CommonClassDao commonClassDao) {
        this.commonClassDao = commonClassDao;
    }

    List<String> scopelist;

    /**
     * Gets the policy roles entity data.
     *
     * @param request the request
     * @param response the response
     */
    @RequestMapping(
            value = {"/get_RolesData"},
            method = {org.springframework.web.bind.annotation.RequestMethod.GET},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void getPolicyRolesEntityData(HttpServletRequest request, HttpServletResponse response) {
        try {
            Map<String, Object> model = new HashMap<>();
            ObjectMapper mapper = new ObjectMapper();
            model.put("rolesDatas", mapper.writeValueAsString(commonClassDao.getUserRoles()));
            response.getWriter().write(new JSONObject(new JsonMessage(mapper.writeValueAsString(model))).toString());
        } catch (Exception e) {
            LOGGER.error("Exception Occured" + e);
        }
    }

    /**
     * Save roles and Mechid entity data.
     *
     * @param request the request
     * @param response the response
     * @return the model and view
     */
    @RequestMapping(
            value = {"/save_NonSuperRolesData"},
            method = {org.springframework.web.bind.annotation.RequestMethod.POST})
    public ModelAndView SaveRolesEntityData(HttpServletRequest request, HttpServletResponse response) {
        try {
            StringBuilder scopeName = new StringBuilder();
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            String userId = UserUtils.getUserSession(request).getOrgUserId();
            JsonNode root = mapper.readTree(request.getReader());
            ReadScopes adapter = mapper.readValue(root.get("editRoleData").toString(), ReadScopes.class);
            for (int i = 0; i < adapter.getScope().size(); i++) {
                if (i == 0) {
                    scopeName.append(adapter.getScope().get(0));
                } else {
                    scopeName.append("," + adapter.getScope().get(i));
                }
            }
            LOGGER.info(
                    "*************************Logging UserID for Roles Function***********************************");
            LOGGER.info("UserId:  " + userId + "Updating the Scope for following user" + adapter.getLoginId()
                    + "ScopeNames" + adapter.getScope());
            LOGGER.info(
                    "*********************************************************************************************");
            UserInfo userInfo = new UserInfo();
            userInfo.setUserLoginId(adapter.getLoginId().getUserName());
            userInfo.setUserName(adapter.getLoginId().getUserName());

            boolean checkNew = false;
            if (adapter.getId() == 0 && "mechid".equals(adapter.getRole())) {
                // Save new mechid scopes entity data.
                LOGGER.info(
                        "*********************Logging UserID for New Mechid Function********************************");
                LOGGER.info("UserId:" + userId + "Adding new mechid-scopes for following user" + adapter.getLoginId()
                        + "ScopeNames " + adapter.getScope());
                LOGGER.info(
                        "*******************************************************************************************");
                // First add the mechid to userinfo
                commonClassDao.save(userInfo);
                checkNew = true;
            }

            PolicyRoles roles = new PolicyRoles();
            roles.setId(adapter.getId());
            roles.setLoginId(adapter.getLoginId());
            roles.setRole(adapter.getRole());
            roles.setScope(scopeName.toString());
            if (checkNew) {
                roles.setLoginId(userInfo);
                commonClassDao.save(roles);
            } else {
                commonClassDao.update(roles);
            }
            response.setCharacterEncoding(PolicyUtils.CHARACTER_ENCODING);
            response.setContentType(PolicyUtils.APPLICATION_JSON);
            request.setCharacterEncoding(PolicyUtils.CHARACTER_ENCODING);
            response.getWriter().write(new JSONObject("{rolesDatas: "
                + mapper.writeValueAsString(commonClassDao.getUserRoles()) + "}").toString());
        } catch (Exception e) {
            LOGGER.error("Exception Occured" + e);
        }
        return null;
    }

    /**
     * Gets the policy scopes entity data.
     *
     * @param request the request
     * @param response the response
     */
    @RequestMapping(
            value = {"/get_PolicyRolesScopeData"},
            method = {org.springframework.web.bind.annotation.RequestMethod.GET},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void getPolicyScopesEntityData(HttpServletRequest request, HttpServletResponse response) {
        try {
            scopelist = new ArrayList<>();
            Map<String, Object> model = new HashMap<>();
            ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
            List<String> scopesData = commonClassDao.getDataByColumn(PolicyEditorScopes.class, "scopeName");
            model.put("scopeDatas", mapper.writeValueAsString(scopesData));
            response.getWriter().write(new JSONObject(new JsonMessage(mapper.writeValueAsString(model))).toString());
        } catch (Exception e) {
            LOGGER.error("Exception Occured" + e);
        }
    }
}

@Setter
@Getter
class ReadScopes {
    private int id;
    private UserInfo loginId;
    private String role;
    private List<String> scope;
}
