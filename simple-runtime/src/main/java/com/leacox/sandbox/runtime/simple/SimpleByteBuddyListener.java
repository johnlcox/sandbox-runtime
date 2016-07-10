package com.leacox.sandbox.runtime.simple;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.utility.JavaModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ByteBuddy agent {@link AgentBuilder.Listener} that logs and tracks errors.
 *
 * <p>By tracking when an error occurs, it allows stopping execution if an error occurs.
 *
 * @author John Leacox
 */
public class SimpleByteBuddyListener extends AgentBuilder.Listener.Adapter {
  private static final Logger logger = LoggerFactory.getLogger(SimpleByteBuddyListener.class);
  private boolean errorDuringInstrumentation = false;

  @Override
  public void onError(
      String typeName, ClassLoader classLoader, JavaModule javaModule, Throwable throwable) {
    logger.warn("ERROR on transformation " + typeName, throwable);
    this.errorDuringInstrumentation = true;
  }

  /**
   * Returns {@code true} if an error occurred during instrumentation, {@code false} otherwise.
   */
  public boolean hadErrorDuringInstrumentation() {
    return errorDuringInstrumentation;
  }
}
