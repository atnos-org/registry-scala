# Motivation

#### Functions

What is the best software component?

> a function?

Yes indeed! A function is like a closed box with a nice label on top declaring exactly what it is doing. It is also a 
**sealed** box so the provider of the function can change the implementation without you having to change any of your code:

```
def square(n: Int): Int = 
  n * n -- who knew?
```

A higher-order function is even better. You can pass another function to alter the behavior of the first one:

```
def squareOkOrZero(check: Int => Bool, n: Int): Int =
  if (check(n)) square(n) else 0 
```

So functions are like boxes inside boxes inside boxes,... and the top-level box is totally sealed. This is fantastic 
for modularity because this hides low-level information.

#### The problem

This is also disastrous for reuse because we sometimes want to open the box, rearrange slightly the inside, and shut the 
box again. Really? Yes.

##### Encoders

For example we can use an `Encoder` type to describe how a `Company` can be serialized to `Json`:
```
case class Company(departments: List[Department])
case class Department(employees: List[Employee])
case class Employee(name: Name, age: Age)

case class Name(value: Text)
case class Age(value: Int)

-- this code uses a fictive `Json` library providing functions to create Json values
-- like `string`, `number`, `obj`, `arr`, `.=`

def nameEncoder(name: Name): Json
  string(name.value)

-- other signature are omitted
def ageEncoder(age: Age): Json
  number(name.value)

def employeeEncoder(employee: Employee): Json = 
  obj("n" .= nameEncoder(employee.name), 
      "a" .= ageEncoder(employee.age))
      
def departmentEncoder(department: Department): Json = 
  obj("employees" .= arr(department.employees.map(employeeEncoder))
  
def companyEncoder(company: Company): Json = 
  obj("departments" .= arr(company.departments.map(departmentEncoder))
```

Once given a `companyEncoder` you can encode any `Company`, great! However you are restricted to just one implementation. 
If you want to change some of the field names, for example use better fields names for the `employeeEncoder`, `name` and 
`age` instead of `n` and `a`, you need redefine *all* your encoders and "thread" a specific `employeeEncoder` from the top:
```
def employeeEncoder1(employee: Employee): Json =
  obj("name" .= nameEncoder(employee.age), 
      "age" .= ageEncoder(employee.age))

def departmentEncoder1(empEncoder: Employee => Json)(department: Department): Json =
  obj("employees" .= arr(department.employees.map(empEncoder))

def companyEncoder1(dptEncoder: Department => Json)(company: Company): Json =
  obj("departments" .= arr(company.departments.map(dptEncoder))
```

Then you can define
```
def myCompanyEncoder1(company: Company): Json =
  companyEncoder1(departmentEncoder1(employeeEncoder'))(company)
```
Which means that you need to manually assemble all the encoders you will need. There are of course other solutions to
 this issue, relying on type classes and/or macros. They have similar drawbacks, for example there can only be one encoder
for the `Employee` type (and using wrapper types to go around that restriction might be impossible if that data structure comes from a library).

This issue happens in other contexts, when using ScalaCheck generators for instance, but it is especially present when 
trying to structure applications as a set of "components", which is the main use case for this library.

##### Applications

When building medium to large applications it is very tempting to start grouping related functions together when they share 
the same implementation or the same configuration. For example when saving data to a database:
```

package acme.company.repository

import cats._
import acme.logging

trait CompanyRepository {
  def saveCompany(company: Company): IO[Unit]
  def getCompanies: IO(List[Company])
  def getCompanyById(companyId: String): IO[Option[Company]]
}

case class CompanyRepositoryConfig(host: String, port: Int)

case class CompanyRepositoryPostgres(connection: Connection, logging: Logging) implements CompanyRepository {
  // use the connection and the logging to implement those methods
  def saveCompany(company: Company): IO[Unit] = ???
  def getCompanies: IO(List[Company]) = ???
  def getCompanyById(companyId: String): IO[Option[Company]] = ???
}

object CompanyRepositoryPostgres {
  def newCompanyRepository(config: CompanyRepositoryConfig, logging: Logging): IO[CompanyRepository] =
    newConnection(config).map { connection =>
      CompanyRepositoryPostgres(connection, logging)
    } 
}

```

In the code above `newCompanyRepository` is a constructor for a `CompanyRepository` and uses some configuration and a `Logging` component.
 If you scale this approach to a full application you end up in the situation described for encoders where you need to
  manually call several functions to create the full structure. You will also need to parametrize those functions so that
   you can create different versions of the application for different environments: production, staging, development...

```
val logging = Logging.newLoggeing

val companyRepository =
   CompanyRepositoryPostgres.newCompanyRepository(
     CompanyRepositoryConfig("host", 5432),
     logging)

// more definitions...

// the full application
val app =
  App.newApp(
    logging,
    companyRepository,
    impageProcessing,
    s3Access,
    }
```

##### The solution

In summary there are advantages to manually assembling functions:

 - we don't need any fancy typelevel technique, just good old functions
 - we are as flexible as we want and can specify exactly which behaviour is needed where
 - unit testing is straightforward, just call a function directly with its arguments

But there are obvious drawbacks:

 - this code is tedious to write
 - it impedes refactoring because a simple change in the structure of your data model or application can trigger many changes

The solution? Abstract over the construction process in order to modify it to suit our needs. This library provides a 
simple data structure, a [Registry](./registry.md), and a resolution algorithm to encode the assembly of functions and modify it if necessary.