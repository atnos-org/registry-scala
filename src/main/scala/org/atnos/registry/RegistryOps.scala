package org.atnos.registry
import scala.reflect.runtime.universe._


trait RegistryImplicits extends RegistryLowImplicits with Constraints with HLists {

  implicit class RegistryOps1[Ins <: HList, Out <: HList](r: Registry[Ins, Out]) {
    def +:[A1, R](f: A1 => R)(implicit tag: TypeTag[A1 => R]): Registry[A1 :: Ins, R :: Out] =
      addFunction1(r)(f)

    def <+>[Ins1 <: HList, Out1 <: HList, AddedIns <: HList, AddedOut <: HList](other: Registry[Ins1, Out1])(implicit addIns: Add[Ins1, Ins, AddedIns], addOut: Add[Out1, Out, AddedOut]):
      Registry[AddedIns, AddedOut] =
      Registry(r.values ++ other.values, r.functions ++ other.functions)
  }

  implicit class RegistryOps2[Ins <: HList, Out <: HList](r: Registry[Ins, Out]) {
    def +:[A1, A2, R](f: (A1, A2) => R)(implicit tag: TypeTag[(A1, A2) => R]): Registry[A1 :: A2 :: Ins, R :: Out] =
      addFunction2(r)(f)
  }

  implicit class RegistryOps3[Ins <: HList, Out <: HList](r: Registry[Ins, Out]) {
    def +:[A1, A2, A3, R](f: (A1, A2, A3) => R)(implicit tag: TypeTag[(A1, A2, A3) => R]): Registry[A1 :: A2 :: A3 :: Ins, R :: Out] =
      addFunction3(r)(f)
  }

  implicit class RegistryOps4[Ins <: HList, Out <: HList](r: Registry[Ins, Out]) {
    def +:[A1, A2, A3, A4, R](f: (A1, A2, A3, A4) => R)(implicit tag: TypeTag[(A1, A2, A3, A4) => R]): Registry[A1 :: A2 :: A3 :: A4 :: Ins, R :: Out] =
      addFunction4(r)(f)
  }

  implicit class RegistryOps5[Ins <: HList, Out <: HList](r: Registry[Ins, Out]) {
    def +:[A1, A2, A3, A4, A5, R](f: (A1, A2, A3, A4, A5) => R)(implicit tag: TypeTag[(A1, A2, A3, A4, A5) => R]): Registry[A1 :: A2 :: A3 :: A4 :: A5 :: Ins, R :: Out] =
      addFunction5(r)(f)
  }

  implicit class RegistryOps6[Ins <: HList, Out <: HList](r: Registry[Ins, Out]) {
    def +:[A1, A2, A3, A4, A5, A6, R](f: (A1, A2, A3, A4, A5, A6) => R)(implicit tag: TypeTag[(A1, A2, A3, A4, A5, A6) => R]): Registry[A1 :: A2 :: A3 :: A4 :: A5 :: A6 :: Ins, R :: Out] =
      addFunction6(r)(f)
  }

  implicit class RegistryOps7[Ins <: HList, Out <: HList](r: Registry[Ins, Out]) {
    def +:[A1, A2, A3, A4, A5, A6, A7, R](f: (A1, A2, A3, A4, A5, A6, A7) => R)(implicit tag: TypeTag[(A1, A2, A3, A4, A5, A6, A7) => R]): Registry[A1 :: A2 :: A3 :: A4 :: A5 :: A6 :: A7 :: Ins, R :: Out] =
      addFunction7(r)(f)
  }

  implicit class RegistryOps8[Ins <: HList, Out <: HList](r: Registry[Ins, Out]) {
    def +:[A1, A2, A3, A4, A5, A6, A7, A8, R](f: (A1, A2, A3, A4, A5, A6, A7, A8) => R)(implicit tag: TypeTag[(A1, A2, A3, A4, A5, A6, A7, A8) => R]): Registry[A1 :: A2 :: A3 :: A4 :: A5 :: A6 :: A7 :: A8 :: Ins, R :: Out] =
      addFunction8(r)(f)
  }

  implicit class RegistryOps9[Ins <: HList, Out <: HList](r: Registry[Ins, Out]) {
    def +:[A1, A2, A3, A4, A5, A6, A7, A8, A9, R](f: (A1, A2, A3, A4, A5, A6, A7, A8, A9) => R)(implicit tag: TypeTag[(A1, A2, A3, A4, A5, A6, A7, A8, A9) => R]): Registry[A1 :: A2 :: A3 :: A4 :: A5 :: A6 :: A7 :: A8 :: A9 :: Ins, R :: Out] =
      addFunction9(r)(f)
  }

}

