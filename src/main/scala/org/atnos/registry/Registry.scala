package org.atnos.registry

import cats.implicits._
import Constraints._
import scala.reflect.runtime.universe._

/**
 * A Registry keeps track of a list of values and functions
 *
 * They can be used to build other values and one can ask the
 * Registry to create a new value for a given type.
 *
 * The algorithm goes this way:
 *
 *  1. find the first value of that type in the registry
 *  2. if not found find the first function returning that type of value
 *  3. try to recursively build the inputs for that function
 *  4. if the inputs are found, apply the function and store the newly created value on top of the registry
 *
 * This data structure can be used for:
 *
 *  - dependency injection, by storing components constructors, configuration values. Mocking is achieved by adding mocks
 *    to the registry so that they can be found first during the resolution algorithm above
 *
 *  - wiring ScalaCheck generators when they depend on each other
 *
 *  - wiring JSON Encoders / Decoders when they depend on each other
 *
 */
case class Registry[Ins <: HList, Out <: HList](values: List[(Any, TypeTag[_])], functions: List[(Any, TypeTag[_])]) {
  /** API */

  def make[A : TypeTag](implicit contains: Contains[A, Out], subset: Subset[Ins, Out]): A =
    // this make version is guaranteed to succeed with the implicit constraints above
    // unless there is a cycle in the registry. In that case an exception will be thrown sooner
    makeOption.get

  def makeFast[A : TypeTag](implicit contains: Contains[A, Out]): A =
    makeUnsafe

  def makeUnsafe[A : TypeTag]: A =
    makeOption match {
      case Some(a) => a
      case None => throw new Exception("could not make a value of type "+implicitly[TypeTag[A]]+" out of the registry\n"+this)
    }

  def makeOption[A](implicit tag: TypeTag[A]): Option[A] =
    makeValueFromType(tag.tpe).map(_.asInstanceOf[A])

  override def toString: String =
    values.map    { case (v, t) => s"$v: ${t.tpe}" }.mkString("\n", "\n", "\n")+
    functions.map { case (_, t) => s"${t.tpe}" }.mkString("\n", "\n", "\n")

  /** IMPLEMENTATION */

  private def makeValueFromType(tpe: Type): Option[Any] =
    findValue(tpe).orElse(constructValue(tpe))

  private def findValue(tpe: Type): Option[Any] =
    values.collectFirst { case (a, t) if t.tpe.toString == tpe.toString =>
      a
    }

  private def constructValue(tpe: Type): Option[Any] =
    findFunctionWithResult(tpe).flatMap { case (f, functionTag) =>
      findInputs(functionTag).map { inputs =>
        applyFunction(f, inputs)
      }
    }

  private def findFunctionWithResult(tpe: Type): Option[(Any, TypeTag[_])] =
    functions.collectFirst { case (f, t) if t.tpe.typeArgs.last == tpe =>
      (f, t)
    }

  private def findInputs(functionTag: TypeTag[_]): Option[List[Any]] = {
    val inputTypes = functionTag.tpe.typeArgs.dropRight(1)
    inputTypes.traverse(makeValueFromType)
  }

  private def applyFunction(f: Any, inputs: List[Any]): Any = {
    val method = f.getClass.getDeclaredMethods.filter(_.getName == "apply").head
    method.invoke(f, inputs.asInstanceOf[Seq[Object]]:_*)
  }

}


object Registry extends RegistryLowImplicits with Constraints with HLists {

  implicit class RegistryOps1[Ins <: HList, Out <: HList](r: Registry[Ins, Out]) {
    def +:[A1, R](f: A1 => R)(implicit tag: TypeTag[A1 => R]): Registry[A1 :: Ins, R :: Out] =
      addFunction1(r)(f)
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

  /** the empty registry */
  def end: Registry[HNil.type, HNil.type] =
    Registry[HNil.type, HNil.type](Nil, Nil)

  /** synonym for end if that keyword is already taken: rend = registry end */
  def rend: Registry[HNil.type, HNil.type] =
    end

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

}


