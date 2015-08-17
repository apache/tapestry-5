package org.example.testapp.pages;

import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.corelib.components.RadioGroup;
import org.apache.tapestry5.services.PropertyEditContext;
import org.example.testapp.entities.BeanForTAP1981;

public class RadioGroupWithValidation {

  @Component(parameters = { "value=bean.number", "validate=prop:beanValidator" })
  private RadioGroup group2;

  /* */
  @Persist
  private BeanForTAP1981 bean;

  /* */
  @Environmental
  private PropertyEditContext context;

  public BeanForTAP1981 getBean() {
    if (bean == null) {
      bean = new BeanForTAP1981();
    }
    return bean;
  }

  public FieldValidator getBeanValidator() {
    return context.getValidator(group2);
  }

}
