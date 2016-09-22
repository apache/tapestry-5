package com.example;

public enum Animal {

  CAT("Cat"), DOG("Dog");
  
  private final String displayName;

  private Animal(String displayName) {
    this.displayName = displayName;
  }
  
  @Override
  public String toString() {
    return displayName;
  }
  
}
