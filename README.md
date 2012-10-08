Dalesbred
=========

Dalesbred is a breed of domestic sheep originating in the United Kingdom.
It is also a library that strives to make database access from Java nicer.
Dalesbred assumes that SQL is a great way to access database, but that JDBC
as an API is causes pain. Therefore it wraps JDBC with a set of helpers
while still providing access to low-level functionality if necessary.

Getting started
---------------

Setup a database connection:

    Database db = Database.forUrlAndCredentials(
        "jdbc:postgresql://example-host/example-db", "login", "password");

For container provided data source:

    Database db = Database.forJndiDataSource("java:comp/env/jdbc/ExampleDb"")

For other data source:

    Database db = Database.forDataSource(myDataSource);

For _javax.inject_-compatible dependency injection container (e.g. Guice), you
can just make sure that there's a _Provider\<DataSource\>_ around and then just @Inject
Database where you need it.

Finding stuff
-------------

Finding simple results consisting of just basic types is simple:

    List<Integer> newIds = db.findAll(Integer.class, "select id from department where created_date > ?", date);

To fetch results with multiple columns, you need a class with matching constructor:

    List<Department> departments = db.findAll(Department.class, "select id, name from department);

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

Alternatively, you can supply your own RowMapper or ResultSetProcessor-implementations in place
of the class and handle the result sets manually, but usually this should be unnecessary.

See the example project and test cases for more examples.

TODO
----

- optional support for JodaTime
- JUnit test case support
