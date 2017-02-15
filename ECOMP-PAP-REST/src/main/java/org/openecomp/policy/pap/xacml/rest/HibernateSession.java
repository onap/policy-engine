/*-
 * ============LICENSE_START=======================================================
 * ECOMP-PAP-REST
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

package org.openecomp.policy.pap.xacml.rest;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;


public class HibernateSession{
	private static SessionFactory xacmlsessionFactory;


	static {
		try {
			Configuration configuration= new Configuration();
			configuration.setProperty("hibernate.connection.url", XACMLPapServlet.papDbUrl);
			configuration.setProperty("hibernate.connection.username", XACMLPapServlet.papDbUser);
			configuration.setProperty("hibernate.connection.password", XACMLPapServlet.papDbPassword);
			configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
			configuration.setProperty("hibernate.connection.driver_class", XACMLPapServlet.papDbDriver);	
			configuration.setProperty("hibernate.show_sql", "false");	
			configuration.setProperty("hibernate.connection.autocommit", "true");
			configuration.setProperty("hibernate.c3p0.min_size", "5");
			configuration.setProperty("hibernate.c3p0.max_size", "200");
			configuration.setProperty("hibernate.c3p0.timeout", "3600");
			configuration.setProperty("hibernate.c3p0.idle_test_period", "3600");
			
			StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
			xacmlsessionFactory = configuration.configure("/hibernate.cfg.xml").buildSessionFactory(builder.build());
			
		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}
	public static Session getSessionFactory() throws HibernateException {
		return xacmlsessionFactory.openSession();
	}

}
