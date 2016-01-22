/*
 * Copyright 2015 The Apache Software Foundation.
 *
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
 */
package org.apache.syncope.client.console.wizards;

import java.io.Serializable;
import org.apache.syncope.client.console.panels.ModalPanel;
import org.apache.syncope.client.console.panels.NotificationPanel;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.extensions.wizard.WizardModel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AjaxWizard<T extends Serializable> extends Wizard implements ModalPanel<T> {

    private static final long serialVersionUID = -1272120742876833520L;

    protected static final Logger LOG = LoggerFactory.getLogger(AjaxWizard.class);

    private T item;

    private final boolean edit;

    private NotificationPanel feedbackPanel;

    /**
     * Construct.
     *
     * @param id The component id.
     * @param item model object.
     * @param model wizard model
     * @param edit <tt>true</tt> if edit mode.
     */
    public AjaxWizard(final String id, final T item, final WizardModel model, final boolean edit) {
        super(id);
        this.item = item;
        this.edit = edit;
        setOutputMarkupId(true);
        setDefaultModel(new CompoundPropertyModel<>(this));
        init(model);
    }

    @Override
    protected Component newButtonBar(final String id) {
        return new AjaxWizardButtonBar(id, this, this.edit);
    }

    @Override
    protected Component newFeedbackPanel(final String id) {
        if (feedbackPanel == null) {
            feedbackPanel = new NotificationPanel(id);
        }
        return feedbackPanel;
    }

    public NotificationPanel getFeedbackPanel() {
        return feedbackPanel;
    }

    protected abstract void onCancelInternal();

    protected abstract Serializable onApplyInternal();

    /**
     * @see org.apache.wicket.extensions.wizard.Wizard#onCancel()
     */
    @Override
    public final void onCancel() {
        final AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
        try {
            onCancelInternal();
            send(AjaxWizard.this, Broadcast.BUBBLE, new NewItemCancelEvent<>(item, target));
        } catch (Exception e) {
            LOG.warn("Wizard error on cancel", e);
            error(getString("wizard.cancel.error"));
            feedbackPanel.refresh(target);
        }
    }

    /**
     * @see org.apache.wicket.extensions.wizard.Wizard#onFinish()
     */
    @Override
    public final void onFinish() {
        final AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
        try {
            final Serializable res = onApplyInternal();
            send(AjaxWizard.this, Broadcast.BUBBLE, new NewItemFinishEvent<>(item, target).setResult(res));
        } catch (Exception e) {
            LOG.warn("Wizard error on finish", e);
            error(getString("wizard.apply.error"));
            feedbackPanel.refresh(target);
        }
    }

    @Override
    public T getItem() {
        return item;
    }

    /**
     * Replaces the default value provided with the constructor.
     *
     * @param item new value.
     * @return the current wizard instance.
     */
    public AjaxWizard<T> setItem(final T item) {
        this.item = item;
        return this;
    }

    public abstract static class NewItemEvent<T> {

        private final T item;

        private final AjaxRequestTarget target;

        public NewItemEvent(final T item, final AjaxRequestTarget target) {
            this.item = item;
            this.target = target;
        }

        public T getItem() {
            return item;
        }

        public AjaxRequestTarget getTarget() {
            return target;
        }

        public abstract String getEventDescription();
    }

    public static class NewItemActionEvent<T> extends NewItemEvent<T> {

        private static final String EVENT_DESCRIPTION = "new";

        private int index = 0;

        public NewItemActionEvent(final T item, final AjaxRequestTarget target) {
            super(item, target);
        }

        public NewItemActionEvent(final T item, final int index, final AjaxRequestTarget target) {
            super(item, target);
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public String getEventDescription() {
            return NewItemActionEvent.EVENT_DESCRIPTION;
        }
    }

    public static class EditItemActionEvent<T> extends NewItemActionEvent<T> {

        private static final String EVENT_DESCRIPTION = "edit";

        public EditItemActionEvent(final T item, final AjaxRequestTarget target) {
            super(item, target);
        }

        public EditItemActionEvent(final T item, final int index, final AjaxRequestTarget target) {
            super(item, index, target);
        }

        @Override
        public String getEventDescription() {
            return EditItemActionEvent.EVENT_DESCRIPTION;
        }
    }

    public static class NewItemCancelEvent<T> extends NewItemEvent<T> {

        private static final String EVENT_DESCRIPTION = "cancel";

        public NewItemCancelEvent(final T item, final AjaxRequestTarget target) {
            super(item, target);
        }

        @Override
        public String getEventDescription() {
            return NewItemCancelEvent.EVENT_DESCRIPTION;
        }
    }

    public static class NewItemFinishEvent<T> extends NewItemEvent<T> {

        private static final String EVENT_DESCRIPTION = "finish";

        private Serializable result;

        public NewItemFinishEvent(final T item, final AjaxRequestTarget target) {
            super(item, target);
        }

        @Override
        public String getEventDescription() {
            return NewItemFinishEvent.EVENT_DESCRIPTION;
        }

        public NewItemFinishEvent<T> setResult(final Serializable result) {
            this.result = result;
            return this;
        }

        public Serializable getResult() {
            return result;
        }
    }

    @Override
    public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
        onApplyInternal();
    }

    @Override
    public void onError(final AjaxRequestTarget target, final Form<?> form) {
        feedbackPanel.refresh(target);
    }
}