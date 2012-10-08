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

Making queries
--------------

    List<Integer> ids = db.findAll(query("select id from department"), Integer.class);

    List<Department> departments = db.findAll(
        query("select id, name from department where manager_id=?", managerId), Department.class);

See the example project for full examples.
