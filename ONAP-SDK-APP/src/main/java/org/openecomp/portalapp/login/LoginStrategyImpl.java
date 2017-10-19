/*-
 * ================================================================================
 * ONAP Portal SDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property
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
 * ================================================================================
 */
package org.openecomp.portalapp.login;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.policy.common.logging.flexlogger.FlexLogger;
import org.onap.policy.common.logging.flexlogger.Logger;
import org.openecomp.portalsdk.core.auth.LoginStrategy;
import org.openecomp.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.openecomp.portalsdk.core.onboarding.exception.PortalAPIException;
import org.openecomp.portalsdk.core.onboarding.util.CipherUtil;
import org.openecomp.portalsdk.core.util.SystemProperties;
import org.springframework.web.servlet.ModelAndView;

public class LoginStrategyImpl extends LoginStrategy {

    private static final Logger LOGGER = FlexLogger.getLogger(LoginStrategyImpl.class);

    @Override
    public ModelAndView doLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 'login' for opensource is same as 'external' login.
        return doExternalLogin(request, response);
    }

    @Override
    public String getUserId(HttpServletRequest request) throws PortalAPIException {
        // Check ONAP Portal cookie
        if (!isLoginCookieExist(request))
            return null;

        String userid = null;
        try {
            userid = getUserIdFromCookie(request);
        } catch (Exception e) {
            LOGGER.error("Exception Occured" + e);
        }
        return userid;
    }

    private static String getUserIdFromCookie(HttpServletRequest request) throws PortalAPIException {
    	String userId = ""; 
		Cookie userIdCookie = getCookie(request, USER_ID); 
		if (userIdCookie != null) { 
			final String cookieValue = userIdCookie.getValue(); 
			if (!SystemProperties.containsProperty(SystemProperties.Decryption_Key)) 
				throw new IllegalStateException("Failed to find property " + SystemProperties.Decryption_Key); 
			final String decryptionKey = SystemProperties.getProperty(SystemProperties.Decryption_Key); 
			try {
				userId = CipherUtil.decrypt(cookieValue, decryptionKey);
				LOGGER.debug("getUserIdFromCookie: decrypted as {}" +userId); 
			} catch (Exception e) {
				LOGGER.error("Exception Occured in getUserIdFromCookie" + e);
			} 
		} 
		return userId;
    }

    private static boolean isLoginCookieExist(HttpServletRequest request) {
        Cookie ep = getCookie(request, EP_SERVICE);
        return (ep != null);
    }

    private static Cookie getCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies)
                if (cookie.getName().equals(cookieName))
                    return cookie;

        return null;
    }

}
