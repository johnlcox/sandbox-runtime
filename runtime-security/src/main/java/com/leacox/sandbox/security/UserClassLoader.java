package com.leacox.sandbox.security;

import com.leacox.sandbox.security.permission.UserSetAccessiblePermission;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A class loader for loading runtime user jars.
 *
 * <p>This classloader allows isolating user code from runtime code and other user code.
 *
 * @author John Leacox
 */
public class UserClassLoader extends URLClassLoader {
  public UserClassLoader(URL userJarPath, ClassLoader parent) {
    super(new URL[] {userJarPath}, parent);
  }

  private static final Set<String> forbiddenClasses = newHashSet(
      "java.io.ObjectInputStream",
      "java.io.ObjectOutputStream",
      "java.io.ObjectStreamField",
      "java.io.ObjectStreamClass",
      "java.util.logging.Logger",
      "java.sql.DriverManager",
      "javax.sql.rowset.serial.SerialJavaObject",
      "java.lang.ClassLoader"
  );

  private static final Set<String> forbiddenPackages = newHashSet(
      "com.leacox.sandbox.runtime.security",
      "net.bytebuddy"
  );

  private static Set<String> newHashSet(String... args) {
    Set<String> set = new HashSet<>(args.length);
    Collections.addAll(set, args);
    return set;
  }

  /**
   * Checks if the class name is in the forbiddenClasses or forbiddenPackages sets and throws a
   * {@code SecurityException} if it is.
   */
  private void checkForbiddenClass(String name) {
    if (forbiddenClasses.contains(name) || forbiddenPackages.stream().anyMatch(name::startsWith)) {
      throw new SecurityException("This class [" + name + "] is disabled.");
    }
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    checkForbiddenClass(name);

    return super.findClass(name);
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    checkForbiddenClass(name);

    return super.loadClass(name, resolve);
  }

  @Override
  protected PermissionCollection getPermissions(CodeSource codesource) {
    PermissionCollection permissions = super.getPermissions(codesource);
    // Give the user loader permissions, including more fine-grained reflection (setAccessible)
    // permission. These user permissions could be initialized other ways (a one time use setter,
    // some sort of factory), but since the UserSetAccessiblePermission requires a reference to this
    // classloader we can't simply pass a collection of permissions to the constructor of this
    // loader. For simplicity sake the permissions are hardcoded for now.
    permissions.add(new UserSetAccessiblePermission(this));
    permissions.add(new RuntimePermission("accessDeclaredMembers"));
    return permissions;
  }
}
