package org.atnos.registry

import org.specs2._
import Registry._
import cats.effect._
import Lift._
import Application._

class ApplicationSpec extends Specification with ScalaCheck { def is = s2"""

  we can assemble an application from a registry $test1
  we can create different registries $test2


"""

  def test1 = {
    val registry =
      EventListenerConfig(Uri("https://kafka/bookings")).to[IO] +:
      DatabaseConfig("postgres://database", 5432).to[IO] +:
      (App.newApp _).to[IO] +:
      (ApiDefault.newApiDefault _).to[IO] +:
      ConsoleLogger.newConsoleLogger.to[IO] +:
      (BookingEventListenerDefault.newBookingEventListenerDefault _).to[IO] +:
      (AvailabilityEventListenerDefault.newAvailabilityEventListenerDefault _).to[IO] +:
      (PersistentBookingRepository.newPersistentBookingRepository _).to[IO] +:
      (PostgresDatabase.newPostgresDatabase _).to[IO] +:
      (KafkaEventListener.newKafkaEventListener _).liftArgs +:
      (ConnectionPool.newConnectionPool _).liftArgs +:
        rend

    val app: App = registry.make[IO[App]]
    app must not(beNull)
  }

  def test2 = {

/*
    val components =
      App.apply _ +:
        Api.apply _ +:
        KafkaEventListenerDefault.apply _ +:
        BookingsEventListenerDefault.apply _ +:
        AvailabilitiesEventListenerDefault.apply _ +:
        PersistentBookingRepository.apply _ +:
        PostgresDatabase.apply _ +:
        ConnectionPool.newConnectionPool +:
        ConsoleLogger.apply _ +:
        end

    val prod =
      EventListenerConfig(Uri("https://kafka/bookings")) +:
        DatabaseConfig("postgres://database", 5432) +:
        end

    val dev =
      EventListenerConfig(Uri("https://kafka-dev/bookings")) +:
        DatabaseConfig("localhost", 5432) +:
        end

    def registry[Ins, Out](env: Registry[Ins, Out]) = env <+> components

    val prodRegistry = registry(prod)
    val devRegistry  = registry(dev)
    val app: App = devRegistry.make[App]

    app must not(beNull)
*/
    ok
  }



}

object Application {
  trait Logger {
    def info(s: String): IO[Unit]
    def error(s: String): IO[Unit]
  }

  case class ConsoleLogger() extends Logger {
    def info(s: String): IO[Unit]  = IO.delay(println("[INFO] "+s))
    def error(s: String): IO[Unit] = IO.delay(println("[ERROR] "+s))
  }

  object ConsoleLogger {
    def newConsoleLogger: Logger =
      new ConsoleLogger
  }

  trait FromRow[A]
  trait ToRow[A]
  trait Command
  trait Database {
    def get[A : FromRow](command: Command): IO[Option[A]]
    def list[A : FromRow](command: Command): IO[List[A]]
    def insert[A : ToRow](command: Command, as: List[A]): IO[Unit]
  }

  case class PostgresDatabase(pool: ConnectionPool) extends Database {
    def get[A : FromRow](command: Command): IO[Option[A]] = ???
    def list[A : FromRow](command: Command): IO[List[A]] = ???
    def insert[A : ToRow](command: Command, as: List[A]): IO[Unit] = ???
  }

  object PostgresDatabase {
    def newPostgresDatabase(pool: ConnectionPool): Database =
      PostgresDatabase(pool)
  }

  case class DatabaseConfig(host: String, port: Int)

  trait PostgresConnectionPool
  case class ConnectionPool(pool: PostgresConnectionPool)

  object ConnectionPool {

    // creating a connection pool is an IO action
    // it might fail if the database cannot be accessed
    def newConnectionPool(config: DatabaseConfig, logger: Logger): IO[ConnectionPool] =
      IO.delay(ConnectionPool(new PostgresConnectionPool {}))
  }

  trait Request
  trait RequestId

  trait BookingRepository {
    def storeRequest(request: Request): IO[Unit]
    def getRequestById(requestId: RequestId): IO[Option[Request]]
    def getAllRequests: IO[List[Request]]
  }

  case class BookingRepositoryConfig()

  class PersistentBookingRepository(
    config: BookingRepositoryConfig, logger: Logger, database: Database) extends BookingRepository {
    def storeRequest(request: Request): IO[Unit] = ???
    def getRequestById(requestId: RequestId): IO[Option[Request]] = ???
    def getAllRequests: IO[List[Request]] = ???
  }

  object PersistentBookingRepository {
    def newPersistentBookingRepository(config: BookingRepositoryConfig, logger: Logger, database: Database): BookingRepository =
      PersistentBookingRepository(config, logger, database)
  }

  trait Event
  case class Uri(s: String)
  trait EventListener {
    def consumeEvents(consumer: List[Event] => IO[Unit]): IO[Unit]
  }

  case class EventListenerConfig(eventTopic: Uri)

  case class KafkaEventListener(config: EventListenerConfig) extends EventListener {
    def consumeEvents(consumer: List[Event] => IO[Unit]): IO[Unit] = ???
  }

  object KafkaEventListener {
    def newKafkaEventListener(config: EventListenerConfig): IO[EventListener] =
      IO.pure(KafkaEventListener(config))
  }

  trait BookingEventListener {
    def consumeBookings: IO[Unit]
  }

  case class BookingEventListenerDefault(logger: Logger, listener: EventListener) extends BookingEventListener

  object BookingEventListenerDefault {
    def newBookingEventListenerDefault(logger: Logger, listener: EventListener): BookingEventListener =
      BookingEventListenerDefault(logger, listener)
  }

  trait AvailabilityEventListener {
    def consumeAvailabilities: IO[Unit]
  }

  case class AvailabilityEventListenerDefault(
    logger: Logger, listener: EventListener) extends AvailabilityEventListener

  object AvailabilityEventListenerDefault {
    def newAvailabilityEventListenerDefault(logger: Logger, listener: EventListener): AvailabilityEventListener =
      AvailabilityEventListenerDefault(logger, listener)
  }

  trait Response

  trait Api {
    def getBookings(request: Request): IO[Response]
    def getAvailabilities(request: Request): IO[Response]
    def createMatch(request: Request): IO[Response]
  }

  case class ApiDefault(logger: Logger,
                        bookingRepository: BookingRepository) extends Api

  object ApiDefault {
    def newApiDefault(logger: Logger, bookingRepository: BookingRepository): Api =
      ApiDefault(logger, bookingRepository)
  }

  case class App(
                  api: Api,
                  bookings: BookingEventListener,
                  availabilities: AvailabilityEventListener)

  object App {
    def newApp(api: Api,
               bookings: BookingEventListener,
               availabilities: AvailabilityEventListener): App = App(api, bookings, availabilities)
  }

}

