# The Registry

#### The resolution algorithm

Let's imagine for a moment that you have a stack of functions to create "output values". You also put on your stack
 some "input" values. If you want a value of a given type you can:

 1. go through the list of existing values and if you find one with the desired type return it
 1. otherwise try to find a function returning a value of the given type
 1. if you find such a function apply the same algorithm to build all its input values
 1. every newly built value is put on top of the stack so it is available as an input to another function

You can eventually create a value out of the Registry if:

 - the value type is one of the existing values types
 - or if its type is the output type of one of the functions
 - the function inputs types are also existing value types or output types of other functions
 - there are no cycles!

#### A small example

Let's use a `Registry` to deal with the "encoders" example given in the [motivation](./motivation.md) section. We need
 first to introduce the type of encoders, `Encoder`:

```
trait Encoder[A] {
  def encode(a: A): Json
}
```

Then we can define a list of encoders and encoder functions:

```
val nameEncoder = new Encoder[Name] { def encode(n: Name): Json = string(n.value) }
val ageEncoder = new Encoder[Age] { def encode(a: Age): Json = number(a.value) }

val employeeEncoder: (Encoder[Name], Encoder[Age]) => Encoder[Employee] =
  (nameE: Encoder[Name], ageE: Encoder[Age]) => new Encoder[Employee] {
    def encode(employee: Employee): Json =
      obj("n" := nameE.encode(employee.name),
          "a" := ageE.encode(employee.age))
  }

val departmentEncoder: (Encoder[Employee]) =>  Encoder[Department] =
  (employeeE: Encoder[Employee]) => new Encoder[Department] {
    def encode(department: Department): Json =
      obj("employees" := arr(department.employees.map(employeeE.encode)))
  }

val companyEncoder: (Encoder[Department]) => Encoder[Company] = (departmentE: Encoder[Department]) => new Encoder[Company] {
  def encode(company: Company): Json =
    obj("departments" := arr(company.departments.map(departmentE.encode)))
}
```

We can already see something interesting. The right levels of abstraction are respected because the `departmentEncoder` 
doesn't have to know how the `employeeEncoder` is implemented for example.

Now we put everything in a `Registry`
```
import org.atnos.registry

val registry =
     nameEncoder +:
     ageEncoder +:
     employeeEncoder +:
     departmentEncoder +:
     companyEncoder +:
     end
```

In the code above `end` is the "empty" registry and `+:` adds a new element to the registry. 

With a `registry` we can ask to make any encoder
```
val nameEncoder1    = registry.make[Encoder[Name]]
val companyEncoder1 = registry.make[Encoder[Company]]
```

Can we produce an `Encoder[Company]` where all the names will be capitalized? Yes, by adding another `Encoder[Name]` on 
top of the existing one in the registry:
```

val nameCapitalizedEncoder = new Encoder[Name] {
  def encode(name: Name): Json = 
    nameEncoder.encode(Name(name.value.toUpperCase))
}

val registry1 = nameCapitalizedEncoder +: registry

val companyEncoder2 = registry1.make[Encoder[Company]]
```

Since the resolution algorithm looks for values "top to bottom" on the registry stack it will find `nameCapitalizedEncoder`
 to be used when building other encoders.

That's all it takes! Now you can have a look at the main reason for this library to exist: how to build [applications](./applications.md).