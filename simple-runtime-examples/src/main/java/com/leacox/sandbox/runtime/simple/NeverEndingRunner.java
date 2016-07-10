package com.leacox.sandbox.runtime.simple;

/**
 * A runner that attempts to avoid being killed using a recursive approach.
 *
 * <p>This will eventually throw a StackOverflowError leading to it being killed anyway.
 *
 * @author John Leacox
 */
public class NeverEndingRunner implements Runnable {
  @Override
  public void run() {
    int i = 0;
    while (true) {
      try {
        i++;
      } catch (ThreadDeath td1) {
        catchIt();
      }
    }
  }

  private void catchIt() {
    try {
      System.out.println("Catch recursive");
      catchIt();
    } catch (ThreadDeath td) {
      catchIt();
    }
  }
}
