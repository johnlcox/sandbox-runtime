# sandbox-runtime

An implementation of a sandboxed environment for running untrusted code with limited security permissions. Additionally, it shows how custom permissions can be implemented that provided finer grained security that the standard permissions available in Java.

Credit for many of the ideas for running sandboxed code goes to Will Sargent's [Sandbox Experiment in Scala](https://github.com/wsargent/sandboxexperiment).

## Building and Running

To build the project run:

```
mvn package
```

To execute the examples run:

```
java -jar simple-runtime-bootstrap/target/simple-runtime-bootstrap-1.0-SNAPSHOT.jar

```

## The Sandbox

The sandbox works by using the Java `SecurityManager` along with a custom `ClassLoader` and `Policy`. The policy determines the permissions of the running code by the associated code source or class loader for each class. One caveat to this is that code that is to run with limited permissions must be loaded from a separate jar at runtime.

The permissions for sandboxed code is determined by the `RuntimePolicy` class and the `UserClassLoader`. The `UserClassLoader` explains how dynamic permissions could be given to different sandboxed jars.


## Fine-grained Permissions

One of the problems with using the `SecurityManager` is that some of the permissions provided by Java are somewhat broader than you would want. For instance, many libraries require reflection, but there are only 2 permissions around reflection.

 * `ReflectPermission("suppressAccessChecks")`
 * `ReflectPermission("newProxyInPackage.{package name}")`


 Enabling the `suppressAccessChecks` for sandboxed code effectively defeats the security provided by the sandbox. The sandboxed code can use this permission to access private fields and methods on all code, including Java classes and your own runtime environment classes, and disable the security manager or access methods that bypass normal security checks. By leaving this permission disabled many widely used libraries will not function in the sandbox environment.

 It would be nice to be able to selectively enable the `suppressAccessChecks` permission; to allow it as long as the object that is being accessed (callee) and the object performing the access (caller) are from the same code source. In other words, allow the sandboxed code to perform reflections on itself, but not on the Java classes or classes of your own runtime environment.

 This is possible to accomplish using [byte-buddy](https://github.com/raphw/byte-buddy) to replace the methods of Java classes that perform the security check with a custom implementation and security permission.

 For example, the `suppressAccessChecks` permission is checked by the `AccessibleObject` class in the `setAccessible` method. By replacing this method we can check the class loader instance of the callee and the caller, and if they match perform a security check using a custom permission `UserSetAccessiblePermission`. If the callee and caller do not match, then instead if checks the standard `suppressAccessChecks` permission.


## License

    Copyright 2016 John Leacox

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.