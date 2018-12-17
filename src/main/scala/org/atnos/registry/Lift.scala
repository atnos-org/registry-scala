package org.atnos.registry

import cats.Monad
import cats.implicits._

/**
 * There is hopefully a better way to abstract over the arity of these functions
 * without impacting compilation times too much
 */
object Lift {

  implicit class ArgsToOps1[A1, R, M[_] : Monad](f: A1 => M[R]) {
    def liftArgs: M[A1] => M[R] = _.flatMap(f)
  }
  implicit class ArgsToOps2[A1, A2, R, M[_] : Monad](f: (A1, A2) => M[R]) {
    def liftArgs: (M[A1], M[A2]) => M[R] = f.to[M].apply(_,_).flatMap(identity)
  }
  implicit class ArgsToOps3[A1, A2, A3, R, M[_] : Monad](f: (A1, A2, A3) => M[R]) {
    def liftArgs: (M[A1], M[A2], M[A3]) => M[R] = f.to[M].apply(_,_,_).flatMap(identity)
  }
  implicit class ArgsToOps4[A1, A2, A3, A4, R, M[_] : Monad](f: (A1, A2, A3, A4) => M[R]) {
    def liftArgs: (M[A1], M[A2], M[A3], M[A4]) => M[R] = f.to[M].apply(_,_,_,_).flatMap(identity)
  }
  implicit class ArgsToOps5[A1, A2, A3, A4, A5, R, M[_] : Monad](f: (A1, A2, A3, A4, A5) => M[R]) {
    def liftArgs: (M[A1], M[A2], M[A3], M[A4], M[A5]) => M[R] = f.to[M].apply(_,_,_,_,_).flatMap(identity)
  }
  implicit class ArgsToOps6[A1, A2, A3, A4, A5, A6, R, M[_] : Monad](f: (A1, A2, A3, A4, A5, A6) => M[R]) {
    def liftArgs: (M[A1], M[A2], M[A3], M[A4], M[A5], M[A6]) => M[R] = f.to[M].apply(_,_,_,_,_,_).flatMap(identity)
  }
  implicit class ArgsToOps7[A1, A2, A3, A4, A5, A6, A7, R, M[_] : Monad](f: (A1, A2, A3, A4, A5, A6, A7) => M[R]) {
    def liftArgs: (M[A1], M[A2], M[A3], M[A4], M[A5], M[A6], M[A7]) => M[R] = f.to[M].apply(_,_,_,_,_,_,_).flatMap(identity)
  }
  implicit class ArgsToOps8[A1, A2, A3, A4, A5, A6, A7, A8, R, M[_] : Monad](f: (A1, A2, A3, A4, A5, A6, A7, A8) => M[R]) {
    def liftArgs: (M[A1], M[A2], M[A3], M[A4], M[A5], M[A6], M[A7], M[A8]) => M[R] = f.to[M].apply(_,_,_,_,_,_,_,_).flatMap(identity)
  }
  implicit class ArgsToOps9[A1, A2, A3, A4, A5, A6, A7, A8, A9, R, M[_] : Monad](f: (A1, A2, A3, A4, A5, A6, A7, A8, A9) => M[R]) {
    def liftArgs: (M[A1], M[A2], M[A3], M[A4], M[A5], M[A6], M[A7], M[A8], M[A9]) => M[R] = f.to[M].apply(_, _, _, _, _, _, _, _, _).flatMap(identity)
  }
  implicit class ArgsToOps10[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, R, M[_] : Monad](f: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) => M[R]) {
    def liftArgs: (M[A1], M[A2], M[A3], M[A4], M[A5], M[A6], M[A7], M[A8], M[A9], M[A10]) => M[R] = f.to[M].apply(_,_,_,_,_,_,_,_,_,_).flatMap(identity)
  }

  implicit class ToOps[A](a: A) {
    def to[M[_] : Monad]: M[A] = Monad[M].pure(a)
  }
  implicit class ToOps1[A1, R](f: A1 => R) {
    def to[M[_] : Monad]: M[A1] => M[R] = _.map(f)
  }
  implicit class ToOps2[A1, A2, R](f: (A1, A2) => R) {
    def to[M[_] : Monad]: (M[A1], M[A2]) => M[R] = (_,_).mapN(f)
  }
  implicit class ToOps3[A1, A2, A3, R](f: (A1, A2, A3) => R) {
    def to[M[_] : Monad]: (M[A1], M[A2], M[A3]) => M[R] = (_,_,_).mapN(f)
  }
  implicit class ToOps4[A1, A2, A3, A4, R](f: (A1, A2, A3, A4) => R) {
    def to[M[_] : Monad]: (M[A1], M[A2], M[A3], M[A4]) => M[R] = (_,_,_,_).mapN(f)
  }
  implicit class ToOps5[A1, A2, A3, A4, A5, R](f: (A1, A2, A3, A4, A5) => R) {
    def to[M[_] : Monad]: (M[A1], M[A2], M[A3], M[A4], M[A5]) => M[R] = (_,_,_,_,_).mapN(f)
  }
  implicit class ToOps6[A1, A2, A3, A4, A5, A6, R](f: (A1, A2, A3, A4, A5, A6) => R) {
    def to[M[_] : Monad]: (M[A1], M[A2], M[A3], M[A4], M[A5], M[A6]) => M[R] = (_,_,_,_,_,_).mapN(f)
  }
  implicit class ToOps7[A1, A2, A3, A4, A5, A6, A7, R](f: (A1, A2, A3, A4, A5, A6, A7) => R) {
    def to[M[_] : Monad]: (M[A1], M[A2], M[A3], M[A4], M[A5], M[A6], M[A7]) => M[R] = (_,_,_,_,_,_,_).mapN(f)
  }
  implicit class ToOps8[A1, A2, A3, A4, A5, A6, A7, A8, R](f: (A1, A2, A3, A4, A5, A6, A7, A8) => R) {
    def to[M[_] : Monad]: (M[A1], M[A2], M[A3], M[A4], M[A5], M[A6], M[A7], M[A8]) => M[R] = (_,_,_,_,_,_,_,_).mapN(f)
  }
  implicit class ToOps9[A1, A2, A3, A4, A5, A6, A7, A8, A9, R](f: (A1, A2, A3, A4, A5, A6, A7, A8, A9) => R) {
    def to[M[_] : Monad]: (M[A1], M[A2], M[A3], M[A4], M[A5], M[A6], M[A7], M[A8], M[A9]) => M[R] = (_,_,_,_,_,_,_,_,_).mapN(f)
  }
  implicit class ToOps10[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, R](f: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) => R) {
    def to[M[_] : Monad]: (M[A1], M[A2], M[A3], M[A4], M[A5], M[A6], M[A7], M[A8], M[A9], M[A10]) => M[R] = (_,_,_,_,_,_,_,_,_,_).mapN(f)
  }

}
