package com.leacox.sandbox.runtime.security.transform;

import net.bytebuddy.pool.TypePool;

/**
 * A context for the transformation of classes as part of the bootstrap class loader.
 *
 * @author John Leacox
 */
public class BootstrapTransformationContext {
  private final TypePool bootstrapPool;

  /**
   * Creates a new instance of {@code BootstrapTransformationContext}.
   */
  public BootstrapTransformationContext(TypePool bootstrapPool) {
    this.bootstrapPool = bootstrapPool;
  }

  /**
   * Returns the {@link TypePool} for the bootstrap classloader and classpath.
   */
  public TypePool getBootstrapPool() {
    return bootstrapPool;
  }
}