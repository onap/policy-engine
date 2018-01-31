/*-
 * ============LICENSE_START=======================================================
 * ONAP-PAP-REST
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

package org.onap.policy.pap.xacml.rest.adapters;

import java.util.ArrayList;

public class GridData {
	private ArrayList<Object> attributes;
	private ArrayList<Object> alAttributes;
	private ArrayList<Object> transportProtocols;
	private ArrayList<Object> appProtocols;

	public ArrayList<Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(ArrayList<Object> attributes) {
		this.attributes = attributes;
	}

	public ArrayList<Object> getAlAttributes() {
		return alAttributes;
	}

	public void setAlAttributes(ArrayList<Object> alAttributes) {
		this.alAttributes = alAttributes;
	}

	public ArrayList<Object> getAppProtocols() {
		return appProtocols;
	}

	public void setAppProtocols(ArrayList<Object> appProtocols) {
		this.appProtocols = appProtocols;
	}

	public ArrayList<Object> getTransportProtocols() {
		return transportProtocols;
	}

	public void setTransportProtocols(ArrayList<Object> transportProtocols) {
		this.transportProtocols = transportProtocols;
	}
}
