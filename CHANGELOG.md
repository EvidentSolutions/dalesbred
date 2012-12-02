## x.y.z (yyyy-mm-dd)

  - Support for batch updates.
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
