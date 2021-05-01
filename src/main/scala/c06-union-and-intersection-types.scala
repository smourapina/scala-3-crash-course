import enumsExercises.{CustomerId, PaymentAuthorizationError, PaymentId, PaymentStatus, Token}

/**
 * Chapter 6: Union and intersection types
 */

object UnionTypes:
  /**
   * Example 1: Basics
   * [] Union type
   * [] Commutativity
   * [] Variance
   * [] Union types vs either
   */

  // definition
  val x: Boolean | String = true // can only be used with pattern matching (see below)

  // usage
  def f(in: Boolean | String): String =
    in match
      case _: Boolean => "bool"
      case _: String => "str"
  f(x)

  // A <: A | B
  // B <: A| B
  
  val yy: Option[String] | Option[Int] = ???
  val xxx: Option[String | Int] = yy // xx and yy are the same
  
  // How many values does type `Either[Boolean, Boolean]` have? -> 4 (true / false in right/left)
  // How many values does type `Boolean | Boolean` have? -> 2 values
  // Union types are erased at compiler time; also a monad (good for sequential composition)

  /**
   * Example 2: What type will be inferred?
   * [] Can we improve inference with union types?
   */
  val what: String | Int = if (1 == 1) "a" else 10 // compiler looks for the least upper bound
  // Improve inference by adding the type

  /**
   * Example 3: Possible use case: union types and eithers
   */
  enum FooError:
    case FooError1
    case FooError2

  enum BarError:
    case BarError1
    case BarError2

  val error1: Either[FooError, String] = Left(FooError.FooError1)
  val error2: Either[BarError, String] = Left(BarError.BarError1)

  val value: Either[FooError | BarError, Unit] = // allows more precision in specifying types (explicitely)
    for
      _ <- error1
      _ <- error2
    yield ()

//
// Exercise 1: Model a PaymentAuthorizationError ADT from Chapter 3 (enums) using a union type.
// What pros/cons do you see when you use a union type vs enums in modeling ADTs?
//
object ModelPaymentAuthorizationError:
  case class IllegalPaymentStatus(existingPaymentId: String, existingPaymentStatus: String)
  case class IllegalRequestData(reason: String)
  case class CustomerUnknown(unknownCustomerId: CustomerId)
  case class InvalidToken(invalidToken: Token) 
  type PaymentAuthorizationError = IllegalPaymentStatus | IllegalRequestData | CustomerUnknown | InvalidToken
 
object IntersectionTypes:
  /**
   * Example 1: Basics
   * [] Example
   * [] Subtyping
   * [] Variance
   * [] Definition
   */
  object Example1:
    trait A:
      def foo: String

    trait B:
      def bar: Int

    def x: A & B = new A with B {
      // override methods here
    } 
    // x will have the members from type A and all the members from type B
    x.foo
    x.bar
  
    val a: A = x
    val b: B = x
  
    // Same applies as for union types for variance 
 
  /**
   * Example 2: Conflicting members
   * [] Same name different type
   * [] Same name same type
   */
  object Example21:
    trait A:
      def foo: String
    trait B:
      def foo: Int

    def x: A & B = ???
    x.foo // type is: String & Int; caller needs to provide implementation

  object Example22:
    trait A:
      def foo: Boolean
    trait B:
      def foo: Boolean

    def x: A & B = ???
    x.foo // works, type is: Boolean, it is up to the caller to pick right implementation

  /**
   * Example 3: Intersection types vs compound types (a.k.a. `with` types from Scala 2)
   * [] With vs &
   * [] Commutativity
   */
  trait Foo:
    def f: AnyVal
  trait A extends Foo:
    override def f: Boolean
  trait B extends Foo:
    override def f: AnyVal

  val x: String with Int = ??? // valid interserction type

// Comutativity:
// :type (???: A & B).f - Boolean & AnyVal (in scala2: type is boolean)
// :type (???: B & A).f - AnyVal & Boolean (in scala2: type is anyval)

// btw. there's a nice talk by Dean Wampler that thoroughly explains
// all the properties of union and intersection types: https://youtu.be/8H9KPlGSBnM?t=1270
