package org.atnos.registry

/**
 * Set of constraints applicable to being able to create instances out of a Registry
 *
 *  - Contains[A, Out] specifies that A is contained in the list of types Out
 *  - Subset[Ins, Out] specifies that all the elements of Ins are contained in the list of types Out
 */
trait Constraints {

  trait Contains[A, Out <: HList]

  implicit def containsBase[A, Out <: HList]: Contains[A, A :: Out] =
    new Contains[A, A :: Out] {}

  implicit def containsRecursive[A, B, Out <: HList](implicit recursive: Contains[A, Out]): Contains[A, B :: Out] =
    new Contains[A, B :: Out] {}

  trait Subset[Ins <: HList, Out <: HList]

  implicit def subsetBase[A, Out <: HList](implicit contains: Contains[A, Out]): Subset[A :: HNil.type, Out] =
    new Subset[A :: HNil.type, Out] {}

  implicit def subsetRecursive[A, Ins <: HList, B, Out <: HList](implicit contains: Contains[A, Out], subset: Subset[Ins, Out]): Subset[A :: Ins, Out] =
    new Subset[A :: Ins, Out] {}

}

object Constraints extends Constraints