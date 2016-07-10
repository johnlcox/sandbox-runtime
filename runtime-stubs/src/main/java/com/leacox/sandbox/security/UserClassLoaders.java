package com.leacox.sandbox.security;

/**
 * Static utility methods for working with a user class loader.
 *
 * @author John Leacox
 */
public class UserClassLoaders {
  private UserClassLoaders() {}

  private static final String USER_CLASS_LOADER_CLASS_NAME =
      "com.leacox.sandbox.security.UserClassLoader";

  /**
   * Returns true if loader is a {@code UserClassLoader}, false otherwise.
   */
  public static boolean isUserClassLoader(ClassLoader loader) {
    return loader.getClass().getName().equals(USER_CLASS_LOADER_CLASS_NAME);
  }
}
