## Migration from 0.x to 1.0

Dalesbred 1.0 is not source compatible with 0.x because of the following changes:

- Changed package structure.
- Some less commonly used APIs were simplified.
- A few obscure APIs were completely eliminated.

However, the migration should be a relatively simple process. This guide provides the details.

### New package structure

Base package is now `org.dalesbred` instead of `fi.evident.dalesbred`. Furthermore some classes were
moved to new packages (e.g. `fi.evident.dalesbred.SqlQuery` is now `org.dalesbred.query.SqlQuery`).
All the changes are not listed here -- let your IDE's autocomplete fix the imports.

Classes that are not part of public API, but need to be `public` for technical reasons are placed
under `org.dalesbred.internal`. Do not use classes defined there since they are not guaranteed
to stay compatible between releases.

### Java 1.8

Dalesbred now requires Java 1.8. This enables some nice improvements such as enabling use of
lambdas and `Optional<T>` in APIs.

### Removed instantiators

`Instantiator`, `InstantiatorRegistry` and related classes and methods were completely removed since they
were seldom used and added too much complication for their weight. Instead of registering an instantiator for `Foo`
globally, you can simply use a custom `ResultSetProcessor` in queries for `Foo`.

### Simplified type-conversion registration

`TypeConversion` is no longer part of public API and you don't need to implement it to register
type-conversions. Instead, you can register normal functions as conversions using `TypeConversionRegistry`.

### Enum binding changes

`Database` no longer has `EnumMode` for setting globally how enum types are persisted. Rather you can
customize the persistence through `TypeConversionRegistry` just like for any other types. By default
enums are persisted as varchars using their constant names. (This corresponds to the previous `EnumMode.NAME`
functionality.)

Previously enums on PostgreSQL were persisted as native enums in the database using a database enum type that was
guessed based on the class name of the enum. So if your enum class was named `FooBar`, you needed to have
an enum type named `foo_bar` in the database. Now they are persisted as varchars using constant names by default,
but you can use the previous functionality by calling
`typeConversionRegistry.registerNativeEnumConversion(FooBar.class, "foo_bar")`

### Transaction management changes

- Made `TransactionContext` an interface instead of abstract class.
- Removed support for configuring default propagation. Default propagation is now always `REQUIRED`, but you
  can override this on a per-transaction basis.
- Removed support for configuring default isolation level. If you need to set
  the default isolation, set it for the connections at your connection-provider.
- `@Transactional` annotation and `TransactionalProxyFactory` along with `Database.createTransactionalProxyFor`
  were removed, since the functionality really belongs to the runtime environment instead of Dalesbred. If you
  really need the functionality, copy [TransactionalProxyFactory of Dalesbred 0.8](https://github.com/EvidentSolutions/dalesbred/blob/0.x/dalesbred/src/main/java/fi/evident/dalesbred/support/proxy/TransactionalProxyFactory.java).
- Removed support for transaction retries. They were hardly useful and not supported for Spring integration.

### Guice and AOP Alliance support removed

Dalesbred no longer ships with out-of-the-box support for Guice and AOP Alliance. If you need the support,
you can port the classes from the 0.x branch:

- [fi.evident.dalesbred.support.aopalliance](https://github.com/EvidentSolutions/dalesbred/tree/0.x/dalesbred/src/main/java/fi/evident/dalesbred/support/aopalliance)
- [fi.evident.dalesbred.support.guice](https://github.com/EvidentSolutions/dalesbred/tree/0.x/dalesbred/src/main/java/fi/evident/dalesbred/support/guice)

### Renamed classes

- Renamed `InstantiationException` to `InstantiationFailureException` because the former
  was already used in `java.lang`.

### Moved functionality

- Changed `ResultTable.getColumnTypes` and `ResultTable.ColumnMetadata.getType` to return `Type`-objects
  instead of `Class<?>`-objects. Previous functionality is available with `ResultTable.getRawColumnTypes`
  and `ResultTable.ColumnMetadata.getRawType`.
- Removed `VariableResolvers`-class and moved its `resolverForMap` to `VariableResolver.forMap` and `resolverForBean`
  to `VariableResolver.forBean`.
- Removed `NamedParameterQueries.namedQuery`. Use `SqlQuery.namedQuery` instead.
- Removed `fi.evident.dalesbred.connection.DriverManagerDataSourceProvider`.
  New `org.dalesbred.connection.DriverManagerConnectionProvider` can be used instead.

### Removed functionality

- Removed `getCount` from `NonUniqueResultException` so that it is possible to throw the exception
  without reading all rows from the database. If you really need to have the count, you can ask for
  a list of results and check for uniqueness yourself.
- Removed `SqlQuery.confidential`. Some database drivers will print the values passed to
  the database in exceptions anyway, so the only safe way to make sure that values are not
  revealed inadvertently is not to show exceptions at all.
- Moved `fi.evident.dalesbred.Reflective` to test folder so that it's not visible in the API.
  Dalesbred did not use it for anything and it doesn't really make sense for Dalesbred to
  include such an annotation. You can always define one yourself if you need one in your project.
- Removed `fi.evident.dalesbred.instantiation.InstantiationListener` completely. This was hardly
  ever used. If you need to perform initialization on objects returned by Dalesbred, you can
  always do it yourself.
- Removed `Database.getDialect`. There should not be a need to access the Dialect directly.
