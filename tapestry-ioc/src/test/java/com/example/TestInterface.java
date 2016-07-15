package com.example;

public interface TestInterface {
  public default String getTestString() {
    return "Alpha";
  }
}
