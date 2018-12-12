package org.atnos.registry

import org.specs2._
import Registry._
import cats.effect._
import Lift._

class RegistrySpec extends Specification with ScalaCheck { def is = s2"""

Basic resolution algorithm
==========================

make a value without function call
  for an Int   $register1
  for a String $register2

make a value with a function call
  with one argument  $register3
  with two arguments $register4

A value can be mocked by adding the mock on top of the registry
  mock a value $mockValue


Effectful components
====================

Constructors can be lifted to a given monad $lift1


"""

  def register1 = {
    val registry = 1 +: C1 +: C2 +: rend
    val a = registry.make[Int]
    a ==== 1
  }

  def register2 = {
    val registry = 1 +: "hey" +: C1 +: C2 +: rend
    val a = registry.make[String]
    a ==== "hey"
  }

  def register3 = {
    val registry = 1 +: C1 +: C2 +: rend
    val a = registry.make[C1]
    a ==== C1(1)
  }

  def register4 = {
    val registry = 1 +: "hey" +: C1 +: C2 +: rend
    val a = registry.make[C2]
    a ==== C2("hey", 1)
  }

  def mockValue = {
    val registry = "ho" +: 2 +: 1 +: "hey" +: C1 +: C2 +: rend
    val a = registry.make[C2]
    a ==== C2("ho", 2) // instead of C2("hey", 1)
  }

  def lift1 = {
    val registry =
      DatabaseConfig("host", 5432).to[IO] +:
      Database.newDatabase[IO].liftArgs +:
      ConsoleLogger.newConsoleLogger[IO].to[IO] +:
      rend

    val database = registry.make[IO[Database[IO]]]
    database.unsafeRunSync must beLike { case PostgresDatabase(_, _) => ok }
  }

}

case class C1(n: Int)
case class C2(s: String, n: Int)



