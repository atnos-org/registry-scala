package org.atnos.registry

import cats.Monad
import cats.effect.{IO, LiftIO}

trait Logger[M[_]] {

  def info(s: String): M[Unit]
  def error(s: String): M[Unit]
  def warn(s: String): M[Unit]
  def debug(s: String): M[Unit]

}

case class ConsoleLogger[M[_]: Monad]() extends Logger[M] {

  def info(s: String): M[Unit] =
    Monad[M].pure(println(s"[INFO] $s"))

  def error(s: String): M[Unit] =
    Monad[M].pure(println(s"[ERROR] $s"))

  def warn(s: String): M[Unit] =
    Monad[M].pure(println(s"[WARN] $s"))

  def debug(s: String): M[Unit] =
    Monad[M].pure(println(s"[DEBUG] $s"))

}

object ConsoleLogger {

  def newConsoleLogger[M[_] : Monad : LiftIO]: Logger[M] =
    ConsoleLogger[M]

}

// DATABASE
case class Command()

trait FromRow[A]
trait ToRow[A]

trait Database[M[_]] {

 def get[A : FromRow](c: Command): M[Option[A]]
 def list[A : FromRow](c: Command): M[List[A]]
 def insert[A : ToRow](c: Command, as: List[A]): M[Unit]

}

case class DatabaseConfig(host: String, port: Int)

case class PostgresDatabase[M[_] : Monad : LiftIO](config: DatabaseConfig, logger: Logger[M]) extends Database[M] {
  def get[A : FromRow](c: Command): M[Option[A]] = Monad[M].pure(None)
  def list[A : FromRow](c: Command): M[List[A]] = Monad[M].pure(Nil)
  def insert[A : ToRow](c: Command, as: List[A]): M[Unit] = Monad[M].pure(())
}

object Database {

  // Starting the database is likely to be an IO action
  def newDatabase[M[_] : Monad : LiftIO]: (DatabaseConfig, Logger[M]) => IO[Database[M]] =
    (config: DatabaseConfig, logger: Logger[M]) =>
      IO.pure(PostgresDatabase(config, logger))

}

