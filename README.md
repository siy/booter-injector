[![Build Status](https://travis-ci.org/siy/booter-injector.svg?branch=master)](https://travis-ci.org/siy/booter-injector)

# booter-injector
Tiny and fast dependency injector for Java 8 and up.

## Overview

Booter-injector is a yet another dependency injection container (framework, whatewer). 
It's small (less than 50KB), fast (comparable to Java **new** call) and haze no external dependencies.

## Motivation
The project aims three main goals:
1. Make dependency injection fast
2. Minimal and convenient configuration
2. Facilitate best practices - constructor injection and keep configuration close to configured class.

First goal is achieved by adopting method handles and run-time generated lambdas and lazy evaluation. 
Second goal is achieved by building configuration at run and automatically detecting proper constructor in most cases.
  

## Getting Started
### Adding dependency to Maven project
(NOTE: library is not yet included into public Maven repos)

~~~~
    <dependency>
      <groupId>io.booter</groupId>
      <artifactId>injector</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>
~~~~

## Basic example

