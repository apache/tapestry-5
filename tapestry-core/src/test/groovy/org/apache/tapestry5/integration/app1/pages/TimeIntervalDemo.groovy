package org.apache.tapestry5.integration.app1.pages

import org.apache.tapestry5.annotations.Persist
import org.apache.tapestry5.annotations.Property


class TimeIntervalDemo extends LocalDateDemo {

    @Persist
    @Property
    private Date previousRender;

    void setupRender() {
        if (previousRender == null)
        {
            previousRender = new Date()
            previousRender.setHours(0)
            previousRender.setMinutes(0)
            previousRender.setSeconds(0)
        }
            
    }

    void afterRender() {
        previousRender = new Date()
    }

    Date getJacobBirth() {
        return new Date(2010 - 1900, 1, 15) // Feb 15 2010
    }

    Date getJacobVote() {
        return new Date(2010 + 18 - 1900, 1, 15) // Feb 15 2028
    }
}
