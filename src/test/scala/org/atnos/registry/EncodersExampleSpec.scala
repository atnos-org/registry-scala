package org.atnos.registry

import org.specs2.Specification
import org.atnos.registry._
import Json._
import Encoders._

class EncodersExampleSpec extends Specification { def is = s2"""

 An encoding for a deeply nested encoder can be replaced $e1

"""

  def e1 = {
    val registry =
        nameEncoder +:
        ageEncoder +:
        employeeEncoder +:
        departmentEncoder +:
        companyEncoder +:
        rend

    val nameEncoder1       = registry.make[Encoder[Name]]
    val ageEncoder1        = registry.make[Encoder[Age]]
    val employeeEncoder1   = registry.make[Encoder[Employee]]
    val departmentEncoder1 = registry.make[Encoder[Department]]
    val companyEncoder1    = registry.make[Encoder[Company]]

    val nameCapitalizedEncoder = new Encoder[Name] {
      def encode(name: Name): Json =
        nameEncoder.encode(Name(name.value.toUpperCase))
    }

    val registry1 = nameCapitalizedEncoder +: registry

    val companyEncoder2 = registry1.make[Encoder[Company]]
    val company = Company(List(Department(List(Employee(Name("eric"), Age(45))))))


    companyEncoder2.encode(company) ====
      JsonObject(Map("departments" ->
        JsonArray(List(
          JsonObject(Map("employees" ->
            JsonArray(List(JsonObject(Map(
              "n" -> JsonString("ERIC"),
              "a" -> JsonNumber(45)))))))))))
  }

}
