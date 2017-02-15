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
package org.openecomp.policy.xacml.std.pap;

import java.util.Collection;
import java.util.LinkedList;

import org.openecomp.policy.xacml.api.pap.EcompPDP;
import org.openecomp.policy.xacml.api.pap.EcompPDPGroup;

import com.att.research.xacml.api.pap.PDP;
import com.att.research.xacml.api.pap.PDPGroup;

public class StdPDPItemSetChangeNotifier {
	
	private Collection<StdItemSetChangeListener> listeners = null;
	
	public interface StdItemSetChangeListener {
		
		public void changed();
		
		public void groupChanged(EcompPDPGroup group);
		
		public void pdpChanged(EcompPDP pdp);

	}
	
	public void addItemSetChangeListener(StdItemSetChangeListener listener) {
		if (this.listeners == null) {
			this.listeners = new LinkedList<StdItemSetChangeListener>();
		}
		this.listeners.add(listener);
	}
	
	public void removeItemSetChangeListener(StdItemSetChangeListener listener) {
		if (this.listeners != null) {
			this.listeners.remove(listener);
		}
	}

	public void fireChanged() {
		if (this.listeners == null) {
			return;
		}
		for (StdItemSetChangeListener l : this.listeners) {
			l.changed();
		}		
	}

	public void firePDPGroupChanged(EcompPDPGroup group) {
		if (this.listeners == null) {
			return;
		}
		for (StdItemSetChangeListener l : this.listeners) {
			l.groupChanged(group);
		}
	}

	public void firePDPChanged(EcompPDP pdp) {
		if (this.listeners == null) {
			return;
		}
		for (StdItemSetChangeListener l : this.listeners) {
			l.pdpChanged(pdp);
		}
	}
}
