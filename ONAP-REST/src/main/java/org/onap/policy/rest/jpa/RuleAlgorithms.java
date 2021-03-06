/*-
 * ============LICENSE_START=======================================================
 * ONAP-REST
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import com.att.research.xacml.api.Identifier;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.Setter;

/**
 * The Class RuleAlgorithms.
 */
@Entity
@Table(name = "RuleAlgorithms")
@NamedQuery(name = "RuleAlgorithms.findAll", query = "SELECT d FROM RuleAlgorithms d")
@Getter
@Setter
public class RuleAlgorithms implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final char STANDARD = 'S';
    public static final char CUSTOM = 'C';

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;

    @Column(name = "is_standard", nullable = false)
    private char isStandard;

    @Column(name = "xacml_id", nullable = false, unique = true, length = 255)
    private String xacmlId;

    @Column(name = "short_name", nullable = false, length = 64)
    private String shortName;

    /**
     * Instantiates a new rule algorithms.
     *
     * @param id the id
     * @param standard the standard
     */
    public RuleAlgorithms(Identifier id, char standard) {
        if (id != null) {
            this.xacmlId = id.stringValue();
        }
        this.isStandard = standard;
    }

    /**
     * Instantiates a new rule algorithms.
     *
     * @param id the id
     */
    public RuleAlgorithms(Identifier id) {
        this(id, RuleAlgorithms.STANDARD);
    }

    /**
     * Instantiates a new rule algorithms.
     */
    public RuleAlgorithms() {
        this(null, RuleAlgorithms.STANDARD);
    }

    @Transient
    public boolean isStandard() {
        return this.isStandard == RuleAlgorithms.STANDARD;
    }

    @Transient
    public boolean isCustom() {
        return this.isStandard == RuleAlgorithms.CUSTOM;
    }
}
