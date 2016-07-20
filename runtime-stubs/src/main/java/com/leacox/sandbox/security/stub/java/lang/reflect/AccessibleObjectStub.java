package com.leacox.sandbox.security.stub.java.lang.reflect;

import com.leacox.sandbox.security.UserClassLoaders;
import com.leacox.sandbox.security.permission.UserSetAccessiblePermission;

import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ReflectPermission;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;

/**
 * A stub class for the {@link AccessibleObject} class.
 *
 * <p>This class provides a replacement method for the {@code setAccessible(...)} methods of the
 * {@code AccessibleObject} class that give more fine grained permission control over the
 * setAccessible reflection permission.
 *
 * @author John Leacox
 */
public class AccessibleObjectStub {
  //private static final Logger logger = Logger.getLogger(AccessibleObjectStub.class.getName());
  private final static Permission STANDARD_ACCESS_PERMISSION = new ReflectPermission("suppressAccessChecks");

  /**
   * A stub replacement method for {@link AccessibleObject#setAccessible(boolean)} that adds an
   * additional permission check for
   * {@link UserSetAccessiblePermission} if the object
   * {@code setAccessible} is being called on is from a user class loader.
   *
   * <p>This stub alone is not enough for {@code setAccessible} functionality. If this method
   * successfully returns (e.g. does not through a {@link SecurityException}, then the original
   * {@link AccessibleObject#setAccessible0(AccessibleObject, boolean)} method must be called
   * to actually set the accessibility flag. This is configured via the matching transformation
   * definition {@code AccessibleObjectTransformer} which adds the {@code setAccessible0} method
   * as a chained method of the method interception.
   *
   * @param ao the AccessibleObject {@code setAccessible} was called on
   * @param flag the new value for the {@code accessible} flag
   * @throws SecurityException if the request is denied
   */
  public static void setAccessible(@This AccessibleObject ao, boolean flag)
      throws SecurityException {
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      Permission permission = STANDARD_ACCESS_PERMISSION;
      if (isFromUserLoader(ao)) {
        try {
          permission = getUserAccessPermission(ao);
        } catch (Exception e) {
          // Ignore. Use standard permission.
          //logger.log(Level.INFO,
          //    "Unable to create user permission. Fall back to standard permission.", e);
        }
      }

      sm.checkPermission(permission);
    }
  }

  public static void setAccessible(AccessibleObject[] array, boolean flag) {
    for (AccessibleObject ao : array) {
      setAccessible(ao, flag);
    }
  }

  private static Permission getUserAccessPermission(AccessibleObject ao)
      throws IllegalAccessException, InvocationTargetException, InstantiationException,
      NoSuchMethodException, ClassNotFoundException {
    ClassLoader aoClassLoader = getAccessibleObjectLoader(ao);
    return new UserSetAccessiblePermission(aoClassLoader);
  }

  private static ClassLoader getAccessibleObjectLoader(AccessibleObject ao) {
    return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
      @Override
      public ClassLoader run() {
        if (ao instanceof Executable) {
          return ((Executable) ao).getDeclaringClass().getClassLoader();
        } else if (ao instanceof Field) {
          return ((Field) ao).getDeclaringClass().getClassLoader();
        }

        throw new IllegalStateException("Unknown AccessibleObject type: " + ao.getClass());
      }
    });
  }

  private static boolean isFromUserLoader(AccessibleObject ao) {
    ClassLoader loader = getAccessibleObjectLoader(ao);

    if (loader == null) {
      return false;
    }

    return UserClassLoaders.isUserClassLoader(loader);
  }
}
