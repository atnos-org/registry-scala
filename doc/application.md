# Applications

There are many ways to configure and build Scala applications, starting from the infamous [Cake pattern](http://www.warski.org/blog/2011/04/di-in-scala-cake-pattern-pros-cons/), 
to [using lots of different libraries](https://di-in-scala.github.io/).

The approach we present here is based on constructor injection, automating the wiring process with a simple data structure.
In general we want to be able to:

 1. [define independent components](#define-components) requiring only knowledge of their immediate dependencies
 1. [unit test](#unit-test) them
 1. [configure](#configuration) them for different environments
 1. [instantiate the full application](#make-the-application) or a subset of it
 1. [integrate the application](#integration) and mock some dependencies
 1. [manage resources](#resources)
 1. [define singletons](#singletons)
 1. [define context-dependent configurations](#context-dependent-configurations)
 1. control the [start-up](#start-up) of the application
 1. [parametrize components](#parametrize-components-with-a-monad) with a specific monad

#### GoodBookings.com

We are going to build a booking application which needs to:

 1. listen to reservation requests ("bookings") and store them in a database
 1. listen to accommodation availabilities and store them in a database
 1. offer an API to browse bookings, availabilities and make a manual match
 1. support logging for debugging, auditing,...

### Define components

A modular approach to building such an application consists in defining distinct components:

`Logger.scala`
```
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

```

`Database.scala`
```
// low level sql interactions with a database

trait Database {
  def get[A : FromRow](command: Command): IO[Option[A]]
  def list[A : FromRow](command: Command): IO[List[A]]
  def insert[A : ToRow](command: Command, as: List[A]): IO[Unit]
}

case class PostgresDatabase(pool: ConnectionPool) extends Database {
  // use the connection pool to implement those functions
  def get[A : FromRow](command: Command): IO[Option[A]] = ???
  def list[A : FromRow](command: Command): IO[List[A]] = ???
  def insert[A : ToRow](command: Command, as: List[A]): IO[Unit] = ???
} 

case class DatabaseConfig(host: String, port: Int)

case class ConnectionPool(pool: PostgresConnectionPool) 

object ConnectionPool {

  // creating a connection pool is an IO action
  // it might fail if the database cannot be accessed
  def newConnectionPool(config: DatabaseConfig, logger: Logger): IO[ConnectionPool] = 
    Postgres.createConnectionPool(config.host, config.port).map(ConnectionPool)
}

```
`BookingRepository.scala`
```

// A "domain level" component for storing and retrieving all the data
trait BookingRepository {
  def storeRequest(request: Request): IO[Unit]
  def getRequestById(requestId: RequestId): IO[Option[Request]]
  def getAllRequests: IO[List[Request]]
}

case class PersistentBookingRepository(
  config: BookingRepositoryConfig, logger: Logger, database: Database) extends BookingRepository {
  
  // implement the methods here
  
}

```
`EventListener.scala`
```
// A generic event listener to an events system like Kafka
trait EventListener {
  // subscribe to events and consume them
  def consumeEvents(consumer: List[Event] => IO[Unit]): IO[Unit]
}

case class EventListenerConfig(eventTopic: Uri)

case class KafkaEventListener(config: EventListenerConfig) extends EventListener {
  // implement the method here 
}

object KafkaEventListener {
  // Creating the event listener will probably start a connection in IO
  def newEventListener(config: EventListenerConfig): IO[EventListener] =
    ???
}
```
`BookingEventListener.scala`
```
// A specific listener for bookings
trait BookingEventListener {
  def consumeBookings: IO[Unit]
}

case class BookingEventListenerDefault(logger: Logger, listener: EventListener) extends BookingEventListener
```
`AvailabilityEventListener.scala`
```
// A specific listener for availabilities
trait AvailabilityEventListener {
  def consumeAvailabilities: IO[Unit]
}

case class AvailabilityEventListenerDefault(
  logger: Logger, listener: EventListener) extends AvailabilityEventListener
```
`Api.scala`
```
// A HTTP API to query the data in the database
trait Api {
  def getBookings(request: Request): IO[Response]
  def getAvailabilities(request: Request): IO[Response]
  def createMatch(request: Request): IO[Response]
}

case class ApiDefault(logger: Logger, 
  bookingRepository: BookingRepository,
  availabilityRepository: AvailabilityRepository) extends Api
```
`App.scala`
```
case class App(
  api: Api,
  bookings: BookingEventListener,
  availabilities: AvailabilityEventListener)
```

The overall dependency graph looks like
```
                         App

       +------------------+----------------------+
       |                  |                      |
       v                  v                      v
      Api  BookingsEventListener AvailabilitiesEventListener
       |                  |                      |
       |                  |                      |
       v                  |                      |
  BookingRepository <-----+----------------------+
       |
       v
    Database
```

On this diagram we don't show the `Logger` component which is likely to be embedded everywhere and the `EventListener`
component embedded in both `Booking` and `Availability` listeners.

### Unit test

Unit-testing components as defined above is really straightforward because each component defines a constructor function for
 which you can provide dummy values if necessary. For example a `Logger` which doesn't print anything to the console:
```
val noLogger = new Logger {
  def info(s: String) = IO.pure(())
  def error(s: String) = IO.pure(())
}
```

### Configuration

Configuring the application consists in gathering all constructors and the required pieces of 
configuration (the `Config` data types) into a `Registry`:
```
val registry =
  EventListenerConfig(uri"https://kafka/bookings") +:
  DatabaseConfig("postgres://database", 5432) +:
  (App.newApp _).to[IO] +:
  (ApiDefault.newApiDefault _).to[IO] +:
  (KafkaEventListener.newKafkaEventListener _).liftArgs +:
  (BookingEventListenerDefault.BookingEventListenerDefault _).to[IO] +:
  (AvailabilityEventListenerDefault.newAvailabilityEventListenerDefault _).to[IO] +:
  (PersistentBookingRepository.newPersistentBookingRepository _).to[IO] +:
  (PostgresDatabase.PostgresDatabase).to[IO] +:
  (ConnectionPool.newConnectionPool _).liftArgs +:
  (ConsoleLogger.newConsoleLogger).to[IO] +:
  end
```

The code above creates a `Registry` by adding values and constructors with the `+:` operator (`end` represents the empty registry). 
Since `registry` just a value, you can create smarter functions to handle different configurations for different environments you want to run on:

```
val components =
  App.apply _ +:
  ApiDefault.apply _ +:
  KafkaEventListenerDefault.apply _ +:
  BookingsEventListenerDefault.apply _ +:
  AvailabilitiesEventListenerDefault.apply _ +:
  PersistentBookingRepository.apply _ +:
  PostgresDatabase.apply _ +:
  ConnectionPool.newConnectionPool +:
  ConsoleLogger.apply _ +:
  end

val prod =
  EventListenerConfig(uri"https://kafka/bookings") +:
  DatabaseConfig("postgres://database", 5432) +:
  end

val dev =
  EventListenerConfig(uri"https://kafka-dev/bookings") +:
  DatabaseConfig("localhost", 5432) +:
  end

def registr[Ins, Out](env: Registry[Ins, Out]) = env <+> components

val prodRegistry = registry(prod)
val devRegistry  = registry(dev)
```

In that case the `<+>` operator is use to "append" 2 registries together. Now we can "make" the application.

### Make the application

In order to "make" the application we use the `make` method and specify the type of the desired component:
```
val app: App = devRegistry.make[App]
```

`make` will recursively build all the dependencies until it can build the full `App`. In case a constructor or a piece of configuration is missing the compiler will display a message like:
```
No instance for (Contains EventListenerConfig '[])
  arising from a use of ‘make’
```

We don't have to build the whole application. Once we have a Registry we can also integrate subsets of the application.

### Integration

For example we can just as easily instantiate and start the `BookingEventListener`:
```
listener = make @BookingEventListener devRegistry

listener & consumeBookings
```
This will create both the listener and the underlying database so that consumed events will be stored. Yet, for integration testing you might prefer to skip storing bookings altogether. In that case you can define a `MockDatabase`:
```
mockDatabase = Database {
  get = pure Nothing
, list = pure []
, insert = pure ()
}
```

And add it "on top" of the `devRegistry`:
```
listener = make @BookingEventListener (fun mockDatabase +: devRegistry)

listener & consumeBookings
```
Now no booking will be stored in the database while you run the listener.
Can wiring and re-wiring your application be simpler than that?

Actually the full story is a bit more complicated :-).
For one thing some components need to carefully allocate resources.

### Resources

For example the constructor for the `Database` returns an `IO Database`. This is problematic for the registry resolution algorithm because the `BookingRepository.new` function requires a `Database` not an `IO Database`. What can we do? The simplest thing is to actually "lift" everything into the same `IO` monad using some variations of the `val` and `fun` combinators:

 - `valTo @m`    lifts a value `a` into `m a`
 - `funTo @m`    lifts a function `a -> b -> c -> ... -> o` into `m a -> m b -> m c -> ... -> m o`
 - `funAs @m` lifts a function `a -> b -> c -> ... -> m o` into `m a -> m b -> m c -> ... -> m o`

(please read the [reference guide](./reference.md) for a list of the "lifting" combinators)

This means that a "real-life" application registry looks like:
```
registry =
     valTo @IO (EventListenerConfig [urihttps://kafka/bookings])
  +: valTo @IO (DatabaseConfig "postgres://database" 5432)
  +: funTo @IO Database.new
  +: funTo @IO BookingRepository.new
  +: funTo @IO EventListener.new
  +: funTo @IO BookingEventListener.new
  +: funTo @IO AvailabilitiesEventistener.new
  +: funTo @IO Api.new
  +: funTo @IO Database.new
  +: end
```

In general the monad used won't even be `IO` but a `ResourceT` monad because components allocating resources should better close them down gracefully when they are done. While you can use your own `ResourceT IO` this library provides a `Data.Registry.RIO` monad supporting resource allocation (please have a look at the API).

In terms of resources management we are almost there. We still need to solve one issue.

When we use `Database.new` we get back an `IO Database` which goes on top of the stack. This `IO Database` value can then be used by all the lifted functions in our registry, like `BookingRepository.new`. However since the `BookingRepository` is used by 3 other components everytime we use it we will get a new version of the `Database`! Because the action `IO Database` will be executed 3 times giving us 3 accesses to the database. This is clearly undesirable since a `Database` component maintains a pool of connections to the database. What we need is to make a "singleton" for the database.

### Singletons

The `singleton` function does exactly this:
```
registry =
     singleton @IO @Database $

     valTo @IO (EventListenerConfig [urihttps://kafka/bookings])
  +: valTo @IO (DatabaseConfig "postgres://database" 5432)
  +: funTo @IO Database.new
  +: funTo @IO BookingRepository.new
  +: funTo @IO EventListener.new
  +: funTo @IO BookingEventListener.new
  +: funTo @IO AvailabilitiesEventistener.new
  +: funTo @IO Api.new
  +: funTo @IO Database.new
  +: end
```

The `singleton` declaration will slightly "tweak" the registry to say "if you create an `IO Database` cache this action so that the same `Database` is returned everytime an `IO Database` value is needed. Since caching is involved the signature of the `registry` changes from a pure value to a monadic one:
```
registry :: IO Registry inputs outputs
registry = devRegistry & singleton @IO @Database
```
And if you need to make several singletons in your application you will have to use the "bind" monadic operator
```
registry :: IO Registry inputs outputs
registry = devRegistry &
            singleton @IO @Database >>=
            singleton @IO @Metrics
```

In terms of configuration we are almost done. We just need to address one last difficulty.

We have 2 different listeners which are both using an `EventListener`. That component can be configured to listen to a specific queue of events with `EventListenerConfig`. But if the 2 listeners eventually share the same configuration they are going to listen to the same event!

### Context dependent configurations

What we need then is to "specialize" the configuration to use depending on what we are building. If we are building a `BookingEventListener` we want the `EventListener` to be created with `configBooking` and if we are building an `AvailabilityEventListener` we want the `EventListener` to be created with `configAvailability`
```
configBooking =
  EventListenerConfig [urihttps://kafka-prod/bookings]

configAvailability =
  EventListenerConfig [urihttps://kafka-prod/availabilities]
```

Then we need to tell the Registry what we want to happen with the `specialize` function:
```
registry =
  devRegistry &
-- when trying to build IO BookingEventListener, use configBooking whenever
-- an EventListenerConfig is required
  specializeVal @(IO BookingEventListener) configBooking &
  specializeVal @(IO AvailabilityEventListener) configAvailability
```

If it all looks too confusing please have a look at the [reference guide](./reference.md) to see all the available combinators and their meaning at once.

Our main use-cases for configuring and instantiating the application are now covered, we can add another feature, controlling the start-up

### Start-up

Some applications once started, before being even used can run some actions:

 - do a self health-check: "is the database really connected?"
 - do a dependency health-check: "is the event service available?"
 - load caches

For those use cases you can benefit from the `Data.Registry.RIO` type which not only has a `ResourceT` instance but also defines a list of actions which you can create with the `Data.Registry.Warmup` module. For example you can write:
```
-- in Database.scala

new :: Config -> RIO Database
new config = do
  -- allocate the connection as a resource
  connection <- allocate (createConnection config) pure
  let database = Database {
         get    = getWithConnection connection
       , list   = listWithConnection connection
       , insert = insertWithConnection connection
       }
  warmupWith (warmupOf database (database & tryList))

tryList :: Database -> IO ()
tryList database =
  void $ (database & list) (Command "select * from bookings limit 1")
```

If the action passed to `warmupOf` throws an exception then the whole application start-up will be aborted and the exception reported. This will happen when you use the
`Data.Registry.RIO.withRegistry` function:
```
startApp = withRegistry prodRegistry $\result application ->
  if isSuccess result then
    do _ <- fork $ application & bookings & consumeBookings
       _ <- fork $ application & availabilities & consumeAvailabilities
       pure ()

  else
    print $ "could not start the application" <> show result
```

`withRegistry` gives you the opportunity to act on the result of starting the full application before making the application available.

This is all entirely optional though! You don't have to use `Data.Registry.Warmup` nor the `RIO` type and you can decide by yourself how to deal with resources and startup.

### Parametrize components with a monad

It can be useful to make the interfaces to your components slightly more generic in terms of what "effects" the interfaces
can offer. For example `Logger` could be implemented like this:

```
data Logger m = Logger {
  info  :: Text -> m ()
, error :: Text -> m ()
}

-- | RequestId can be used to add some "tracing" information to the log
--   statements
new :: (MonadReader RequestId, MonadIO m) => Logger m
new = Logger m {
  info t  = print ("[INFO] " <> t)
, error t = print ("[ERROR] " <> t)
}
```

Then when you define your registry you can specify which monad `m` you want your components to run in.
For example:
```

type M = ReaderT RequestId IO

components =
     funTo @IO (Database.new @M)
  +: funTo @IO (BookingRepository.new @M)
  +: funTo @IO (EventListener.new @M)
  +: funTo @IO (BookingEventListener.new @M)
  +: funTo @IO (AvailabilitiesEventistener.new @M)
  +: funTo @IO (Api.new @M)
  +: funTo @IO (Database.new @M)
  +: end
```

By keeping the monad `m` open on each component interface you can have constructors using
something like [`MonadConc m`](https://hackage.haskell.org/package/concurrency) and then use [`dejafu`](https://hackage.haskell.org/package/dejafu)
to test the concurrency of your application.

In any case the monad you use should be more or less isomorphic to `ReaderT Env IO` to allow your components to access `IO` directly
and benefit from libraries for [async computations](https://hackage.haskell.org/package/async), [retrying](https://hackage.haskell.org/package/retry),
or [controlling resources](https://hackage.haskell.org/package/resourcet).