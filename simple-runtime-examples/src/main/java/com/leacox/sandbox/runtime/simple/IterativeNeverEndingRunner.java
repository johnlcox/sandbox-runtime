package com.leacox.sandbox.runtime.simple;

/**
 * A runner that attempts to avoid being killed using an iterative approach.
 *
 * <p>In practice, using Oracle JDK 1.8.0_91, this class sometimes locks up the entire JVM after the
 * runtime tries to stop it around 12,000 to 20,000 times. I'm unsure why this is occurring.
 *
 * @author John Leacox
 */
public class IterativeNeverEndingRunner implements Runnable {
  @Override
  public void run() {
    try {
      int i = 0;
      while (true) {
        try {
          i++;
        } catch (ThreadDeath td) {
        }
      }
    } catch (ThreadDeath td) {
      run();
    }
  }
}
