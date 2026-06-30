## Integrations

### Java

Dalesbred provides built-in [type-conversions](miscellaneous.md#custom-type-conversions) for the following classes:

| Model type | Direction | Database type |
|------------|-----------|---------------|
| `java.net.URI` | <-> | `String` |
| `java.net.URL` | <-> | `String` |
| `java.util.TimeZone` | <-> | `String` |
| `Short`/`Integer`/`Long`/`Float`/`Double` | <- | `Number` |
| `BigInteger`/`BigDecimal` | <- | `Number` |
| `BigInteger` | -> | `BigDecimal` |
| `String`/`java.io.Reader` | <- | `Clob` |
| `byte[]`/`java.io.InputStream` | <- | `Blob` |
| `org.w3c.dom.Document` | <- | `SQLXML` |
| `java.time.Instant` | <-> | `Timestamp` |
| `java.time.LocalDateTime` | <-> | `Timestamp` |
| `java.time.LocalTime` | <-> | `Time` |
| `java.time.ZoneId` | <-> | `String` |
| `java.time.LocalDate` | <-> | `java.util.Date`/`java.sql.Date` |


### Kotlin

Dalesbred has no required dependencies on [Kotlin](https://kotlinlang.org/), but comes with a set of extension methods
to make Kotlin use nicer. Just import everything from `org.dalesbred.integration.kotlin` and you're good to go:

```kotlin
import org.dalesbred.integration.kotlin.*

...

fun findEmployees() = db.findAll<Employee>("""
      select id, name, salary
        from employee
      order by name, id
    """)
```

### Joda-Time

If [Joda-Time](http://www.joda.org/joda-time/) is detected on the classpath, Dalesbred will automatically
register [type-conversions](miscellaneous.md#custom-type-conversions) between Joda-Time's
[DateTime](https://www.joda.org/joda-time/apidocs/?org/joda/time/DateTime.html), [LocalDate](https://www.joda.org/joda-time/apidocs/?org/joda/time/LocalDate.html)
and [LocalTime](https://www.joda.org/joda-time/apidocs/?org/joda/time/LocalTime.html) to [java.sql.Timestamp](https://download.java.net/jdk8/docs/api/?java/sql/Timestamp.html),
[java.sql.Date](https://download.java.net/jdk8/docs/api/?java/sql/Date.html) and [java.sql.Time](https://download.java.net/jdk8/docs/api/?java/sql/Time.html).

### Spring

Dalesbred has support for integration with [Spring Framework](https://spring.io/projects/spring-framework)
and its transaction management. To integrate Dalesbred, create a configuration class inheriting from
[DalesbredConfigurationSupport](https://dalesbred.org/docs/api/org/dalesbred/integration/spring/DalesbredConfigurationSupport.html)
and specify beans for [DataSource](https://download.java.net/jdk8/docs/api/?javax/sql/DataSource.html) and
[PlatformTransactionManager](https://docs.spring.io/spring/docs/current/javadoc-api/?org/springframework/transaction/PlatformTransactionManager.html).
A minimal configuration would therefore be something like the following:

```java
@Configuration
@EnableTransactionManagement
public class MyDatabaseConfiguration extends DalesbredConfigurationSupport {

    @Bean
    public DataSource dataSource() {
        return new JndiDataSourceLookup().getDataSource("jdbc/my-database");
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }
}
```

After this you can inject [Database](https://dalesbred.org/docs/api/org/dalesbred/Database.html) normally in your beans.