trait RegistryLowImplicits extends RegistryOps {


  implicit class RegistryOps0[Ins <: HList, Out <: HList](r: Registry[Ins, Out]) {
    def +:[A : TypeTag](a: A): Registry[Ins, A :: Out] =
      addValue(r)(a)
  }


}

trait RegistryOps {

  def addValue[Ins <: HList, Out <: HList, A](r: Registry[Ins, Out])(a: A)(implicit tag: TypeTag[A]): Registry[Ins, A :: Out] =
    r.copy(values = (a, tag) +: r.values)

  def addFunction1[Ins <: HList, Out <: HList, A1, R](r: Registry[Ins, Out])(f: A1 => R)(implicit tag: TypeTag[A1 => R]): Registry[A1 :: Ins, R :: Out] =
    r.copy(functions = (f, tag) +: r.functions)

  def addFunction2[Ins <: HList, Out <: HList, A1, A2, R](r: Registry[Ins, Out])(f: (A1, A2) => R)(implicit tag: TypeTag[(A1, A2) => R]): Registry[A1 :: A2 :: Ins, R :: Out] =
    r.copy(functions = (f, tag) +: r.functions)

  def addFunction3[Ins <: HList, Out <: HList, A1, A2, A3, R](r: Registry[Ins, Out])(f: (A1, A2, A3) => R)(implicit tag: TypeTag[(A1, A2, A3) => R]): Registry[A1 :: A2 :: A3 :: Ins, R :: Out] =
    r.copy(functions = (f, tag) +: r.functions)

  def addFunction4[Ins <: HList, Out <: HList, A1, A2, A3, A4, R](r: Registry[Ins, Out])(f: (A1, A2, A3, A4) => R)(implicit tag: TypeTag[(A1, A2, A3, A4) => R]): Registry[A1 :: A2 :: A3 :: A4 :: Ins, R :: Out] =
    r.copy(functions = (f, tag) +: r.functions)

  def addFunction5[Ins <: HList, Out <: HList, A1, A2, A3, A4, A5, R](r: Registry[Ins, Out])(f: (A1, A2, A3, A4, A5) => R)(implicit tag: TypeTag[(A1, A2, A3, A4, A5) => R]): Registry[A1 :: A2 :: A3 :: A4 :: A5 :: Ins, R :: Out] =
    r.copy(functions = (f, tag) +: r.functions)

  def addFunction6[Ins <: HList, Out <: HList, A1, A2, A3, A4, A5, A6, R](r: Registry[Ins, Out])(f: (A1, A2, A3, A4, A5, A6) => R)(implicit tag: TypeTag[(A1, A2, A3, A4, A5, A6) => R]): Registry[A1 :: A2 :: A3 :: A4 :: A5 :: A6 :: Ins, R :: Out] =
    r.copy(functions = (f, tag) +: r.functions)

  def addFunction7[Ins <: HList, Out <: HList, A1, A2, A3, A4, A5, A6, A7, R](r: Registry[Ins, Out])(f: (A1, A2, A3, A4, A5, A6, A7) => R)(implicit tag: TypeTag[(A1, A2, A3, A4, A5, A6, A7) => R]): Registry[A1 :: A2 :: A3 :: A4 :: A5 :: A6 :: A7 :: Ins, R :: Out] =
    r.copy(functions = (f, tag) +: r.functions)

  def addFunction8[Ins <: HList, Out <: HList, A1, A2, A3, A4, A5, A6, A7, A8, R](r: Registry[Ins, Out])(f: (A1, A2, A3, A4, A5, A6, A7, A8) => R)(implicit tag: TypeTag[(A1, A2, A3, A4, A5, A6, A7, A8) => R]): Registry[A1 :: A2 :: A3 :: A4 :: A5 :: A6 :: A7 :: A8 :: Ins, R :: Out] =
    r.copy(functions = (f, tag) +: r.functions)

  def addFunction9[Ins <: HList, Out <: HList, A1, A2, A3, A4, A5, A6, A7, A8, A9, R](r: Registry[Ins, Out])(f: (A1, A2, A3, A4, A5, A6, A7, A8, A9) => R)(implicit tag: TypeTag[(A1, A2, A3, A4, A5, A6, A7, A8, A9) => R]): Registry[A1 :: A2 :: A3 :: A4 :: A5 :: A6 :: A7 :: A8 :: A9 :: Ins, R :: Out] =
    r.copy(functions = (f, tag) +: r.functions)

}


