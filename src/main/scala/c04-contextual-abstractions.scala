import Typeclasses.{Data, Show}

/**
 * Chapter 4: Contextual abstractions (a.k.a. implicits we all love(d) in Scala 2).
 *
 * The table below presents contextual language features of Scala 3 and how they relate to Scala 2.
 *
 * +----------------------------------------------------------+------------------------------------------+-------------------------------------------+
 * |                   Contextual feature                     |        Implementation in Scala 2         |         Implementation in Scala 3         |
 * +----------------------------------------------------------+------------------------------------------+-------------------------------------------+
 * | implicit parameters                                      | implicit val                             | given/using clauses                       |
 * | extension methods (adding methods to existing types)     | implicit class                           | extension clause                          |
 * | implicit conversion                                      | implicit def                             | Conversion[From, To] type class           |
 * | type classes                                             | trait + implicit object + implicit class | trait + given/using + ext. method(s)      |
 * | context functions (formerly known as implicit functions) | -                                        | context functions                         |
 * |----------------------------------------------------------+------------------------------------------+-------------------------------------------+
 *
 * Why implicits deprecated in favor of distinct Contextual Abstractions: https://dotty.epfl.ch/docs/reference/contextual/motivation.html
 * Relationship between Scala 3 contextual features and Scala 2 implicits: http://dotty.epfl.ch/docs/reference/contextual/relationship-implicits.html
 * Note: `implicit` keyword will be supported only until the last release of Scala 3.1.x.
 */

// format: off
object ImplicitConfusion:
  implicit def fun1(baz: Int): String = ??? // Implicit conversion 🙈
  def fun2(implicit bar: String): String = ??? // Function, that requires implicit parameter to run.
  implicit def fun3(implicit foo: String): String = ??? // Type class instance, that requires another type class instance. Think of Encoder[List[A]].
  implicit class Class(val foo: String) {} // Implicit class a.k.a. syntax
// format: on

/**
 * Chapter 3.1: Implicit parameters a.k.a. term inference
 * [] Given/using
 * [] Given/using and implicit can be used interchangibly
 * [] Term inference -> compiler looks for the type
 */
//object ImplicitParams:
//  import scala.concurrent.ExecutionContext
//  import scala.concurrent.Future
//
//  implicit val executionContext: ExecutionContext = ExecutionContext.parasitic
//  def sendPostRequest(url: String)(implicit ec: ExecutionContext): Future[String] = Future.unit.map(_ => "OK")
//  sendPostRequest("https://moia.io")

object ImplicitParams:
  import scala.concurrent.ExecutionContext
  import scala.concurrent.Future

  given ExecutionContext = ExecutionContext.parasitic // no name needed (unless you will use it)
  def sendPostRequest(url: String)(using ExecutionContext): Future[String] = Future.unit.map(_ => "OK")
  sendPostRequest("https://moia.io")

/**
 * Chapter 3.2: Extension methods
 * [] New extension syntax
 * [] Type parameters in the new syntax
 */
//@main def extensionMethods =
//  implicit class StringOps(value: String) {
//    def times(times: Int): Seq[String] = (1 to times).map(_ => value)
//    def unit: Unit = ()
//  }
//
//  println("a".times(20).toList)
//  println("a".times(20).toList)

@main def extensionMethods =
  extension[A](value: A)
    def times(times: Int): Seq[A] = (1 to times).map(_ => value)
    def unit: Unit = ()

  println("a".times(20).toList)
  println("a".times(20).toList)

/**
 * Exercises: extension methods
 */
@main def extensionMethodsExercises =
  // Exercise 1: Extend java.time.Instant with a method
  // calculating the duration between two instants: def -(until: Instant): java.time.Duration
  // Hint: You can use java.time.Duration.between(one, two).
  import java.time.* // In Scala 3 `*` is the wildcard import and not `_` anymore.

  extension(instant: Instant)
    def -(until: Instant): java.time.Duration = java.time.Duration.between(instant, until)
  
// The following should compile:
  println(Instant.now() - Instant.now().plusSeconds(10))

// Exercise 2: Implement def +(thatTuple: Tuple2[A, B]) function on the Tuple2
// Hint: Use the Numeric type class (https://www.scala-lang.org/api/current/scala/math/Numeric.html).

