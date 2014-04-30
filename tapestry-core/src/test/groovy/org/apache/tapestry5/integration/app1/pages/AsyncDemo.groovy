package org.apache.tapestry5.integration.app1.pages

import org.apache.tapestry5.Block
import org.apache.tapestry5.ioc.annotations.Inject
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer


class AsyncDemo {

    @Inject
    Block clickBlock, formBlock

    @Inject
    AjaxResponseRenderer responseRenderer

    void onActionFromUpdateTargetLink() {

        responseRenderer.addRender "target", clickBlock
    }

    void onSubmitFromForm() {
        responseRenderer.addRender "target", formBlock
    }
}
