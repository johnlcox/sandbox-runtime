package com.leacox.sandbox.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.security.AccessController;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;

/**
 * A policy that grants {@code AllPermission} to the application/runtime class loader and a limited
 * set of permissions to the {@link UserClassLoader}.
 *
 * <p>In order to apply different permissions to user code vs application code the classes must be
 * isolated in different class loaders.
 *
 * <p>Additionally, since class loaders are hierarchical it is best to have a minimal set of class
 * on the classpath when the application is bootstrapped. The bootstrap/system class loaders will be
 * available to both application and user class loaders. The best way to accomplish this is to have
 * a bootstrapping class that creates a separate isolated application classloader by loading
 * additional jars at runtime and then a separate user classloader that loaders the users jar.
 *
 * @author John Leacox
 */
public class RuntimePolicy extends Policy {
  private static final Logger logger = LoggerFactory.getLogger(RuntimePolicy.class);
  private static final Marker securityMarker = MarkerFactory.getMarker("SECURITY");

  private final ProtectionDomain providerDomain;
  private final Permissions allPermissions;

  /**
   * Creates a new instance of {@code RuntimePolicy}.
   */
  public RuntimePolicy() {
    // Determine this policies domain. This is necessary to avoid a chicken-and-egg problem.
    Policy thisPolicy = this;
    this.providerDomain = AccessController.doPrivileged(
        (PrivilegedAction<ProtectionDomain>) () -> thisPolicy.getClass().getProtectionDomain());

    Permissions permissions = new Permissions();
    permissions.add(new AllPermission());
    permissions.setReadOnly();
    this.allPermissions = permissions;
  }

  private boolean isPolicy(ProtectionDomain domain) {
    return providerDomain == domain;
  }

  private boolean isUser(ProtectionDomain domain) {
    return domain.getClassLoader() instanceof UserClassLoader;
  }

  @Override
  public PermissionCollection getPermissions(ProtectionDomain domain) {
    boolean isPolicy = isPolicy(domain);
    boolean isUser = isUser(domain);

    String sourceLocation = codeSourceLocation(domain);
    String classLoaderName = classLoaderName(domain);
    logger.info(securityMarker,
        "getPermissions, codeSource = {}, classLoader = {}, isUser = {}",
        sourceLocation, classLoaderName, isPolicy, isUser);

    Permissions permissions;
    if (isPolicy || !isUser) {
      permissions = allPermissions;
    } else {
      permissions = userPermissions();
    }

    logger
        .info(securityMarker,
            "getPermissions, codeSource = {}, classLoader = {}, isUser = {}, permissions = {}",
            sourceLocation, classLoaderName, isUser, permissions);

    return permissions;
  }

  @Override
  public boolean implies(ProtectionDomain domain, Permission permission) {
    boolean isUser = isUser(domain);

    String sourceLocation = codeSourceLocation(domain);
    String classLoaderName = classLoaderName(domain);
    logger.info(securityMarker,
        "implies, codeSource = {}, classLoader = {}, permission = {}, isUser = {}",
        sourceLocation, classLoaderName, permission, isUser);

    boolean implies;
    if (isPolicy(domain)) {
      implies = true;
    } else {
      implies = super.implies(domain, permission);
    }

    logger.info(securityMarker,
        "implies, codeSource = {}, classLoader = {}, permission = {}, isUser = {}, result = {}",
        sourceLocation, classLoaderName, permission, isUser, implies);

    return implies;
  }

  private String codeSourceLocation(ProtectionDomain domain) {
    CodeSource source = domain.getCodeSource();
    if (source == null) {
      return "null";
    }

    return source.getLocation().getPath();
  }

  private String classLoaderName(ProtectionDomain domain) {
    ClassLoader loader = domain.getClassLoader();
    if (loader == null) {
      return "null";
    }

    return loader.getClass().getName();
  }

  private Permissions userPermissions() {
    // No permissions
    return new Permissions();
  }
}
