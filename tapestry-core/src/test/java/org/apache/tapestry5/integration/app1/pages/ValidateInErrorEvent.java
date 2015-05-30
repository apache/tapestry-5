package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;

@Import(module = {"validate-in-error"})
public class ValidateInErrorEvent {
    @Property
    private String value;
}
