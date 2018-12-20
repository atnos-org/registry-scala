package org.atnos.registry

/**
 * Set of constraints applicable to being able to create instances out of a Registry
 *
 *  - Contains[A, Out] specifies that A is contained in the list of types Out
 *  - Subset[Ins, Out] specifies that all the elements of Ins are contained in the list of types Out
 *
 *  The ordering of implicits through the ConstraintsHigh and ConstraintsLow traits is essential to
 *  making the search fast enough
 */
trait Constraints extends ConstraintsHigh with ConstraintsLow

trait ConstraintsLow {

  implicit def containsRecursive[A, B, Out <: HList](implicit recursive: Contains[A, Out]): Contains[A, B :: Out] =
    new Contains[A, B :: Out] {}

  implicit def subsetRecursive[A, Ins <: HList, B, Out <: HList](implicit contains: Contains[A, Out], subset: Subset[Ins, Out]): Subset[A :: Ins, Out] =
    new Subset[A :: Ins, Out] {}


  implicit def add1[A, H1 <: HList, H2 <: HList, H3 <: HList](implicit add: Add[H1, H2, H3]): Add[A :: H1, H2, A :: H3] =
    new Add[A :: H1, H2, A :: H3] {}

}

trait ConstraintsHigh {

  implicit def containsBase[A, Out <: HList]: Contains[A, A :: Out] =
    new Contains[A, A :: Out] {}

  implicit def subsetBase[A, Out <: HList](implicit contains: Contains[A, Out]): Subset[A :: HNil.type, Out] =
    new Subset[A :: HNil.type, Out] {}

  implicit def add0[H <: HList]: Add[HNil.type, H, H] =
    new Add[HNil.type, H, H] {}

}

trait Contains[A, Out <: HList]
trait Add[H1 <: HList, H2 <: HList, R <: HList]
trait Subset[Ins <: HList, Out <: HList]

object Constraints extends Constraints