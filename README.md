# registry-scala

## Presentation

This library provides a data structure, a Registry, to control the creation of functions from other functions. 
You can use this technique to:

 - create applications out of software components ("dependency injection")
 - fine tune JSON encoders/decoders
 - create composable data generators for nested datatypes

You can watch a video presenting the main ideas behind the library [here](https://skillsmatter.com/skillscasts/12299-wire-once-rewire-twice).

The following sections introduce in more details the problem that this library is addressing, the concepts behind the solution and various use-cases which can arise on real projects:

 1. [what is the problem](doc/motivation.md)?
 1. **TODO** the concept of a Registry and the resolution algorithm
 1. **TODO** how does this compare to monad and effects?
 
## Tutorials

 1. **TODO** use a Registry to create applications and define components
 2. **TODO** use a Registry to compose Hedgehog generators
 
## How-tos

 1. **TODO** how to install this library?
 1. **TODO** how to do mocking?
 1. **TODO** how to specialize some values in some contexts?
 1. **TODO** how to make a singleton for a database?
 1. **TODO** how to allocate resources which must be finalized?
 1. **TODO** how to initialize components in an application?
 1. **TODO** how to extract a dot graph from the registry in an application?
 1. **TODO** how to interact with a library using monad transformers?

## Reference guides

 1. **TODO** main operators and functions
 2. **TODO** implementation notes
