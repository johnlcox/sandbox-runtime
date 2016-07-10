package com.leacox.sandbox.runtime.security.transform;

import net.bytebuddy.agent.builder.AgentBuilder;

/**
 * A definition of a ByteBuddy transformation to happen at the bootstrap classloader level.
 *
 * @author John Leacox
 */
public interface BootstrapTransformationDefinition {
  /**
   * Gets the matcher for this transformation.
   *
   * @param context the context of the bootstrap transformation
   */
  AgentBuilder.RawMatcher getRawMatcher(BootstrapTransformationContext context);

  /**
   * Gets the transformer for this transformation.
   *
   * @param context the context of the bootstrap transformation
   */
  AgentBuilder.Transformer getTransformer(BootstrapTransformationContext context);
}