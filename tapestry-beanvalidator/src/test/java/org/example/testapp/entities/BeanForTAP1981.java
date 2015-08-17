package org.example.testapp.entities;

import javax.validation.constraints.Max;

public class BeanForTAP1981 {

  private int number;

  @Max(2)
  public int getNumber() {
    return number;
  }

  public void setNumber(final int value) {
    number = value;
  }

}
