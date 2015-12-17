/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.client.console.wizards.any;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.syncope.client.console.panels.search.SearchClause;
import org.apache.syncope.client.console.panels.search.SearchUtils;
import org.apache.syncope.client.lib.SyncopeClient;
import org.apache.syncope.common.lib.search.AbstractFiqlSearchConditionBuilder;
import org.apache.syncope.common.lib.to.GroupTO;

public class GroupHandler extends AnyHandler<GroupTO> {

    private static final long serialVersionUID = 8058288034211558376L;

    private List<SearchClause> uDynClauses;

    private Map<String, List<SearchClause>> aDynClauses;

    public GroupHandler(final GroupTO anyTO) {
        super(anyTO);
    }

    public List<SearchClause> getUDynClauses() {
        if (this.uDynClauses == null) {
            this.uDynClauses = SearchUtils.getSearchClauses(this.anyTO.getUDynMembershipCond());
        }
        return this.uDynClauses;
    }

    public void setUDynClauses(final List<SearchClause> uDynClauses) {
        this.uDynClauses = uDynClauses;
    }

    public Map<String, List<SearchClause>> getADynClauses() {
        if (this.aDynClauses == null) {
            this.aDynClauses = SearchUtils.getSearchClauses(this.anyTO.getADynMembershipConds());
        }
        return this.aDynClauses;
    }

    public void setADynClauses(final Map<String, List<SearchClause>> aDynClauses) {
        this.aDynClauses = aDynClauses;
    }

    public String getUDynMembershipCond() {
        if (CollectionUtils.isEmpty(this.uDynClauses)) {
            return this.anyTO.getUDynMembershipCond();
        } else {
            return getFIQLString(this.uDynClauses, SyncopeClient.getUserSearchConditionBuilder());
        }
    }

    public Map<String, String> getADynMembershipConds() {
        if (this.aDynClauses == null || this.aDynClauses.isEmpty()) {
            return this.anyTO.getADynMembershipConds();
        } else {
            final Map<String, String> res = new HashMap<>();

            for (Map.Entry<String, List<SearchClause>> entry : this.aDynClauses.entrySet()) {
                if (CollectionUtils.isNotEmpty(entry.getValue())) {
                    res.put(entry.getKey(), getFIQLString(entry.getValue(),
                            SyncopeClient.getAnyObjectSearchConditionBuilder(entry.getKey())));
                }
            }

            return res;
        }
    }

    private String getFIQLString(final List<SearchClause> clauses, final AbstractFiqlSearchConditionBuilder bld) {
        return SearchUtils.buildFIQL(clauses, bld);
    }

    public GroupTO fillDynamicConditions() {
        this.anyTO.setUDynMembershipCond(this.getUDynMembershipCond());
        this.anyTO.getADynMembershipConds().putAll(this.getADynMembershipConds());
        return this.anyTO;
    }
}