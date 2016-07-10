package com.leacox.sandbox.security;

/**
 * A security manager that adds additional restrictions around the creation of threads.
 *
 * @author John Leacox
 */
public class RuntimeSecurityManager extends SecurityManager {
  private static final RuntimePermission modifyThreadPermission =
      new RuntimePermission("modifyThread");
  private static final RuntimePermission modifyThreadGroupPermission =
      new RuntimePermission("modifyThreadGroup");

  @Override
  public void checkAccess(Thread t) {
    super.checkAccess(t);

    checkPermission(modifyThreadPermission);
  }

  @Override
  public void checkAccess(ThreadGroup g) {
    super.checkAccess(g);

    checkPermission(modifyThreadGroupPermission);
  }
}
