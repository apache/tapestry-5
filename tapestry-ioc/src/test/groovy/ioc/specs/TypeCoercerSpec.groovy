package ioc.specs

import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.MonthDay
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Period
import java.time.Year
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

import org.apache.tapestry5.commons.services.TypeCoercer
import org.apache.tapestry5.commons.util.TimeInterval
import org.apache.tapestry5.func.F
import org.apache.tapestry5.ioc.annotations.AnnotationUseContext
import org.apache.tapestry5.plastic.PlasticUtils

import com.example.Animal;

import spock.lang.Unroll

class TypeCoercerSpec extends AbstractSharedRegistrySpecification {

  TypeCoercer coercer = getService TypeCoercer

  static String BIG_DECIMAL_VALUE = "12345656748352435842385234598234958234574358723485.35843534285293857298457234587"

  static String BIG_INTEGER_VALUE = "12384584574874385743";

  static byte byte1 = 65, byte2 = 66

  static short short1 = 22, short2 = 7

  static int int1 = 200, int2 = 300

  static long long1 = 100l, long2 = 200l

  static float float1 = 22.1f, float2 = -123.5

  static double double1 = 123d, double2 = 999.9d

  static char char1 = "1".charAt(0), char2 = "2".charAt(0)


  @Unroll
  def "coerce #inputTypeName #input to #typeName should be #expected"() {

    expect:

    coercer.coerce(input, type) == expected

    where:


    input                             | type                 | expected
    "-15"                             | Double               | -15d
    "2.27"                            | Double               | 2.27d
    227l                              | int                  | 227
    -37                               | Number               | -37
    new StringBuilder("12345")        | int                  | 12345
    "52"                              | Integer              | 52
    this                              | String               | toString()
    55l                               | Integer              | 55
    256l                              | Boolean              | true
    ""                                | Boolean              | false
    " "                               | Boolean              | false
    "x"                               | Boolean              | true
    " z "                             | Boolean              | true
    "false"                           | Boolean              | false
    "FAlse"                           | Boolean              | false
    null                              | Boolean              | false
    256d                              | Integer              | 256
    22.7                              | Integer              | 22
    0                                 | Boolean              | false
    32838l                            | Boolean              | true
    127                               | Byte                 | new Byte("127")
    58d                               | Short                | new Short("58")
    33                                | Long                 | 33l
    22                                | Float                | 22f
    1234                              | Double               | 1234d
    31.14f                            | Double               | 31.14f.doubleValue()
    []                                | Boolean              | false
    ["a"]                             | Boolean              | true
    [null]                            | Boolean              | true
    new BigDecimal(BIG_DECIMAL_VALUE) | Double               | 1.2345656748352436E49
    BIG_INTEGER_VALUE                 | BigInteger           | new BigInteger(BIG_INTEGER_VALUE)
    -12345678L                        | BigInteger           | new BigInteger("-12345678")
    "bravo"                           | List                 | ["bravo"]
    null                              | Iterator             | null
    null                              | List                 | null
    null                              | Collection           | null
    null                              | String               | null
    ["a", 123] as Object[]            | List                 | ["a", 123]
    ["a", "b"] as String[]            | List                 | ["a", "b"]
    [true, false] as boolean[]        | List                 | [true, false]
    [byte1, byte2] as byte[]          | List                 | [byte1, byte2]
    [char1, char2] as char[]          | List                 | [char1, char2]
    [short1, short2] as short[]       | List                 | [short1, short2]
    [int1, int2] as int[]             | List                 | [int1, int2]
    [long1, long2] as long[]          | List                 | [long1, long2]
    [float1, float2] as float[]       | List                 | [float1, float2]
    [double1, double2] as double[]    | List                 | [double1, double2]
    "foo/bar/baz.txt"                 | File                 | new File("foo/bar/baz.txt")
    new TimeInterval("2 h")           | Long                 | 2 * 60 * 60 * 1000l
    "2 h"                             | TimeInterval         | new TimeInterval("120 m")
    F.flow()                          | Boolean              | false
    F.flow(1, 2, 3)                   | Boolean              | true
    F.flow(1, 2, 3)                   | List                 | [1, 2, 3]
    "mixin"                           | AnnotationUseContext | AnnotationUseContext.MIXIN
    123                               | Object[]             | [123] as Object[]
    [1, 2, 3]                         | Object[]             | [1, 2, 3] as Object[]
    // TAP5-2565
    Animal.DOG                        | String               | 'DOG'
    'CAT'                             | Animal               | Animal.CAT

    // TAP5-2645
    2020                              | Year                 | Year.of(2020)
    Year.of(2020)                     | Integer              | 2020
    2020                              | Year                 | Year.of(2020)
    Month.OCTOBER                     | Integer              | 10
    10                                | Month                | Month.OCTOBER
    Month.OCTOBER                     | String               | "OCTOBER"
    "oCTobER"                         | Month                | Month.OCTOBER
    "--10-04"                         | MonthDay             | MonthDay.of(10, 4)
    MonthDay.of(10, 4)                | String               | "--10-04"
    "2020-10"                         | YearMonth            | YearMonth.of(2020, 10)
    YearMonth.of(2020, 10)            | String               | "2020-10"
    "2020-10-04"                      | LocalDate            | LocalDate.of(2020, 10, 4)
    LocalDate.of(2020, 10, 4)         | Long                 | ZonedDateTime.of(LocalDate.of(2020, 10, 4), LocalTime.of(0, 0), ZoneId.systemDefault()).toEpochSecond() * 1_000
    LocalDate.of(2020, 10, 4)         | String               | "2020-10-04"
    LocalDate.of(2020, 10, 4)         | YearMonth            | YearMonth.of(2020, 10)
    LocalDate.of(2020, 10, 4)         | MonthDay             | MonthDay.of( 10, 4)
    YearMonth.of(2020, 10)            | Month                | Month.OCTOBER
    YearMonth.of(2020, 10)            | Year                 | Year.of(2020)
    DayOfWeek.THURSDAY                | Integer              | 4
    4                                 | DayOfWeek            | DayOfWeek.THURSDAY
    DayOfWeek.THURSDAY                | String               | "THURSDAY"
    "THURSdaY"                        | DayOfWeek            | DayOfWeek.THURSDAY
    LocalDate.of(2020, 10, 4)         | String               | "2020-10-04"
    "2020-10-04"                      | LocalDate            | LocalDate.of(2020, 10, 4)
    34862776991000L                   | LocalTime            | LocalTime.of(9, 41, 2, 776991000)
    LocalTime.of(9, 41, 2, 776991000) | Long                 | 34862776991000L
    "09:41:02.776991"                 | LocalTime            | LocalTime.of(9, 41, 2, 776991000)
    LocalTime.of(9, 41, 2, 776991000) | String               | "09:41:02.776991"

    "2020-10-04T16:59:51.713909"                         | LocalDateTime  | LocalDateTime.of(2020, 10, 4, 16, 59, 51, 713909000)
    LocalDateTime.of(2020, 10, 4, 16, 59, 51, 713909000) | String         | "2020-10-04T16:59:51.713909"
    LocalDateTime.of(2020, 10, 4, 16, 59, 51, 713909000) | LocalDate      | LocalDate.of(2020, 10, 4)
    LocalDateTime.of(2020, 10, 4, 16, 59, 51, 0)         | Long           | ZonedDateTime.of(LocalDate.of(2020, 10, 4), LocalTime.of(16, 59, 51, 0), ZoneId.systemDefault()).toEpochSecond() * 1_000


    "2020-10-04T16:59:51.713909+02:00"                                           | OffsetDateTime | OffsetDateTime.of(2020, 10, 4, 16, 59, 51, 713909000, ZoneOffset.ofHours(2))
    OffsetDateTime.of(2020, 10, 4, 16, 59, 51, 713909000, ZoneOffset.ofHours(2)) | String         | "2020-10-04T16:59:51.713909+02:00"
    OffsetDateTime.of(2020, 10, 4, 16, 59, 51, 713909000, ZoneOffset.ofHours(2)) | OffsetTime     | OffsetTime.of(16, 59, 51, 713909000, ZoneOffset.ofHours(2))

    "Europe/Berlin"                     | ZoneId     | ZoneId.of("Europe/Berlin")
    ZoneId.of("Europe/Berlin")          | String     | "Europe/Berlin"
    "+02:00"                            | ZoneOffset | ZoneOffset.ofHours(2)
    ZoneOffset.ofHoursMinutes(-2, -30) | String     | "-02:30"

    "2020-10-04T16:59:51.713909+02:00[Europe/Berlin]"                                | ZonedDateTime | ZonedDateTime.of(2020, 10, 4, 16, 59, 51, 713909000, ZoneId.of("Europe/Berlin"))
    ZonedDateTime.of(2020, 10, 4, 16, 59, 51, 713909000, ZoneId.of("Europe/Berlin")) | String        | "2020-10-04T16:59:51.713909+02:00[Europe/Berlin]"
    ZonedDateTime.of(2020, 10, 4, 16, 59, 51, 713909000, ZoneId.of("Europe/Berlin")) | ZoneId        | ZoneId.of("Europe/Berlin")

    -1234L                            | Instant              | Instant.ofEpochMilli(-1234L)
    Instant.ofEpochMilli(-1234L)      | Long                 | -1234L
    new Date(1606653132000L)          | Instant              | Instant.ofEpochMilli(1606653132000L)
    Instant.ofEpochMilli(-1234L)      | Long                 | -1234L
    97200000000000L                   | Duration             | Duration.ofHours(27L)
    Duration.ofMinutes(90L)           | Long                 | 5400000000000L
    Period.of(12, 1, 7)               | String               | "P12Y1M7D"
    "P12Y1M7D"                        | Period               | Period.of(12, 1, 7)

    ZonedDateTime.of(LocalDate.of(2020, 11, 29), LocalTime.of(13, 32, 12, 0), ZoneId.of("Europe/Berlin")).toEpochSecond() * 1_000 | Date | new Date(1606653132000L)
    
    inputTypeName = PlasticUtils.toTypeName(input.getClass())
    typeName = PlasticUtils.toTypeName(type)
  }

