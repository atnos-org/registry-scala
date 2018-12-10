package org.atnos.registry

import org.specs2._
import Registry._
import HLists._
import scala.reflect.runtime.universe._

class RegistrySpec extends Specification with ScalaCheck { def is = s2"""

 register a value, a function and get the output $register1

"""

  def register1 = {
    val registry = 1  +: C1 +: C2 +: over
    val a = registry.pp.make[Int]
    a ==== 1
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

case class Function(fun: Any)


case class Registry[+Ins <: HList, +Out <: HList](values: List[(Any, TypeTag[_])], functions: List[(Any, TypeTag[_])]) {
  def addValue[A](a: A)(implicit tag: TypeTag[A]): Registry[Ins, A :: Out] =
    copy(values = (a, tag) +: values)

  def addFunction1[A, B](f: A => B)(implicit tag: TypeTag[A => B]): Registry[A :: Ins, B :: Out] =
    copy(functions = (f, tag) +: functions)

  def addFunction2[A, B, C](f: (A, B) => C)(implicit tag: TypeTag[(A, B) => C]): Registry[A :: B :: Ins, C :: Out] =
    copy(functions = (f, tag) +: functions)

  def make[A : TypeTag]: A =
    makeOption.get

  def makeOption[A : TypeTag]: Option[A] =
    findValue[A]

  def findValue[A](implicit tag: TypeTag[A]): Option[A] =
    values.collectFirst { case (a, t) if t == tag => a.asInstanceOf[A] }

  def findFunctionWithResult[A](implicit tag: TypeTag[A]): Option[Any] =
    functions.collectFirst { case (a, t) if t.tpe.resultType == tag =>
      a
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

  def over: Registry[HNil.type, HNil.type] =
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
