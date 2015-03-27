## 0.7.1 (2015-03-27)

  - Delegate `PreparedStatement`-binding to `Dialect`s.  
  - Added `@DalesbredIgnore` for ignoring constructors, fields and setters.

## 0.7.0 (2014-07-03)

  - Added support for ThreeTen, the backport of java.time.
  - Support for retrieving generated keys.
  - Use `ResultSetMetadata.getColumnLabel` instead of `ResultSetMetadata.getColumnName` to fetch
    names of columns in results as the latter didn't use column aliases on some databases.
    (Thanks to Christoph Gritschenberger.)
  - Added OSGi-headers to jar. (Thanks to Christoph Gritschenberger.)
  - Added SingleConnectionTransactionManager for third-party integration.
  - Migrated the build to use Gradle instead of Maven. (Shouldn't affect users in any way.)
  - Updated versions of the optional dependencies.
  - Minimum supported JDK version is now 1.6.

## 0.6.0 (2014-01-04)

  - Support java.time when running on Java 8.
  - Added withVoidTransaction method family along with VoidTransactionCallback
  - Added adapter for using Dalesbred's own @Transactional annotations with Spring.

## 0.5.4 (2013-10-23)

  - Fixed building Joda LocalDates from database timestamps.

## 0.5.3 (2013-10-23)

  - Fixed building Joda LocalDates from database dates.
  - Support adding sub-queries to QueryBuilder.

## 0.5.2 (2013-09-26)

  - Support native XML-types of databases.
  - Support converting InputStreams/Readers to and from blobs/clobs.
  - Fixed handling null values in conjunction with conversions on reflective bindings.

## 0.5.1 (2013-09-14)

  - Improved initialization hooks for Spring DalesbredConfigurationSupport.
  - Support configuring the way that enums are stored to database.
  - Fixed handling of null result values in MapResultSetProcessor.

## 0.5.0 (2013-09-02)

  - Integration with Spring Framework.
  - Support for pluggable transaction management strategies.
  - Added dialects for Oracle and MySQL.
  - Support converting Clobs to Strings and Blobs to byte-arrays.

## 0.4.0 (2013-03-17)

  - Support for named parameters in SQL-queries.
  - Remove underscores from database column names when mapping them to properties (ie. 'foo_bar' matches 'fooBar').

## 0.3.2 (2013-01-21)

  - Since many databases will uppercase or lowercase column names, ignore the case differences in names when finding
    setters or fields matching the result-set.
  - Removed required dependency to javax.inject. Now Dalesbred has no required dependencies to third-party libraries.

## 0.3.1 (2012-12-07)

  - Support for batch updates.
  - Log execution time of queries.
  - Changed most of the tests to use in-memory HSQLDB.
  - Added dialect for HSQLDB.
  - Added builtin conversions for java.util.TimeZone and org.joda.time.DateTimeZone.
  - Allow type conversions to be registered for interfaces in addition to concrete types.

## 0.3.0 (2012-11-22)

  - Support registering custom instantiators for types.
  - Added support for InstantiationListeners, which receive notifications whenever instantiators create new objects.
  - When configuring the system with Guice, newly instantiated objects will automatically receive Guice injections
    to their members.
  - More flexible reflection instantiation: if result has more column than constructor has parameters, the
    instantiator will try to set the extra columns using setters or direct fields access.
  - Pass more information about the results to instantiators.

## 0.2.5 (2012-11-14)

  - Added support for confidential query parameters, which never show up in logs.
  - Exceptions thrown when executing a query will reference the query that caused the exception to be thrown.
  - Changed the way that Isolation and Propagation are configured, so that the
    default isolation and propagation can be specified at Database-level even
    when using annotations.
  - Support for converting Transactional-annotation into TransactionSettings.
  - Support passing Provider<Database> instead of Database when creating TransactionalTests.

## 0.2.4 (2012-11-05)

  - Support for user defined type-conversions.
  - Optional Guice-support.
  - Optional support for AOP Alliance interceptors.
  - Bug fix: release InitialContext properly when looking up DataSource by JNDI-name.
  - Various small improvements.
  - Added this changelog.
