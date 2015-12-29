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
package org.apache.syncope.client.console.pages;

import org.apache.syncope.client.console.commons.NotificationAwareComponent;
import org.apache.syncope.client.console.panels.NotificationPanel;
import org.apache.syncope.client.console.wicket.markup.head.MetaHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractBasePage extends WebPage implements NotificationAwareComponent {

    private static final long serialVersionUID = 8611724965544132636L;

    protected static final Logger LOG = LoggerFactory.getLogger(AbstractBasePage.class);

    protected static final String CANCEL = "cancel";

    protected static final String SUBMIT = "submit";

    protected static final String APPLY = "apply";

    protected static final String FORM = "form";

    protected final HeaderItem meta = new MetaHeaderItem("X-UA-Compatible", "IE=edge");

    protected NotificationPanel notificationPanel;

    /**
     * Response flag set by the Modal Window after the operation is completed.
     */
    protected boolean modalResult = false;

    public AbstractBasePage() {
        this(null);
    }

    public AbstractBasePage(final PageParameters parameters) {
        super(parameters);

    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        response.render(new PriorityHeaderItem(meta));
    }

    @Override
    public NotificationPanel getNotificationPanel() {
        return notificationPanel;
    }
}
