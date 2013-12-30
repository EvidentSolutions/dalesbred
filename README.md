Dalesbred
=========

Dalesbred is a breed of domestic sheep originating in the United Kingdom.

It is also a library that strives to make database access from Java nicer.
Dalesbred assumes that SQL is a great way to access database, but that JDBC
as an API causes pain. Therefore it wraps JDBC with a set of helpers
while still providing access to low-level functionality.

[![Build Status](https://drone.io/bitbucket.org/evidentsolutions/dalesbred/status.png)](https://drone.io/bitbucket.org/evidentsolutions/dalesbred/latest)

Get started
===========

Configuring the database connection
-----------------------------------

Most things in Dalesbred happen through an instance of `Database`. The easiest
way to get hold of one is to specify the settings manually:

    :::java
    Database db = Database.forUrlAndCredentials("jdbc:example-url", "login", "password");

Note that this performs no connection pooling and is therefore probably not
your preferred way of configuring the system in production. In a container
you'll probably want to use a named `DataSource` lookup up from JNDI:

    :::java
    Database db = Database.forJndiDataSource("java:comp/env/jdbc/ExampleDb");

Alternatively, you might setup a `DataSource` yourself, in which case you can
just create a `Database` out of that:

    :::java
    Database db = Database.forDataSource(myDataSource);

Finding stuff
-------------

Finding simple results consisting of just basic types is simple:

    :::java
    List<Integer> newIds = db.findAll(Integer.class,
            "select id from department where created_date > ?", date);

To fetch results with multiple columns, usually you'd use a class with matching constructor:

    :::java
    List<Department> departments = db.findAll(Department.class,
            "select id, name from department");

    ...

    public final class Department {
        private final int id;
        private final String name;

        public Department(int id, String name) {
            this.id = id;
            this.name = name;
        }

        ...
    }

If there are more columns in the result-set than can be given to a constructor, then the rest of the
columns are set using properties or direct field access. So even the following would work:

    :::java
    List<Department> departments = db.findAll(Department.class,
            "select id, name from department");

    ...

    public final class Department {
        public int id;
        public String name;
    }

You can also convert the results directly to a map:

    :::java
    Map<Integer,String> namesByIds = db.findMap(
            Integer.class, String.class, "select id, name from department");

If for some reason you don't want to map the results into your own class, you
can ask for a `ResultTable`, which is basically a detached representation of a
`ResultSet`:

    :::java
    ResultTable employees = db.findTable("select * from employee");

Alternatively, you can supply your own `RowMapper` or `ResultSetProcessor`-implementation
in place of the class and handle the result sets manually, but usually this should
be unnecessary.

Updates
-------

Normal updates are straightforward, since we don't need to do much work to map the results:

    :::java
    int modifiedRows = db.update("delete from user where id=?", 42);

If you plan to return stuff from updates, they are queries as far as Dalesbred is concerned:

    :::java
    int id = db.findUniqueInt("insert into department (name) values ('foo') returning id");

Transactions
------------

To perform a bunch of operations in transaction, use `TransactionCallback` or `VoidTransactionCallback`:

    :::java
    db.withTransaction(new TransactionCallback<Result>() {
        public Result execute(TransactionContext tx) throws SQLException {
            // transactional operations
            ...
            return result;
        });
    });

    db.withVoidTransaction(new VoidTransactionCallback() {
        public void execute(TransactionContext tx) throws SQLException {
            // transactional operations
            ...
        });
    });

If you are using Java 8, you can use the lighter lambda syntax:

    :::java
    db.withTransaction(tx -> {
        // transactional operations
        ...
        return result;
    });

    db.withVoidTransaction(tx -> {
        // transactional operations
        ...
    });

If you make calls to Database without and explicit transaction, by default
a new transaction is started for each call, but you can disallow this, in
which case exceptions are thrown for calls without an active transaction:

    :::java
    db.setAllowImplicitTransactions(false);

Nested transactions are supported if your database supports them:

    :::java
    db.withTransaction(Propagation.NESTED, new TransactionCallback<Result>() { ... });

Annotation based transactions
-----------------------------

