package org.atnos.registry

import org.specs2._
import Registry._
import HLists._
import scala.reflect.runtime.universe._

class RegistrySpec extends Specification with ScalaCheck { def is = s2"""

 make a value without function call
   for an Int   $register1
   for a String $register2
                                                                       
 make a value without a function call
   with one argument  $register3
   with two arguments $register4

"""

  def register1 = {
    val registry = 1 +: C1 +: C2 +: *:
    val a = registry.make[Int]
    a ==== 1
  }

  def register2 = {
    val registry = 1 +: "hey" +: C1 +: C2 +: *:
    val a = registry.make[String]
    a ==== "hey"
  }

  def register3 = {
    val registry = 1 +: C1 +: C2 +: *:
    val a = registry.make[C1]
    a ==== C1(1)
  }

  def register4 = {
    val registry = 1 +: "hey" +: C1 +: C2 +: *:
    val a = registry.make[C2]
    a ==== C2("hey", 1)
  }

}

case class C1(n: Int)
case class C2(s: String, n: Int)


object Examples {
  val registry =
     1 +:
     C1 +:
     2 +:
     end
}

import cats.implicits._


case class Registry[+Ins <: HList, +Out <: HList](values: List[(Any, TypeTag[_])], functions: List[(Any, TypeTag[_])]) {
  def addValue[A](a: A)(implicit tag: TypeTag[A]): Registry[Ins, A :: Out] =
    copy(values = (a, tag) +: values)

  def addFunction1[A, B](f: A => B)(implicit tag: TypeTag[A => B]): Registry[A :: Ins, B :: Out] =
    copy(functions = (f, tag) +: functions)

  def addFunction2[A, B, C](f: (A, B) => C)(implicit tag: TypeTag[(A, B) => C]): Registry[A :: B :: Ins, C :: Out] =
    copy(functions = (f, tag) +: functions)

  def make[A : TypeTag]: A =
    makeOption.get

  def makeOption[A](implicit tag: TypeTag[A]): Option[A] =
    makeValueFromType(tag.tpe).map(_.asInstanceOf[A])

  def makeValueFromType(tpe: Type): Option[Any] =
    findValue(tpe).orElse(constructValue(tpe))

  def findValue(tpe: Type): Option[Any] =
    values.collectFirst { case (a, t) if t.tpe.toString == tpe.toString =>
      a
    }

  def constructValue(tpe: Type): Option[Any] =
    findFunctionWithResult(tpe).flatMap { case (f, functionTag) =>
      findInputs(functionTag).map { inputs =>
        applyFunction(f, inputs)
      }
    }

  def findFunctionWithResult(tpe: Type): Option[(Any, TypeTag[_])] =
    functions.collectFirst { case (f, t) if t.tpe.typeArgs.last == tpe =>
      (f, t)
    }

  def findInputs(functionTag: TypeTag[_]): Option[List[Any]] = {
    val inputTypes = functionTag.tpe.typeArgs.dropRight(1)
    inputTypes.traverse(makeValueFromType)
  }

  def applyFunction(f: Any, inputs: List[Any]): Any = {
    val method = f.getClass.getDeclaredMethods.filter(_.getName == "apply").head
    method.invoke(f, inputs.asInstanceOf[Seq[Object]]:_*)
  }

}


object Registry extends RegistryLowOps {

  implicit class RegistryOps1[Ins <: HList, Out <: HList](r: Registry[Ins, Out]) {
    def +:[A, B](f: A => B)(implicit tag: TypeTag[A => B]): Registry[A :: Ins, B :: Out] =
      r.addFunction1(f)
  }

  implicit class RegistryOps2[Ins <: HList, Out <: HList](r: Registry[Ins, Out]) {
    def +:[A, B, C](f: (A, B) => C)(implicit tag: TypeTag[(A, B) => C]): Registry[A :: B :: Ins, C :: Out] =
      r.addFunction2(f)
  }

  def end: Registry[HNil.type, HNil.type] =
    Registry[HNil.type, HNil.type](Nil, Nil)

  def *: : Registry[HNil.type, HNil.type] =
    end

}

trait RegistryLowOps {


  implicit class RegistryOps0[Ins <: HList, Out <: HList](r: Registry[Ins, Out]) {
    def +:[A : TypeTag](a: A): Registry[Ins, A :: Out] =
      r.addValue(a)
  }


}

object HLists {

  sealed trait HList

  final case class ::[+H, +T <: HList](head : H, tail : T) extends HList

  implicit class HListOps[T <: HList](tail: T) {
    def ::[H](h : H) : H :: T = HLists.::(h, tail)
  }

  object HNil extends HList {
    def ::[H](h : H) = HLists.::(h, this)
  }
}
