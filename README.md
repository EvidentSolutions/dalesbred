Dalesbred
=========

Dalesbred is a breed of domestic sheep originating in the United Kingdom.

It is also a library that strives to make database access from Java nicer.
Dalesbred assumes that SQL is a great way to access database, but that JDBC
as an API causes pain. Therefore it wraps JDBC with a set of helpers
while still providing access to low-level functionality.

Get started
===========

Configuring the database connection
-----------------------------------

Most things in Dalesbred happen through an instance of _Database_. The easiest
way to get hold of one is to specify the settings manually:

    :::java
    Database db = Database.forUrlAndCredentials("jdbc:example-url", "login", "password");

Note that this performs no connection pooling and is therefore probably not
your preferred way of configuring the system in production. In a container
you'll probably want to use a named DataSource lookup up from JNDI:

    :::java
    Database db = Database.forJndiDataSource("java:comp/env/jdbc/ExampleDb");

Alternatively, you might setup a DataSource yourself, in which case you can
just create a Database out of that:

    :::java
    Database db = Database.forDataSource(myDataSource);

Finally, for  _javax.inject_-compatible dependency injection container (e.g. Guice
or Java EE 6), you can just make sure that there's a
[Provider](http://docs.oracle.com/javaee/6/api/javax/inject/Provider.html)
for DataSource around and then just inject your Database:

    :::java
    @Inject Database db;

Finding stuff
-------------

Finding simple results consisting of just basic types is simple:

    :::java
    List<Integer> newIds = db.findAll(Integer.class,
            "select id from department where created_date > ?", date);

To fetch results with multiple columns, you need a class with matching constructor:

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

You can also convert the results directly to a map:

    :::java
    Map<Integer,String> namesByIds = db.findMap(
            Integer.class, String.class, "select id, name from department");

If for some reason you don't want to map the results into your own class, you
can ask for a ResultTable, which is basically a detached representation of a
ResultSet:

    :::java
    ResultTable employees = db.findTable("select * from employee");

Alternatively, you can supply your own RowMapper or ResultSetProcessor-implementations
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

To perform a bunch of operations in transaction, use TransactionCallback:

    :::java
    db.withTransaction(new TransactionCallback<Result>() {
        public Result execute(TransactionContext tx) throws SQLException {
            // transactional operations
            ...
        });
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

If you are using Guice, Dalesbred can integrate with its interceptor support, see section below.

Guice-integration
-----------------

Dalesbred has support for integration with Guice 3. You can just pass in DataSourceDatabaseModule
or DriverManagerConnectionModule when constructing your injector and you'll get automatic support
for annotation based transactions and can @Inject your database wherever you need it.

    :::java
    Injector injector = Guice.createInjector(new DataSourceDatabaseModule(), new MyOtherModule());

See the Javadoc of the modules to see what how they are configured.


SqlQuery vs. query parameters
-----------------------------

All methods come in two variants: there's an implementation that takes
an SqlQuery as a parameter and there's another implementation that takes
String and variable arguments of parameters. The latter is just convenience
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

Building queries dynamically
----------------------------

At the moment there's no high-level API for building queries, but there is a QueryBuilder that
is basically just a StringBuilder which remembers the query-parameters, so you can say things like:

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
you can register your own TypeConversion-implementations to TypeConversionRegistry:

    :::java
    TypeConversionRegistry conversions = db.getTypeConversionRegistry();
    conversions.registerConversionFromDatabaseType(new StringToEmailAddressConversion());
    conversions.registerConversionToDatabaseType(new EmailAddressToStringConversion());

There are built-in conversions from Joda Time's DateTime, LocalDate and LocalTime to java.sql.Timestamp,
java.sql.Date and java.sql.Time, respectively. These will be automatically registered if Joda Time is
detected on classpath.

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
        <version>0.2.3</version>
    </dependency>

Attributions
============

Image of dalesbred used on the website is by [NicePics on Flickr](http://www.flickr.com/photos/48235612@N00/338947492)
and is used by [CC BY-SA 2.0](http://creativecommons.org/licenses/by-sa/2.0/).