The above transaction mechanism is a decent building block for implementing higher
level abstractions, but it's quite verbose to use in Java. Therefore Dalesbred provides
a simple support for building transactional proxies for services:

    :::java
    public interface MyService {
         void frobnicate();
    }

    public class MyRealService implements MyService {

         @Transactional
         public void frobnicate() {
             ...
         }
    }

    ...

    MyService myService = db.createTransactionalProxyFor(MyService.class, new MyRealService());
    service.frobnicate(); // this call will have a transaction wrapped around it

If you are using Spring Framework or Guice, Dalesbred can integrated with them for transaction
management. Consult the _Integrations_ -section for details.

SqlQuery vs. query parameters
-----------------------------

All methods come in two variants: there's an implementation that takes
an `SqlQuery` as a parameter and there's another implementation that takes
`String` and variable arguments of parameters. The latter is just convenience
method for the further, meaning that the following code fragments are
identical in functionality:

    :::java
    import static fi.evident.dalesbred.SqlQuery.query;

    SqlQuery query = query("select id, name from department where update_timestamp > ?", date);
    db.findAll(Department.class, query);

    db.findAll(Department.class,
        "select id, name from department where update_timestamp > ?", date);

Normally you want to use the latter form, but every once in a while it's
useful to be able to pass the query around with its parameters. In those
cases you'd want to use the first form. An example is when you build
the query dynamically:

    :::java
    db.findAll(Department.class, buildDepartmentQuery(form));

Named queries
-------------

In addition to using positional parameters in your SQL statements, you can also you named parameters:

    :::java
    import static fi.evident.dalesbred.SqlQuery.namedQuery;

    Map<String,Object> values = new HashMap<String,Object>();
    values.put("firstName", "John");
    values.put("lastName", "Doe");

    SqlQuery query = namedQuery("select id from employee where first_name = :firstName and last_name = :lastName", values);
    db.findAll(Department.class, query);

Instead of Maps, you can also pass just regular objects to namedQuery as the source of values. The parameter names
are mapped to properties or fields of the objects. Finally, if you want detailed control, you can pass your own
implementation of `VariableResolver` to resolve the variables.

Building queries dynamically
----------------------------

At the moment there's no high-level API for building queries, but there is a `QueryBuilder` that
is basically just a `StringBuilder` which remembers the query-parameters, so you can say things like:

    :::java
    QueryBuilder qb = new QueryBuilder("select id, name, status from document");
    if (status != null)
        qb.append(" where status=?", status);

    db.findAll(Document.class, qb.build());

For all but simplest dynamic queries, you'll probably want to have a higher level API that understands
the structure of the SQL.

Custom type-conversions
-----------------------

Sometimes you need to convert database values to your own custom types and vice versa. To do that,
you can register your own `TypeConversion`-implementations to `TypeConversionRegistry`:

    :::java
    TypeConversionRegistry conversions = db.getTypeConversionRegistry();
    conversions.registerConversionFromDatabaseType(new StringToEmailAddressConversion());
    conversions.registerConversionToDatabaseType(new EmailAddressToStringConversion());

There are built-in conversions from Joda Time's `DateTime`, `LocalDate` and `LocalTime` to `java.sql.Timestamp`,
`java.sql.Date` and `java.sql.Time`, respectively. These will be automatically registered if Joda Time is
detected on classpath.

When running on Java 8, conversions for `Instant`, `LocalDate`, `LocalDateTime`, `LocalTime`and `ZoneId` from
`java.time`-package are automatically registered.

Large objects
-------------

You can stream large objects (blobs and clobs) to database by just passing `InputStream` or `Reader`
to query. Similarly you can read them by asking back for `InputStream` or `Reader`.

    :::java
    try (InputStream in = new FileInputStream(name)) {
        db.update("insert into my_file (name, contents) values (?,?)", name, in);
    }

    try (InputStream in = db.findUnique(InputStream.class, "select contents from my_file where name=?", name)) {
        ...
    }

Note that the returned `InputStream` or `Reader` is only valid for the duration of the active transaction.

Confidential values
-------------------

Dalesbred tries to provide detailed exceptions and logs which include all the parameters of queries. While
this is generally useful, it could mean that sensitive information such as passwords or credit card numbers
might end up in logs or error messages. When building a query, you can wrap such values with `SqlQuery.confidential`
so that only asterisks will be displayed whenever those values are printed, but they are still sent to database
correctly:

    :::java
    import static fi.evident.dalesbred.SqlQuery.confidential;

    ...

    db.update("insert into credit_card (number) values (?)", confidential(creditCardNumber));

