package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.ioc.annotations.Inject;

public class OnActivateRedirect {

  @Persist("flash")
  @Property(write = false)
  private String message;

  @Inject
  private Request request;

  Object onActivate() {
    if (request.isXHR()) {
      message = "Redirected from XHR";
      return this;
    }
    return null;
  }

  void onAction() {

  }

}
