/**
 * Chapter 5: Opaque types
 */

object AnyValDrawbacks:
  object ValueClasses {
    case class PaymentId(id: String) extends AnyVal
    val paymentId = PaymentId("abc")
    def printName(paymentId: PaymentId) = println(paymentId.id)
    // val somePaymentId: Option[PaymentId] = Some(PaymentId("abc")) // this will box :(
  }
// More on that: https://failex.blogspot.com/2017/04/the-high-cost-of-anyval-subclasses.html

object OpaqueTypes:
  /**
   * Example 1: From value classes to Opaque Types.
   */
//  case class FirstName(value: String) extends AnyVal
  opaque type FirstName >: String = String // guaranteed not to box on runtime

  /**
   * Example 2: Defining companion object with constructors and extensions.
   */
  object FirstName:
    def fromString(str: String): FirstName = str
  
  extension (fn: FirstName) def value: String = fn // extension method cannot be val, needs to be def
  /**
   * Example 3: Type bounds.
   */

  ???

object OpaqueTypesUsage:
  import OpaqueTypes.*
  val x: FirstName = FirstName.fromString("name")
  x.value
  val xx: FirstName = "name" // works becuse of type bound above
  // val yy: String = x // works if we put lower type bound above

/**
 * Exercises
 */

//
// Exercise 1: Use Opaque types.
//
object OpaqueTypeExercises:
  import java.util.Locale
  opaque type Country = String
  
  object Country:
    private val validCodes = Locale.getISOCountries

    private def apply(code: String): Country = new Country(code)

    def fromIso2CountryCode(code: String): Option[Country] = Some(code).filter(validCodes.contains).map(Country.apply)

    def unsafeFromIso2CountryCode(code: String): Country = fromIso2CountryCode(code)
      .getOrElse(
        throw new IllegalStateException(s"Cannot parse country from String. Expected country code. Got '$code'.")
      )

    val Germany: Country = Country("DE")
    val UnitedKingdom: Country = Country("GB")

  extension (c: Country) def code: String = c

@main def opaqueTypeExercisesMain =
  import OpaqueTypeExercises.*
  val country: Option[Country] = Country.fromIso2CountryCode("DE")
  println(country)
  println(country.map(_.code))