  @Unroll
  def "explain #fromName to #toName should be #expected"() {

    expect:

    coercer.explain(from, to) == expected

    where:

    from         | to         | expected
    StringBuffer | Integer    | "Object --> String, String --> Integer"
    void         | Map        | "null --> null"
    void         | Boolean    | "null --> Boolean"
    Object[]     | Boolean    | "Object[] --> Boolean"
    String[]     | List       | "Object[] --> java.util.List"
    Float        | Double     | "Float --> Double"
    Double       | BigDecimal | "Object --> String, String --> java.math.BigDecimal"
    int          | Integer    | ""
    Integer      | Integer    | ""

    fromName = PlasticUtils.toTypeName(from)
    toName = PlasticUtils.toTypeName(to)
  }

  def "no coercion yields the input value unchanged"() {
    def input = new Integer(-37)

    expect:

    coercer.coerce(input, Integer).is(input)
  }

  def "coercions for primitive types and wrappers are the same"() {
    when:

    def c1 = coercer.getCoercion(int, Integer)
    def c2 = coercer.getCoercion(Integer, Integer)

    then:

    c1.is c2
  }

  def "exception when no coercion is found"() {
    when:

    coercer.coerce("", Map)

    then:

    RuntimeException e = thrown()

    e.message.contains "Could not find a coercion from type java.lang.String to type java.util.Map"
  }

  def "exception when a coercion fails"() {
    when:

    coercer.coerce([:], Float)

    then:

    RuntimeException e = thrown()

    e.message.contains "Coercion of {} to type java.lang.Float (via Object --> String, String --> Double, Double --> Float) failed"
    e.cause instanceof NumberFormatException
  }

  def "computed coercions are cached"() {
    def c1 = coercer.getCoercion(StringBuilder, Integer)
    def c2 = coercer.getCoercion(StringBuilder, Integer)

    expect:

    c1.is(c2)

    when:

    coercer.clearCache()
    def c3 = coercer.getCoercion(StringBuilder, Integer)

    then:

    !c1.is(c3)
  }
}
