package com.leacox.sandbox.security.permission;

import com.leacox.sandbox.security.UserClassLoaders;

import java.security.CodeSource;
import java.security.Permission;
import java.util.Objects;

/**
 * A more fine grained permission around the {@code AccessibleObject#setAccessible(...)} methods.
 *
 * <p>This permission allows calling setAccessible on object when both the object and the caller are
 * part of the same {@link CodeSource}.
 *
 * @author John Leacox
 */
public class UserSetAccessiblePermission extends Permission {
  private final ClassLoader loader;

  /**
   * Creates a new UserSetAccessiblePermission for an object loaded by a specific user class loader.
   *
   * @param loader the class loader of the object {@code setAccessible} is being called on
   * @throws NullPointerException if loader is null
   * @throws IllegalArgumentException if loader is not an instance of {@code UserClassLoader}
   */
  public UserSetAccessiblePermission(ClassLoader loader) {
    super("userSetAccessible");

    if (loader == null) {
      throw new NullPointerException("loader:null");
    } else if (!UserClassLoaders.isUserClassLoader(loader)) {
      throw new IllegalArgumentException("loader:Must be a user class loader");
    }

    this.loader = loader;
  }

  @Override
  public boolean implies(Permission permission) {
    if (!(permission instanceof UserSetAccessiblePermission))
      return false;


    UserSetAccessiblePermission that = (UserSetAccessiblePermission) permission;

    return that.loader == this.loader;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserSetAccessiblePermission that = (UserSetAccessiblePermission) o;
    return Objects.equals(loader, that.loader);
  }

  @Override
  public int hashCode() {
    return Objects.hash(loader);
  }

  @Override
  public String getActions() {
    return "";
  }
}
