[![Build Status](https://travis-ci.org/siy/booter-injector.svg?branch=master)](https://travis-ci.org/siy/booter-injector)
# booter-injector
Fast dependency injection library.

## Overview
This library is an opinionated, tiny and extremely fast DI container.

### Opinionated

- Minimalistic configuration.
- Supports only constructor injection (no field injection)
- Zero external dependency (everything is built-in)

### Tiny

Packaged jar file is less than 50kB.

### Extremely fast

Run-time speed is crucial, especially if injector is used to create many instances at run time. 
For most modern DI containers this is an anti-pattern. Booter-injector performance is comparable 
to plan new call, so DI container can be used even in such a patterns.
 