Test support
------------

By including the _dalesbred-junit_ artifact in your project as a test dependency,
you'll get support for writing transactional test cases:

    :::java
    public class MyTest {

        private final Database db = TestDatabaseProvider.databaseForProperties("testdb.properties");

        @Rule
        public final TransactionalTests tx = new TransactionalTests(db);

        @Test
        public void simpleTest() {
            assertEquals("hello, world!", db.queryForUnique(String.class "select 'hello, world!'");
        }
    }

Custom instantiators
--------------------

Sometimes you have objects that you can't instantiate using just constructors and setters, but you'd
still like to be able to build from results. You can register custom instantiators for such objects:

    :::java
    db.getInstantiatorRegistry().registerInstantiator(Foo.class, new Instantiator<Foo>() {
        @Override
        public Foo instantiate(@NotNull InstantiatorArguments arguments) {
            List<?> args = arguments.getValues();
            FooBuilder fooBuilder = new FooBuilder();
            fooBuilder.setBar(args.get(0));
            fooBuilder.setBaz(args.get(1));
            return fooBuilder.build();
        }
    });

Dalesbred will use this instantiator in place of the custom instantiator whenever it needs to build
results of type `Foo`.

InstantiationListeners
----------------------

You can configure a listener to receive notifications whenever Dalesbred creates new instances. The built-in
Guice-support uses this feature to wire the dependencies of newly created objects, but you can use this
callback anything you like:

    :::java
    db.getInstantiatorRegistry().addInstantiationListener(new InstantiationListener() {
        @Override
        public void onInstantiation(@NotNull Object object) {
            System.out.println("instantiated " + object);
        }
    });

Note that currently instantiation listeners are not called for objects instantiated by custom instantiators
registered by users. This limitation could be lifted in the future.

Integrations
============

Spring-integration
------------------

Dalesbred has support for integration with Spring Framework and its transaction management.
To integrate Dalesbred, create a configuration class inheriting from `DalesbredConfigurationSupport`
and specify beans for `DataSource` and `PlatformTransactionManager`. A minimal configuration would
therefore be something like the following:

    :::java

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

After this you can inject `Database` normally in your beans.

Guice-integration
-----------------

Dalesbred has support for integration with Guice 3. You can just pass in `DataSourceDatabaseModule`
or `DriverManagerDatabaseModule` when constructing your injector and you'll get automatic support
for annotation based transactions and can @Inject your database wherever you need it.

    :::java
    Injector injector = Guice.createInjector(new DataSourceDatabaseModule(), new MyOtherModule());

When using either of the Guice modules, you'll also get automatic support for using `@Inject` in the
results returned from database.

See the Javadoc of the modules for details of their configuration.

IDEA-integration
----------------

If you're using [IntelliJ IDEA](https://www.jetbrains.com/idea/), check out
[Dalesbred IDEA Plugin](https://bitbucket.org/evidentsolutions/dalesbred-idea-plugin),
which provides inspections for common errors (e.g. mismatch between query parameters
and query).

More examples
=============

Check out the test cases under _dalesbred/src/test/java_ for more usage examples.

Using with Maven
================

Dalesbred is available on the central Maven repository, so just add the following
dependency to your pom.xml:

    :::xml
    <dependency>
        <groupId>fi.evident.dalesbred</groupId>
        <artifactId>dalesbred</artifactId>
        <version>0.6.0-RC1</version>
    </dependency>

For the JUnit test-support classes, add the following:

    :::xml
    <dependency>
        <groupId>fi.evident.dalesbred</groupId>
        <artifactId>dalesbred-junit</artifactId>
        <version>0.6.0-RC1</version>
        <scope>test</scope>
    </dependency>

Using without Maven
===================

To use Dalesbred without Maven, you'll need to [download the latest jar for Dalesbred](https://bitbucket.org/evidentsolutions/dalesbred/downloads)
and it to your classpath. There are no other required dependencies.

Attributions
============

Image of dalesbred used on the website is by [NicePics on Flickr](http://www.flickr.com/photos/48235612@N00/338947492)
and is used by [CC BY-SA 2.0](http://creativecommons.org/licenses/by-sa/2.0/).
