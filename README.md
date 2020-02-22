# Lightweight Dependency Injection Container

This repository represents my solution to the programming challenge given by queo.

### Structure
``LWDIContainer`` provides the functionality required by the coding challenge. The implementation covered annotations for ``@Bean`` , that allow for bean definition, ``@Inject`` that allows for automatic dependency injection and ``@Named`` to manage beans under a custom name.

### Dependencies & Technology stack

This attempt at solving the challenge provides a framework that can be used for very basic dependency injection. Development was performed in a `test-driven` manner with the support of `junit`. [ClassGraph](https://github.com/classgraph/classgraph) was used for classpath scanning and parts of the reflection code that handles registration of the beans. [AspectJ](https://www.eclipse.org/aspectj/) was used to compile the sources with their custom annotations.


### Build & Run tests

Simply run `mvn clean install` from the root directory of the project to build the artifact. A jar file will be generated and placed in the `target` folder.