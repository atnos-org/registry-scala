package org.atnos.registry

import Json._

case class Company(departments: List[Department])
case class Department(employees: List[Employee])
case class Employee(name: Name, age: Age)

case class Name(value: String)
case class Age(value: Int)

object Encoders {

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

  val companyEncoder: (Encoder[Department]) => Encoder[Company] =
    (departmentE: Encoder[Department]) => new Encoder[Company] {
       def encode(company: Company): Json =
         obj("departments" := arr(company.departments.map(departmentE.encode)))
    }
}


