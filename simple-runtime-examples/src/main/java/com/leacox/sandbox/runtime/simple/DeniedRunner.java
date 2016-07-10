package com.leacox.sandbox.runtime.simple;

import java.lang.reflect.Field;

/**
 * A runner that is denied setAccessible because the String class is not from the same code source
 * as the runner.
 *
 * @author John Leacox
 */
public class DeniedRunner implements Runnable {
  @Override
  public void run() {
    String string = "Hello World";

    String fieldName = "value";

    try {
      Field field = string.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      char[] value = (char[]) field.get(string);
      System.out.println("Should not have read this value: " + value);
    } catch (SecurityException e) {
      System.out.println("Expected Security Exception caught");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
