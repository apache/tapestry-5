package org.apache.tapestry5.integration.app1.pages

import org.apache.tapestry5.annotations.Import
import org.apache.tapestry5.annotations.Persist

// See TAP5-2249
@Import(stylesheet = "empty-if-demo.css")
class EmptyIfDemo {

    @Persist
    boolean show

    void onActionFromHide() {
        show = false
    }

    void onActionFromShow() {
        show = true
    }


}
