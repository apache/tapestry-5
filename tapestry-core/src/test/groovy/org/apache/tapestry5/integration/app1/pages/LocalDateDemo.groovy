package org.apache.tapestry5.integration.app1.pages

import org.apache.tapestry5.annotations.Cached

class LocalDateDemo {

    @Cached
    Date getNow() {
        new Date()
    }
}
