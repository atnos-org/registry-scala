package org.atnos.registry

/**
 * We could use shapeless but this simple HList definition avoids a dependency
 */
sealed trait HList

final case class ::[+H, +T <: HList](head: H, tail: T) extends HList

object HNil extends HList {
  def ::[H](h : H) = org.atnos.registry.::(h, this)
}

trait HLists {

  implicit class HListOps[T <: HList](tail: T) {
    def ::[H](h : H) : H :: T = org.atnos.registry.::(h, tail)
  }
}


object HList extends HLists