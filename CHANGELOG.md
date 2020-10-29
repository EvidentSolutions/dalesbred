## 1.3.3 (2020-10-29)

### Bug fixes

  - Fix query timeouts

## 1.3.2 (2020-04-14)

### Changes

Support passing primitive key and values types for `findMap`.

## 1.3.1 (2020-02-19)

### Changes
  
  - Add support for statement timeouts ([#45](https://github.com/EvidentSolutions/dalesbred/pull/45))
  - Upgraded the version of optional Kotlin dependency to 1.3.61.

## 1.3.0 (2018-08-01)

### New features

  - Add Java 9 module automatic module names for jars. ([#41](https://github.com/EvidentSolutions/dalesbred/issues/41))
  - Support pretty printing of `ResultTable`s 

### Changes

  - Updated the versions of optional dependencies.

### Bug fixes

  - Fix reference to `@DalesbredInstantiator` in exception message 

## 1.2.5 (2017-08-25)

### New features

  - Add optional fetch size and direction to SqlQuery ([#37](https://github.com/EvidentSolutions/dalesbred/pull/37))

## 1.2.4 (2017-06-05)

### New features

  - Add optional Kotlin-support; `org.dalesbred.integration.kotlin` package adds Kotlin extensions to `Database`.
  - Add `updateUnique` to `Database` ([#32](https://github.com/EvidentSolutions/dalesbred/issues/32))
  
## 1.2.3 (2016-12-01)

### New features

  - New convenience methods for `QueryBuilder`.

## 1.2.2 (2016-08-15)

### New features

  - Add `findOptionalInt`, `findOptionalLong` and `findOptionalDouble` methods to `Database`.

## 1.2.1 (2016-04-12)

### Bug fixes

  - `org.slf4j:slf4j-api` was marked as optional dependency in POM, although it's required. 

## 1.2.0 (2016-04-12)

### Changes

  - Use [SLF4J](https://www.slf4j.org/) instead of `java.util.logging` for logging ([#25](https://github.com/EvidentSolutions/dalesbred/issues/25))

## 1.1.0 (2015-11-27)

### New features

  - Allow annotating constructors with `@DalesbredInstantiator` to always use the
    annotated constructor instead of trying to detect constructor automatically.
    ([#19](https://github.com/EvidentSolutions/dalesbred/issues/19))
  - Support binding arrays on Oracle, which does not support standard JDBC API.
  - Add dialect for H2. ([#20](https://github.com/EvidentSolutions/dalesbred/issues/20))
  - Support property paths when binding (instead of just simple names). ([#7](https://github.com/EvidentSolutions/dalesbred/issues/7))

## 1.0.0 (2015-05-29)

### Changes

  - Relax `SqlArray.of` and `SqlArray.varchars` to accept any `Collection` instead of just `List`s.

## 1.0.0-rc.2 (2015-05-17)

### New features

  - Add support for registering custom key functions for native enums.
  - Fail faster if registering native enum conversion on Dialect that does not support native enums.

### Breaking changes

  - Removed support for Guice and AOP Alliance.
  - Removed `@DalesbredTransactional` and `TransactionalProxyFactory`.
  - Removed support for transaction retries.

## 1.0.0-rc.1 (2015-05-14)

### New features

  - Simplified API for registering enum conversions through `TypeConversionRegistry`. ([#14](https://github.com/EvidentSolutions/dalesbred/issues/14))

### Breaking changes

  - Renamed `InstantiationException` to `InstantiationFailureException` because the former
    was already used in `java.lang`.
  - Database native enums are no longer the default because they depended on ugly hack to 
    guess the database type name. By default enums are bound using name, but you can use
    methods in `TypeConversionRegistry` to customize this. 
  - Removed support for setting configuring default isolation level. If you need to set
    the default isolation, set it for the connections at your connection-provider.
  - Removed `Database.getDialect`. There should not be a need to access the Dialect directly.

## 1.0.0-alpha.4 (2015-05-08)

### New features

  - New `updateBatchAndProcessGeneratedKeys` for performing batch updates and returning generated keys. ([#9](https://github.com/EvidentSolutions/dalesbred/issues/9))
  - Guide for migration from 0.x. ([#6](https://github.com/EvidentSolutions/dalesbred/issues/6))

## 1.0.0-alpha.3 (2015-05-06)

### New features

  - Generalized `findMap` to allow more than one column for instantiating the value. ([#2](https://github.com/EvidentSolutions/dalesbred/issues/2))

### Breaking changes

  - Removed `InstantiatorRegistry`, `Instantiator` and related classes/methods from public API.
  - Moved `TypeConversionRegistry` to `org.dalesbred.conversion` and removed `TypedConversion` from
    public API. Register conversion functions to TypeConversionRegistry instead.
  - Removed `getCount` from `NonUniqueResultException` so that its possible to throw the exception 
    without reading all rows from database.
  - `ResultTable.getColumnTypes()` now returns `Type`-objects. New `ResultTable.getRawColumnTypes`
    returns raw `Class<?>` -objects. Similarly `getType` in `ResultTable.ColumnMetadata` now returns
    `Type` and raw class can be returned with `getRawType`.
  - Renamed `VariableResolvers.resolverForMap/resolverForBean` to `VariableResolver.forMap/forBean`.
  - Made `TransactionContext` an interface instead of abstract class.
  - Removed `Database.createTransactionalProxyFor`. Use `TransactionalProxyFactory.createTransactionalProxyFor`instead.
  - Removed accessors for isolation `Database`. `Database.getTransactionManager` can be used to access 
    `TransactionManager` for configuring these.
  - Removed support for configuring default propagation. Use propagation on per-transaction basis.

## 1.0.0-alpha.2 (2015-05-04)

  - Ignore underscores on Java fields and setters when resolving instantiators. (Fixes [#3](https://github.com/EvidentSolutions/dalesbred/issues/3))
  - Reverted functionality of `findUniqueOrNull` and `findOptional` to return null/empty when single null result is returned.

## 1.0.0-alpha.1 (2015-05-02)

First alpha for 1.0 -version of Dalesbred. This is not source compatible with previous versions, 
but unless you have been using really obscure features, just renaming packages in imports should be enough.

### New features

  - New family of `findOptional` -methods in `Database`, returning `java.util.Optional` values.
  - Added `EmptyResultException` as a subclass of `NonUniqueResultException` for case with zero results.

### Breaking changes

  - Renamed base package `fi.evident.dalesbred` to `org.dalesbred`.
  - Other package renames:
    - `fi.evident.dalesbred.dialects` -> `org.dalesbred.dialect`
    - `fi.evident.dalesbred.lob` -> `org.dalesbred.datatype`
    - `fi.evident.dalesbred.results` -> `org.dalesbred.result`
    - `fi.evident.dalesbred.support` -> `org.dalesbred.integration`
    - `fi.evident.dalesbred.tx` -> `org.dalesbred.transaction`
  - Moved some classes to new packages:
    - All annotations to `org.dalesbred.annotation`
    - All transaction-related classes to `org.dalesbred.transaction`.
    - `fi.evident.dalesbred.SqlQuery` -> `org.dalesbred.query.SqlQuery` 
    - `fi.evident.dalesbred.SqlArray` -> `org.dalesbred.datatype.SqlArray`
    - `fi.evident.dalesbred.ResultTable` -> `org.dalesbred.result.ResultTable`
    - `fi.evident.support.proxy.TransactionalProxyFactory` -> `org.dalesbred.transaction.TransactionalProxyFactory`
  - Moved classes that are technically `public`, but are not part of Dalesbred's supported 
    API to `org.dalesbred.internal`.
  - Removed `SqlQuery.confidential`. Some database drivers will print the values passed to
    database in exceptions anyway, so the only safe way to make sure that values are not
    revealed inadvertently is not to show exceptions at all.
  - Removed `NamedParameterQueries.namedQuery`. Use `SqlQuery.namedQuery` instead.
  - Moved `fi.evident.dalesbred.Reflective` to test folder so that it's not visible in API.
  - Renamed `fi.evident.dalesbred.Transactional` to `org.dalesbred.annotation.DalesbredTransactional`
    so that it does not clash with Spring's `@Transactional`
  - Removed `fi.evident.dalesbred.instantiation.InstantiationListener` completely.
  - Removed `fi.evident.dalesbred.connection.DriverManagerDataSourceProvider`. 
    New `org.dalesbred.connection.DriverManagerConnectionProvider` can be used instead.

## 0.8.0 (2015-04-08)

  - Support instantiating arrays and collections of instantiable types from database arrays.
  - Support binding collections as SQL arrays.

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