//  extension[A, B](tuple: Tuple2[A, B])(using a: Numeric[A], b: Numeric[B])
//    def +(thatTuple: Tuple2[A, B]) = (a.plus(tuple._1, thatTuple._1), b.plus(tuple._2, thatTuple._2))
  import scala.math.Numeric.Implicits.*
  
  extension [A: Numeric, B: Numeric](tuple: Tuple2[A, B])
    def +(thatTuple: Tuple2[A, B]) = ((tuple._1 + thatTuple._1), (tuple._2 + thatTuple._2))
  
// The following should compile:
  println((2, 2.1) + (3, 4.0)) // result: (5, 6.1)

/**
 * Chapter 3.3: Type classes
 * [] Type Classes in Scala 3
 * [] implicitly => summon
 */
object Typeclasses:
  case class Data(field: String)

  // 1. Type class declaration
//  trait Show[A]:
//    def show(a: A): String
  trait Show[A]:
    extension(a:A) def show: String
  
  // 2. Type class instance for the custom data type
//  implicit val dataShow: Show[Data] = new Show[Data] { // or implicit object...
//    override def show(data: Data): String = s"Data(field = ${data.field})"
//  }
  given Show[Data] with // optional: give it a name
    extension (data: Data) def show: String = s"Data(field = ${data.field})"

  // 3. Interface w/ summoner
  object Show:
    def show[A](a: A)(using ev: Show[A]): String = ev.show(a)

  // 4. Syntax
//  implicit class ShowOps[A](a: A)(implicit ev: Show[A]) {
//    def show: String = ev.show(a)
//  }
// Not needed anymore
  
  // 5. Usage
  def usage[A: Show](a: A) = a.show

// 6. Derivation
  case class Person(name: String) //derives Show -> companion object of the class needs to have derive method

/**
 * Exercises: type classes
 */
object TypeclassesExercises:
  // Exercise 1. For the following Monad type class declaration implement:
  // - instance for Option
  // - syntax
  trait Monad[F[_]]:
    extension[A](fa: F[A]) def bind[B](f: A => F[B]): F[B]
    extension[A](a: A) def unit: F[A]
  
  given Monad[Option] with
    extension[A](fa: Option[A]) def bind[B](f: A => Option[B]): Option[B] = fa.fold(None)(f)
    extension[A](a: A) def unit: Option[A] = Some(a)

  @main  def pMonad =
    println(10.unit)
    println(Some(2).bind(x => Some(x * 5)).bind(_ => None)) 

/**
 * Chapter 3.4: Implicit conversions
 */
object ImplicitConversions:
  //implicit def booleanToString(input: Boolean): String = input.toString
  given Conversion[Boolean, String] with
    def apply(input: Boolean): String = input.toString
  
  def identity(input: String): String = input
  identity(true)
  val x: String = false

/**
 * Chapter 3.5: Importing givens & extension methods
 * [] _ is replaced by *
 * [] givens must be explicitly imported
 * [] import by given's type
 * [] import by given's name
 * [] import all givens
 * [] import aliases
 */
object Instances:
  given x: String = "hello world"
  given y: Int = 42
  val test: Int = 42
  extension (str: String) def to42: Int = 42
  given Conversion[Boolean, String] with
    def apply(input: Boolean): String = input.toString

object Scoping:
  import Instances.{given String, given Int, *} // need to export givens explicitely (.* does not work)
  import Instances.to42
  import Instances.*
  // can import: all givens (given), by name (given someName) or by type (given String)
  summon[String] // instead of implicitly you use summon
  //summon[Conversion[Boolean, String]]
  "a".to42
  //val str: String = false

/**
 * Chapter 3.6: Context functions
 *
 * https://www.scala-lang.org/blog/2016/12/07/implicit-function-types.html
 */
object ContextFunctions:
  import scala.concurrent.{Future, ExecutionContext}
  import concurrent.ExecutionContext.Implicits.global

  case class TracingContext(traceId: String)
  
  type WithContext[A] = TracingContext ?=> A // using tracing contecxt => A
  
  object WithContext:
    def traceId(using TracingContext): String = summon[TracingContext].traceId

  def createUser(userData: String): WithContext[Future[Unit]] =
    for {
      _ <- validateUser(userData)
      _ = WithContext.traceId
      _ <- insertUser(userData)
    } yield ()
  def validateUser(userData: String)(using TracingContext): Future[Unit] = ???
  def insertUser(userData: String)(using TracingContext): Future[Unit] = ???
