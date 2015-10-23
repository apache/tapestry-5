package ioc.specs

import org.apache.tapestry5.ioc.util.LocalizedNameGenerator
import spock.lang.Specification
import spock.lang.Unroll

class LocalizedNamesGeneratorSpec extends Specification {

  @Unroll
  def "Localized names for #path and #locale are '#expected'"() {

    when:

    LocalizedNameGenerator g = new LocalizedNameGenerator(path, locale)

    then:

    g.asList() == expected.tokenize()

    where:

    path            | locale                         | expected

    "basic.test"    | Locale.US                      | "basic_en_US.test basic_en.test basic.test"
    "noCountry.zap" | Locale.FRENCH                  | "noCountry_fr.zap noCountry.zap"
    "fred.foo"      | new Locale("en", "", "GEEK")   | "fred_en__GEEK.foo fred_en.foo fred.foo"
    "complete.lc"   | new Locale("en", "US", "GEEK") | "complete_en_US_GEEK.lc complete_en_US.lc complete_en__GEEK.lc complete_en.lc complete.lc"
    "context:/blah" | Locale.FRENCH                  | "context:/blah_fr context:/blah"
    "context:/blah" | new Locale("fr", "", "GEEK")   | "context:/blah_fr__GEEK context:/blah_fr context:/blah"
    "context:/blah" | new Locale("fr", "FR", "GEEK") | "context:/blah_fr_FR_GEEK context:/blah_fr_FR context:/blah_fr__GEEK context:/blah_fr context:/blah"

    // The double-underscore is correct, it's a kind of placeholder for the null country. JDK1.3 always converts the locale to upper case. JDK 1.4
    // does not. To keep this test happyt, we selected an all-uppercase locale.

  }
}
