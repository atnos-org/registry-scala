package org.atnos.registry

import cats.Monad
import cats.implicits._

/**
 * There is hopefully a better way to abstract over the arity of these functions
 * without impacting compilation times too much
 */
object Lift {

  implicit class ArgsToOps1[A1, R, M[_] : Monad](f: A1 => M[R]) {
    def liftArgs: M[A1] => M[R] = (ma1: M[A1]) =>
      ma1.flatMap(f)
  }

  implicit class ArgsToOps2[A1, A2, R, M[_] : Monad](f: (A1, A2) => M[R]) {
    def liftArgs: (M[A1], M[A2]) => M[R] = (ma1: M[A1], ma2: M[A2]) =>
      for {
        a1 <- ma1
        a2 <- ma2
        r  <- f(a1, a2)
      } yield r
  }

  implicit class ArgsToOps3[A1, A2, A3, R, M[_] : Monad](f: (A1, A2, A3) => M[R]) {
    def liftArgs: (M[A1], M[A2], M[A3]) => M[R] = (ma1: M[A1], ma2: M[A2], ma3: M[A3]) =>
      for {
        a1 <- ma1
        a2 <- ma2
        a3 <- ma3
        r  <- f(a1, a2, a3)
      } yield r
  }

  implicit class ArgsToOps4[A1, A2, A3, A4, R, M[_] : Monad](f: (A1, A2, A3, A4) => M[R]) {
    def liftArgs: (M[A1], M[A2], M[A3], M[A4]) => M[R] = (ma1: M[A1], ma2: M[A2], ma3: M[A3], ma4: M[A4]) =>
      for {
        a1 <- ma1
        a2 <- ma2
        a3 <- ma3
        a4 <- ma4
        r  <- f(a1, a2, a3, a4)
      } yield r
  }

  implicit class ArgsToOps5[A1, A2, A3, A4, A5, R, M[_] : Monad](f: (A1, A2, A3, A4, A5) => M[R]) {
    def liftArgs: (M[A1], M[A2], M[A3], M[A4], M[A5]) => M[R] = (ma1: M[A1], ma2: M[A2], ma3: M[A3], ma4: M[A4], ma5: M[A5]) =>
      for {
        a1 <- ma1
        a2 <- ma2
        a3 <- ma3
        a4 <- ma4
        a5 <- ma5
        r  <- f(a1, a2, a3, a4, a5)
      } yield r
  }

  implicit class ToOps[A](a: A) {
    def to[M[_] : Monad]: M[A] = Monad[M].pure(a)
  }

  implicit class ToOps1[A1, R](f: A1 => R) {
    def to[M[_] : Monad]: M[A1] => M[R] = (ma1: M[A1]) =>
      ma1.map(f)
  }

  implicit class ToOps2[A1, A2, R](f: (A1, A2) => R) {
    def to[M[_] : Monad]: (M[A1], M[A2]) => M[R] = (ma1: M[A1], ma2: M[A2]) =>
      for {
        a1 <- ma1
        a2 <- ma2
      } yield f(a1, a2)
  }

  implicit class ToOps3[A1, A2, A3, R](f: (A1, A2, A3) => R) {
    def to[M[_] : Monad]: (M[A1], M[A2], M[A3]) => M[R] = (ma1: M[A1], ma2: M[A2], ma3: M[A3]) =>
      for {
        a1 <- ma1
        a2 <- ma2
        a3 <- ma3
      } yield f(a1, a2, a3)
  }

  implicit class ToOps4[A1, A2, A3, A4, R](f: (A1, A2, A3, A4) => R) {
    def to[M[_] : Monad]: (M[A1], M[A2], M[A3], M[A4]) => M[R] = (ma1: M[A1], ma2: M[A2], ma3: M[A3], ma4: M[A4]) =>
      for {
        a1 <- ma1
        a2 <- ma2
        a3 <- ma3
        a4 <- ma4
      } yield f(a1, a2, a3, a4)
  }

  implicit class ToOps5[A1, A2, A3, A4, A5, R](f: (A1, A2, A3, A4, A5) => R) {
    def to[M[_] : Monad]: (M[A1], M[A2], M[A3], M[A4], M[A5]) => M[R] = (ma1: M[A1], ma2: M[A2], ma3: M[A3], ma4: M[A4], ma5: M[A5]) =>
      for {
        a1 <- ma1
        a2 <- ma2
        a3 <- ma3
        a4 <- ma4
        a5 <- ma5
      } yield f(a1, a2, a3, a4, a5)
  }

}
