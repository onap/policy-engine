/*-
 * ============LICENSE_START=======================================================
 * ONAP-REST
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.Setter;

/**
 * The persistent class for the Obadvice database table.
 */
@Entity
@Table(name = "Obadvice")
@NamedQuery(name = "Obadvice.findAll", query = "SELECT o FROM Obadvice o")
@Getter
@Setter
public class Obadvice implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String OBLIGATION = "Obligation";
    public static final String ADVICE = "Advice";
    public static final String EFFECT_PERMIT = "Permit";
    public static final String EFFECT_DENY = "Deny";
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "xacml_id", nullable = false, length = 255)
    private String xacmlId;

    @Column(name = "fulfill_on", nullable = true, length = 32)
    private String fulfillOn;

    @Column(name = "description", nullable = true, length = 2048)
    private String description;

    // bi-directional one-to-many association to Attribute Assignment
    @OneToMany(mappedBy = "obadvice", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private Set<ObadviceExpression> obadviceExpressions = new HashSet<>(2);

    @Column(name = "created_by", nullable = false, length = 255)
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", nullable = false, updatable = false)
    private Date createdDate;

    @Column(name = "modified_by", nullable = false, length = 255)
    private String modifiedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_date", nullable = false)
    private Date modifiedDate;

    /**
     * Instantiates a new obadvice.
     */
    public Obadvice() {
        this.type = Obadvice.OBLIGATION;
        this.fulfillOn = Obadvice.EFFECT_PERMIT;
    }

    /**
     * Instantiates a new obadvice.
     *
     * @param domain the domain
     * @param userid the userid
     */
    public Obadvice(String domain, String userid) {
        this.xacmlId = domain;
        this.type = Obadvice.OBLIGATION;
        this.fulfillOn = Obadvice.EFFECT_PERMIT;
        this.createdBy = userid;
        this.modifiedBy = userid;
    }

    /**
     * Instantiates a new obadvice.
     *
     * @param id the id
     * @param userid the userid
     */
    public Obadvice(Identifier id, String userid) {
        this(id.stringValue(), userid);
    }

    /**
     * Pre persist.
     */
    @PrePersist
    public void prePersist() {
        Date date = new Date();
        this.createdDate = date;
        this.modifiedDate = date;
    }

    /**
     * Pre update.
     */
    @PreUpdate
    public void preUpdate() {
        this.modifiedDate = new Date();
    }

    /**
     * Adds the obadvice expression.
     *
     * @param obadviceExpression the obadvice expression
     * @return the obadvice expression
     */
    public ObadviceExpression addObadviceExpression(ObadviceExpression obadviceExpression) {
        this.obadviceExpressions.add(obadviceExpression);
        obadviceExpression.setObadvice(this);

        return obadviceExpression;
    }

    /**
     * Removes the obadvice expression.
     *
     * @param obadviceExpression the obadvice expression
     * @return the obadvice expression
     */
    public ObadviceExpression removeObadviceExpression(ObadviceExpression obadviceExpression) {
        this.obadviceExpressions.remove(obadviceExpression);
        obadviceExpression.setObadvice(null);

        return obadviceExpression;
    }

    /**
     * Removes the all expressions.
     */
    public void removeAllExpressions() {
        if (this.obadviceExpressions == null) {
            return;
        }
        for (ObadviceExpression expression : this.obadviceExpressions) {
            expression.setObadvice(null);
        }
        this.obadviceExpressions.clear();
    }

    /**
     * Clone.
     *
     * @return the obadvice
     */
    @Transient
    @Override
    public Obadvice clone() {
        Obadvice obadvice = new Obadvice();

        obadvice.type = this.type;
        obadvice.xacmlId = this.xacmlId;
        obadvice.fulfillOn = this.fulfillOn;
        obadvice.description = this.description;
        obadvice.createdBy = this.createdBy;
        obadvice.modifiedBy = this.modifiedBy;
        for (ObadviceExpression exp : this.obadviceExpressions) {
            obadvice.addObadviceExpression(exp.clone());
        }

        return obadvice;
    }
}
