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
package org.apache.syncope.client.console.panels;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.client.console.SyncopeConsoleSession;
import org.apache.syncope.client.console.commons.Constants;
import org.apache.syncope.client.console.commons.SortableDataProviderComparator;
import org.apache.syncope.client.console.pages.AbstractBasePage;
import org.apache.syncope.client.console.wicket.markup.html.bootstrap.dialog.BaseModal;
import org.apache.syncope.client.console.wicket.markup.html.form.ActionLink;
import org.apache.syncope.client.console.wicket.markup.html.form.ActionLinksPanel;
import org.apache.syncope.common.lib.to.SecurityQuestionTO;
import org.apache.syncope.common.lib.types.StandardEntitlement;
import org.apache.syncope.common.rest.api.service.SecurityQuestionService;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityQuestionsPanel extends AbstractTypesPanel<SecurityQuestionTO> {

    private static final long serialVersionUID = 3323019773236588850L;

    private static final Logger LOG = LoggerFactory.getLogger(SecurityQuestionsPanel.class);

    private static final String PAGINATOR_ROWS_KEYS = Constants.PREF_SECURITY_QUESTIONS_PAGINATOR_ROWS;

    private final BaseModal<SecurityQuestionTO> modal;

    public SecurityQuestionsPanel(
            final String id, final PageReference pageReference, final BaseModal<SecurityQuestionTO> modal) {
        super(id, pageReference);
        this.pageRows = prefMan.getPaginatorRows(getRequest(), PAGINATOR_ROWS_KEYS);
        this.modal = modal;

        final WebMarkupContainer container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true);
        add(container);

        buildDataTable(container,
                getColumns(container, pageReference),
                new SecurityQuestionsPanel.SecurityQuestionsProvider(),
                PAGINATOR_ROWS_KEYS);

    }

    private List<IColumn<SecurityQuestionTO, String>> getColumns(
            final WebMarkupContainer webContainer, final PageReference pageReference) {

        final List<IColumn<SecurityQuestionTO, String>> columns = new ArrayList<>();

        for (Field field : SecurityQuestionTO.class.getDeclaredFields()) {

            if (field != null && !Modifier.isStatic(field.getModifiers())) {
                final String fieldName = field.getName();
                if (field.getType().isArray()) {
                    final IColumn<SecurityQuestionTO, String> column = new PropertyColumn<SecurityQuestionTO, String>(
                            new ResourceModel(field.getName()), field.getName()) {

                        private static final long serialVersionUID = 3282547854226892169L;

                        @Override
                        public String getCssClass() {
                            String css = super.getCssClass();
                            if ("key".equals(fieldName)) {
                                css = StringUtils.isBlank(css)
                                        ? "medium_fixedsize"
                                        : css + " medium_fixedsize";
                            }
                            return css;
                        }
                    };
                    columns.add(column);

                } else {
                    final IColumn<SecurityQuestionTO, String> column = new PropertyColumn<SecurityQuestionTO, String>(
                            new ResourceModel(field.getName()), field.getName(), field.getName()) {

                        private static final long serialVersionUID = 3282547854226892169L;

                        @Override
                        public String getCssClass() {
                            String css = super.getCssClass();
                            if ("key".equals(fieldName)) {
                                css = StringUtils.isBlank(css)
                                        ? "medium_fixedsize"
                                        : css + " medium_fixedsize";
                            }
                            return css;
                        }
                    };
                    columns.add(column);
                }
            }
        }

        columns.add(new AbstractColumn<SecurityQuestionTO, String>(new ResourceModel("actions", "")) {

            private static final long serialVersionUID = 2054811145491901166L;

            @Override
            public String getCssClass() {
                return "action";
            }

            @Override
            public void populateItem(final Item<ICellPopulator<SecurityQuestionTO>> item, final String componentId,
                    final IModel<SecurityQuestionTO> model) {

                final SecurityQuestionTO securityQuestionTO = model.getObject();

                final ActionLinksPanel.Builder<Serializable> actionLinks = ActionLinksPanel.builder(pageReference);
                actionLinks.setDisableIndicator(true);
                actionLinks
                        .addWithRoles(new ActionLink<Serializable>() {

                            private static final long serialVersionUID = -3722207913631435501L;

                            @Override
                            public void onClick(final AjaxRequestTarget target, final Serializable ignore) {
                                modal.header(Model.of(String.valueOf(securityQuestionTO.getKey())));
                                modal.setFormModel(securityQuestionTO);
                                target.add(modal.setContent(
                                        new SecurityQuestionModalPanel(modal, pageReference, false)));
                                modal.addSumbitButton();
                                modal.show(true);
                            }
                        }, ActionLink.ActionType.EDIT, StandardEntitlement.SECURITY_QUESTION_UPDATE)
                        .addWithRoles(new ActionLink<Serializable>() {

                            private static final long serialVersionUID = -3722207913631435501L;

                            @Override
                            public void onClick(final AjaxRequestTarget target, final Serializable ignore) {
                                try {
                                    SyncopeConsoleSession.get().getService(SecurityQuestionService.class
                                    ).delete(securityQuestionTO.getKey());
                                    info(getString(Constants.OPERATION_SUCCEEDED));
                                    target.add(webContainer);
                                } catch (Exception e) {
                                    LOG.error("While deleting SecutiryQuestionTO", e);
                                    error(getString(Constants.ERROR) + ": " + e.getMessage());
                                }
                                ((AbstractBasePage) getPage()).getNotificationPanel().refresh(target);

                            }
                        }, ActionLink.ActionType.DELETE, StandardEntitlement.SECURITY_QUESTION_DELETE);

                item.add(actionLinks.build(componentId));
            }
        });

        return columns;

    }

    private final class SecurityQuestionsProvider extends SortableDataProvider<SecurityQuestionTO, String> {

        private static final long serialVersionUID = -185944053385660794L;

        private final SortableDataProviderComparator<SecurityQuestionTO> comparator;

        private SecurityQuestionsProvider() {
            super();
            setSort("key", SortOrder.ASCENDING);
            comparator = new SortableDataProviderComparator<>(this);
        }

        @Override
        public Iterator<SecurityQuestionTO> iterator(final long first, final long count) {
            final List<SecurityQuestionTO> list = SyncopeConsoleSession.get().getService(SecurityQuestionService.class).
                    list();
            Collections.sort(list, comparator);
            return list.subList((int) first, (int) first + (int) count).iterator();
        }

        @Override
        public long size() {
            return SyncopeConsoleSession.get().getService(SecurityQuestionService.class).list().size();
        }

        @Override
        public IModel<SecurityQuestionTO> model(final SecurityQuestionTO object) {
            return new CompoundPropertyModel<>(object);
        }
    }
}
