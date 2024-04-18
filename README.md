# Staged Builder Build Plugin in Java

This repository contains an Annotation Processor (similiar to Lombok) that generates Staged Builders for annotated classes at compile time.

## What is a staged builder?

A "Staged Builder" expands on the concept of builders in Java.  
Traditional builders provide convenience when refactoring constructors and prevent you from accidentally putting in
constructor arguments in the wrong order. They are also a form of self documenting code.  
They come at the great cost of reduced compile time safety, as unlike with constructors adding new required fields will
not produce compile errors, so any path missed in unit testing is prone to subtle introduction of errors.

Generated Staged Builders provide additional compile time safety compared to traditional builders and even constructors
while retaining the advantages of builders, by only letting the programmer compile a call the `build()` Method once all methods
to fill required fields have been called.

## Example

Normal case:
```java
ExampleUser user = ExampleUserBuilder.create()
        .username("Tom")
        .age(30)
        .password("$ Some-Bcrypt-Hash")
        .build();
```

Missing call to `.age()`:
```java
ExampleUser user = ExampleUserBuilder.create()
        .username("Tom")
        .password("$ Some-Bcrypt-Hash")
        .build();
```
```
[ERROR] builder-tester/src/main/java/com/zainlessbrombie/Main.java:[33,9] cannot find symbol
[ERROR]   symbol:   method password(java.lang.String)
[ERROR]   location: interface com.zainlessbrombie.user.ExampleUserBuilder.S.AgeStage
```

## Supported Features

#### Fields can be **entirely ignored** by the builder:

```java
import com.zainlessbrombie.stagedbuilder.BuilderIgnored;

@BuilderIgnored
private String cachedBinaryRepresentation;
```

#### Fields can be marked **optional**, either by using `@BuilderOptional` or by using any annotation called `Nullable`:

```java
import com.zainlessbrombie.stagedbuilder.BuilderOptional;
import javax.annotation.Nullable;

@BuilderOptional
private String deletionReason;

@Nullable
private Address billingAddress;
```

#### The way fields of the built object are filled can be specified:

```java

@StagedBuilder(fieldAccessMethod = BuilderFieldAccessMethod.SETTER)
public class MyClass {}
```
The following methods are available:
* SETTER
  * (Default) assumes that setter methods are available and uses those
* DIRECT_WRITE
  * assumes that fields are public and accesses them directly
* REFLECTION
  * Manually set fields using reflection, make accessible if necessary

#### Validation method can be specified
If a validation method is specified, it will be called by the `build()` method. The builder will assume that the method
is public.
```java

@StagedBuilder(validator = "validate")
public class MyClass {
    public void validate() {}
}
```

## Disadvantages

There are a few disadvantages to using this method:
* Inner classes are not supported yet
* In order for the IDE to pick up the generated source code, the compile needs to have been executed at least once, like with `mvn package`.
* Renaming the class name or package name of a class that has a builder confuses IntelliJ as it does not notice that
  files have changed and possibly confuses other IDEs too.
* The generated source code is actual, line-by-line generated Java Code, not bytecode like in the case of lombok. Switching
  to bytecode is a logical next step for this processor and would be beneficial and could also improve IDE integration.
* Each non-optional stage (field) in the builder has its own `interface`. The first time the builder is used, these interface 
  classes need to be instantiated by the JVM. This generally does not take very long but may introduce a millisecond delay.
  They also take up a bit of memory. From personal experience these delays are insignificant in comparison to other initialization
  delays typically encountered in complex java applications.
* Builders, staged or not, are slower _compared to normal constructor calls_, because each field being filled requires an
  `invokevirtual` method call. This is insignificant compared to the computation required tasks like for HTTP
  method processing, routing and database calls. However, using builders in performance heavy, CPU-Bound, high-iteration-count
  loops is not advised.


## Usage

The processor could simply be added as a maven dependency:
```xml
<dependency>
    <groupId>com.zainlessbrombie</groupId>
    <artifactId>staged-builder</artifactId>
    <version>0.4.1</version>
</dependency>
```

...however, the artifact is not currently hosted anywhere. You either need to host it yourself or provide some other
way for the project to load the dependency.

Lastly, I built this processor to show that this concept is viable and this library is used in a production project.  
If you want to use it now, fork the repo and host the artifact yourself. If you feel that this processor is useful, let me
know. If there is some interest I will likely refine the project to a production-ready state.

