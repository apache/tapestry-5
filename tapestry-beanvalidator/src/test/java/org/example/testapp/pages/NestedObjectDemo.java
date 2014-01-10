package org.example.testapp.pages;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
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
    
    public void onActivate() {
        if (complexBean == null) { 
            complexBean = new ComplexBean();
            SomeSimpleBean otherSimpleBean = new SomeSimpleBean();
            complexBean.setOtherSimpleBean(otherSimpleBean);
        }
    }
    
    void onSuccess() {
        alertManager.alert(Duration.TRANSIENT, Severity.SUCCESS, "Validation passed");
    }

}
