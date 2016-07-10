package com.leacox.sandbox.runtime.simple;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * The main method and bootstrapper for the sandboxed runtime environment.
 *
 * @author John Leacox
 */
public class Main {
  public static void main(String[] args) throws Exception {
    // Add jars to this loader that should be shared between the runtime and the user runnables.
    ClassLoader commonLoader = new URLClassLoader(new URL[] {}, null);

    // For a real runtime it's probably best to create an assembly with a lib directory to load
    // all of the required jars from.
    ClassLoader runnerLoader = new URLClassLoader(new URL[] {
        new File(
            "simple-runtime/target/simple-runtime-1.0-SNAPSHOT.jar").toURI().toURL(),
        new File(
            "runtime-stubs-definitions/target/runtime-stubs-definitions-1.0-SNAPSHOT.jar")
            .toURI().toURL()
    }, commonLoader);

    try {
      Thread.currentThread().setContextClassLoader(runnerLoader);

      Class<?> runtimeClass = runnerLoader.loadClass("com.leacox.sandbox.runtime.simple.SimpleRuntime");
      Object runtimeInstance = runtimeClass.newInstance();

      String startMethodName = "start";
      Method startMethod = runtimeClass.getMethod(startMethodName, ClassLoader.class);
      startMethod.invoke(runtimeInstance, commonLoader);

      Map<String, String> runners = new HashMap<>();
      runners.put(
          "com.leacox.sandbox.runtime.simple.AllowedRunner",
          "simple-runtime-examples/target/simple-runtime-examples-1.0-SNAPSHOT.jar");
      runners.put(
          "com.leacox.sandbox.runtime.simple.DeniedRunner",
          "simple-runtime-examples/target/simple-runtime-examples-1.0-SNAPSHOT.jar");

      // These jars attempt to avoid being killed.
      //runners.put(
      //    "com.leacox.sandbox.runtime.simple.NeverEndingRunner",
      //    "simple-runtime-examples/target/simple-runtime-examples-1.0-SNAPSHOT.jar");
      //runners.put(
      //    "com.leacox.sandbox.runtime.simple.IterativeNeverEndingRunner",
      //    "simple-runtime-examples/target/simple-runtime-examples-1.0-SNAPSHOT.jar");

      String runMethodName = "run";
      Method runMethod = runtimeClass.getMethod(runMethodName, Map.class);
      runMethod.invoke(runtimeInstance, runners);
    } catch (Exception e) {
      System.out.println(e);
      throw new RuntimeException(e);
    }
  }
}
