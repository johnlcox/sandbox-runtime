package com.leacox.sandbox.runtime.simple;

import java.lang.reflect.Method;

/**
 * A runner that is allowed setAccessible permission because the Foo class is from the same code
 * source as the runner.
 *
 * @author John Leacox
 */
public class AllowedRunner implements Runnable {
  @Override
  public void run() {
    Foo foo = new Foo();

    String methodName = "inAccessibleMethod";

    try {
      Method method = foo.getClass().getDeclaredMethod(methodName);
      method.setAccessible(true);
      method.invoke(foo);
    } catch (Exception e) {
      System.out.println(e);
      throw new RuntimeException(e);
    }
  }
}
