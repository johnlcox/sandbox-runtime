package com.leacox.sandbox.runtime.simple;

import static net.bytebuddy.matcher.ElementMatchers.none;

import com.leacox.sandbox.runtime.security.transform.BootstrapTransformationContext;
import com.leacox.sandbox.runtime.security.transform.BootstrapTransformationDefinition;

import com.leacox.sandbox.security.RuntimePolicy;
import com.leacox.sandbox.security.RuntimeSecurityManager;
import com.leacox.sandbox.security.UserClassLoader;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.pool.TypePool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.security.Policy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.jar.JarFile;

/**
 * A simple sandboxed runtime environment.
 *
 * @author John Leacox
 */
public class SimpleRuntime {
  private static final Logger logger = LoggerFactory.getLogger(SimpleRuntime.class);

  /**
   * A class loader that is shared between both the runtime and user environment. This class loader
   * includes everything on the bootstrap and system class path in addition to any shared jars.
   */
  private ClassLoader commonLoader;
  private boolean isStarted = false;

  public void start(ClassLoader commonLoader) {
    this.commonLoader = commonLoader;

    Policy.setPolicy(new RuntimePolicy());
    System.setSecurityManager(new RuntimeSecurityManager());

    instrument();
  }

  private void instrument() {
    Instrumentation instrumentation = ByteBuddyAgent.install();

    File file = new File("runtime-stubs/target/runtime-stubs-1.0-SNAPSHOT.jar");
    String path = file.getAbsolutePath();
    if (!file.exists()) {
      throw new RuntimeException("Unable to find stubs jar");
    }

    JarFile jarFile;
    try {
      jarFile = new JarFile(file);
      instrumentation.appendToBootstrapClassLoaderSearch(jarFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    SimpleByteBuddyListener listener = new SimpleByteBuddyListener();

    AgentBuilder agentBuilder = new AgentBuilder.Default()
        .disableClassFormatChanges()
        .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
        .with(listener)
        .ignore(none());

    TypePool bootstrapTypePool = TypePool.Default.of(new ClassFileLocator.Compound(
        new ClassFileLocator.ForJarFile(jarFile),
        ClassFileLocator.ForClassLoader.of(null)));
    BootstrapTransformationContext context = new BootstrapTransformationContext(bootstrapTypePool);
    for (BootstrapTransformationDefinition definition : ServiceLoader
        .load(BootstrapTransformationDefinition.class)) {
      agentBuilder = agentBuilder
          .type(definition.getRawMatcher(context))
          .transform(definition.getTransformer(context));
    }

    agentBuilder.installOnByteBuddyAgent();

    if (listener.hadErrorDuringInstrumentation()) {
      throw new RuntimeException("Some transformations failed; not continuing");
    }

    isStarted = true;
  }

  /**
   * Run the supplied runners.
   */
  public void run(Map<String, String> runnerJarsByMainClass) {
    if (!isStarted) {
      throw new RuntimeException("Cannot run user jars until the runtime has been started.");
    }

    List<Thread> threads = new ArrayList<>();
    for (Map.Entry<String, String> entry : runnerJarsByMainClass.entrySet()) {
      String runnerClassName = entry.getKey();
      String jarPath = entry.getValue();
      File jarFile = new File(jarPath);
      if (!jarFile.exists()) {
        logger.error("Unable to find runner jar file: {}", jarPath);
      }

      try {
        ClassLoader userLoader = new UserClassLoader(jarFile.toURI().toURL(), commonLoader);
        Class<? extends Runnable> runnerClass =
            (Class<? extends Runnable>) userLoader.loadClass(runnerClassName);
        Runnable runner = runnerClass.newInstance();
        Runnable runnerRunnable = new Runnable() {
          @Override
          public void run() {
            Thread.currentThread().setContextClassLoader(userLoader);
            runner.run();
          }
        };

        Thread thread = new Thread(runnerRunnable);
        threads.add(thread);
        thread.start();
      } catch (Exception e) {
        logger.error("Error creating user runner", e);
      }
    }

    // Give runners 20 seconds to finish
    for (int i = 0; i < 5; i++) {
      List<Thread> finishedThreads = new ArrayList<>();
      for (Thread thread : threads) {
        if (thread.getState().equals(Thread.State.TERMINATED)) {
          finishedThreads.add(thread);
        }
      }
      threads.removeAll(finishedThreads);
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    // Kill any remaining runners. Stop is deprecated, but this is the only way to force stop a
    // thread. Since each runner has it's own classloader there shouldn't be any shared state
    // between different runners, except if there is some sort of static state on standard JVM
    // classes of which a preliminary glance through the OpenJDK source code does not show too many
    // of. For instance, registering shutdown hooks uses a static map, but the runners don't have
    // permission for either creating a thread or registering shutdown hooks, both of which would
    // be required, so that state will never be modified by a runner.
    for (Thread thread : threads) {
      int i = 0;
      while (thread.isAlive()) {
        thread.stop();
        if (thread.isAlive() && i % 1000 == 0) {
          System.out.println("thread is still alive after " + i + " attempts.");
        }
        i++;
      }
    }

    System.out.println("end");
  }
}
