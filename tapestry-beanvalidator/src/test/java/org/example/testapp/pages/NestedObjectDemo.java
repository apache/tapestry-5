package org.example.testapp.pages;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.ClientValidation;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.example.testapp.entities.ComplexBean;
import org.example.testapp.entities.SomeSimpleBean;

public class NestedObjectDemo
{

    @Property
    @Persist
    private ComplexBean complexBean;
    
    @Property
    @Persist
    private String notNullString;
    
    @Inject
    private AlertManager alertManager;
    
    @Property
    private boolean enableClientValidation;
    
    public void onActivate(boolean enableClientValidation) {
    	this.enableClientValidation = enableClientValidation;
        if (complexBean == null) { 
            complexBean = new ComplexBean();
            SomeSimpleBean otherSimpleBean = new SomeSimpleBean();
            complexBean.setOtherSimpleBean(otherSimpleBean);
        }
    }
    
    void onSuccess() {
        alertManager.alert(Duration.TRANSIENT, Severity.SUCCESS, "Validation passed");
    }
    
    public ClientValidation getClientValidation() {
    	return enableClientValidation ? ClientValidation.SUBMIT : ClientValidation.NONE;
    }
    
    Boolean onPassivate() {
    	return enableClientValidation;
    }

}
