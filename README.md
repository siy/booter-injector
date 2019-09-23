[![Build Status](https://travis-ci.org/siy/booter-injector.svg?branch=master)](https://travis-ci.org/siy/booter-injector)
[![](https://jitpack.io/v/siy/booter-injector.svg)](https://jitpack.io/#siy/booter-injector)

# booter-injector
Tiny and fast dependency injector for Java 8. Note that for Java 9+ it requires modifications.

**NOTE:** This implementation uses run-time reflection to perform tasks. Most existing dependency injection framewroks/libraries 
using this approach too. Nevertheless, in more recent versions of Java compile-time dependency injection 
would be better approach.

## Overview

Booter-injector is a yet another dependency injection container (framework, whatever). 
It's small (about 50KB), fast (comparable to Java **new** call) and has no external dependencies.

## License
This library is released under terms and conditions of Apache License, Version 2.0.
Copy of license is included in the sources or can be obtained at 
[Apache Software Foundation Site](http://www.apache.org/licenses/LICENSE-2.0). 

## Motivation
The project aims three main goals:
1. Make dependency injection really fast
2. Minimal and convenient configuration
2. Facilitate best practices - constructor injection and keep configuration close to configured class.

First goal is achieved by adopting method handles and run-time generated lambdas and lazy evaluation. 
Second goal is achieved by building configuration at run time, automatically detecting proper constructor in most 
cases and by favoring convention over configuration. Note that taken approach does not require package scanning
or anything like that.
Third goal is achieved by limiting injection support to constructor injection only and using **@ConfiguredBy** 
annotation.

## Getting Started
### Adding dependency to Maven project

Dependency itself:
~~~~
    <dependency>
        <groupId>org.rx-booter</groupId>
        <artifactId>injector</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
~~~~

Repository:
~~~~
    <repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
~~~~

## Simple example

Create injector:
~~~~
    Injector injector = Injector.create();
~~~~

Instantiate simple class:

~~~~
    Foo foo = injector.get(Foo.class);
~~~~

By default all classes are handled as **prototype** (in terms of Spring DI), i.e. for every **get()** request new 
instance is created. In order to make particular class a singleton it is enough to annotate class with **@Singleton** 
annotation:

~~~~
    @Singleton
    public class Foo {
    ...
    }
~~~~ 

Now for every invocation of **get()** same instance will be returned. By convention singletons created this way 
are lazily instantiated, i.e. the instance of the singleton is created only when first request for singleton is processed.
If particular singleton should be evaluated upfront (i.e. eagerly), it's enough to change annotation as follows:

~~~~
    @Singleton(ComputationStyle.EAGER)
    public class Foo {
    ...
    }
~~~~ 

## @ImplementedBy Annotation

In many cases interfaces have exactly one implementation. For such a cases binding between interface and implementation
can be configured by using **@ImplementedBy** annotation on interface:

Foo.java:
~~~~
    @ImplementedBy(FooImpl.class)
    public interface Foo {
    ...
    }
~~~~
FooImpl.java:
~~~~
    ...
    public class FooImpl implements Foo {
    ...
    }
~~~~

## Advanced Configuration with @ConfiguredBy Annotation

The **@ImplementedBy** annotation is useful in simple cases, but sometimes it's necessary to establish more complex 
bindings like ones for parametrized types and/or annotated types. Such a cases can be handled in two ways - using 
**@ConfiguredBy** annotation or by performing direct injector configuration. Latter approach is discussed below, here 
described use of **@ConfiguredBy** annotation.

In order to establish such a complex binding, add **@ConfiguredBy** annotation to **implementation** class. 
This annotation requires parameter - class where bindings are configured:

~~~~
    @ConfiguredBy(FooImplModule.class)
    public class FooImpl implements Foo {
    ...
    }
~~~~
 
Class passed to **@ConfiguredBy** as a parameter can be either POJO or implementation of **Module** interface.
For further explanation this class will be referred as **_configurator_**.
In case of POJO, all methods of class annotated with **@Supplies** annotation are used as factories for types matching 
method return types. Such a methods are referred as **_supplying methods_**. 
For example, following method in configurator class will produce **List<String>** instances:

~~~~
    public class FooImplModule {
    ...
        @Supplies
        List<String> getStringList() {
        ...
        }
    ...
    }
~~~~

If supplying method has parameters, they will be resolved and injected during method invocation 
just like regular constructor dependencies. Binding annotation attached to the method declaration will be attached 
to type returned by method and then will be used to satisfy dependencies which require annotated type. For example:

~~~~
    public class FooImplModule {
    ...
        @Supplies
        List<String> getStringList() {
        ...
        }
    ...
        @Supplies
        @MyAnnotation
        List<String> getAnnotatedStringList() {
        ...
        }
    ...
    }
    
...
    @ConfiguredBy(FooImplModule.class)
    public class FooImpl implements Foo {
    ...
        public FooImpl(List<String> stringList, @MyAnnotation List<String> annotatedStringList) {
        ...
        }
    ...
    }
~~~~

The **getStringList()** method will satisfy dependency for first (not annotated) parameter of **FooImpl** constructor, 
while **getAnnotatedStringList()** will match dependency for second (annotated) parameter.

**NOTE 1:** Configurator classes are instantiated via injector and their dependencies are resolved as for any other
class instantiated via injector. 

**NOTE 2:** The **@ConfiguredBy** annotation on configurator classes are ignored. 

**NOTE 3:** Annotations which are used to specify dependencies should be annotated with **@BindingAnnotation** 
annotation in order to be recognized by injector. Example of such an annotation declaration is provided below:

~~~~
@Retention(RetentionPolicy.RUNTIME)
@Target({PARAMETER, METHOD})
@Documented
@BindingAnnotation
public @interface MyAnnotation {
}
~~~~

**NOTE 4:** It is possible to declare singleton suppliers with supplying methods. To do so just annotate method with **@Singleton**
annotation. Note though, that this in this case it is impossible to declare eager singleton. Example of declaring 
singleton supplying methods:  

~~~~
    public class FooImplModule {
    ...
        @Supplies
        @Singleton
        List<String> getStringList() {
        ...
        }
    ...
    }
~~~~

Handling of configurator class implementing **Module** interface (or extending **AbstractModule** class) is very 
similar to handling of POJO configurator, but when all methods are collected, then **configure()** method 
is invoked. **AbstractModule** provides convenient boilerplate for such configurators.

For example, different types of bindings can be done using convenient fluent syntax:
~~~~
    public class FooImplModule extends AbstractModule {
        @Override
        protected void configure() {
            //Simple binding
            bind(Bar.class).to(BarImpl.class);
            
            //Binding to instance
            bind(Baz.class).toInstance(bazInstance.class);
            
            //Binding to supplier
            bind(Qux.class).toSupplier(() -> quxInstance);
            
            //Binding to singleton
            bind(Foobar.class).toSingleton(FoobarImpl.class);
        }
    }
~~~~ 

Singleton binding by default is performed as lazy (i.e. lazy singleton will be created). If eager singleton is 
necessary, there is also **toEagerSingleton()** method. Lazy singleton binding can be made explicit by using 
**toLazySingleton()** method. Obviously plain **toSingleton()** is just a synonym to **toLazySingleton()**.

Beside different types of binding, **AbstractModule** provides convenient way to bind different types of 
dependencies:

~~~~
    public class FooImplModule extends AbstractModule {
        @Override
        protected void configure() {
            //Bind annotated class
            bind(Bar.class).annotatedWith(FooBarAnnotation.class).to(FooBarImpl.class);

            //Bind List<String>
            bind(new TypeToken<List<String>>() {}).toInstance(stringListInstance);

            //Bind List<String> annotated
            bind(new TypeToken<List<String>>() {}).annotatedWith(FooBarAnnotation.class).toInstance(annotatedStringListInstance);
        }
    }
~~~~

## Manual Injector Configuration

Beside configuring injector via classes annotated with **@ConfiguredBy** annotation, it is possible to configure 
injector by directly submitting configuration classes to **Injector.configure()**.

## Acknowledgements

This project had a great inspiration from other dependency injection frameworks, but most notable one is, probably,
[Google Guice](https://github.com/google/guice). Also, I'd like to mention 
[di-benchmark](https://github.com/greenlaw110/di-benchmark) project, which, despite some issues, was extremely helpful 
for tuning **booter-injector** performance.
