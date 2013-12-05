package org.apache.tapestry5.integration.app1.pages

import org.apache.tapestry5.annotations.Cached
import org.apache.tapestry5.ioc.annotations.Inject
import org.apache.tapestry5.services.PersistentLocale

class LocalDateDemo {

    @Inject
    PersistentLocale persistentLocale

    @Cached
    Date getNow() {
        new Date()
    }

    void onActionFromFrench() {
        persistentLocale.set Locale.FRENCH
    }

    void onActionFromEnglish() {
        persistentLocale.set Locale.ENGLISH
    }
}
