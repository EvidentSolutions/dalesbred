Dalesbred
=========

Dalesbred is a breed of domestic sheep originating in the United Kingdom.

It is also a library that strives to make database access from Java nicer.
Dalesbred assumes that SQL is a great way to access database, but that JDBC
as an API causes pain. Therefore it wraps JDBC with a set of helpers
while still providing access to low-level functionality.

[![Build Status](https://drone.io/bitbucket.org/evidentsolutions/dalesbred/status.png)](https://drone.io/bitbucket.org/evidentsolutions/dalesbred/latest)

Quick-start
===========

Add dependency to your `pom.xml`:

    :::xml
    <dependency>
        <groupId>fi.evident.dalesbred</groupId>
        <artifactId>dalesbred</artifactId>
        <version>0.6.0</version>
    </dependency>

Create a class with public fields, normal JavaBean accessors or a constructor matching your SQL-query. For example:

    :::java
    public class Department {
        public int id;
        public String name;
    }

Create a database connection:

    :::java
    Database db = Database.forUrlAndCredentials("jdbc:example-url", "login", "password");

Fetch matching rows from table:

    List<Department> departments = db.findAll(Department.class,
            "select id, name from department where name like ?", "%foo");

Insert a new row:

    db.update("insert into user (id, name) values (?, ?)", 42, "Example User");

Documentation
-------------

  - [Reference Documentation](https://dalesbred.evident.fi/docs/current/reference/)
  - [Javadoc](https://dalesbred.evident.fi/docs/current/api/)

IDEA-integration
----------------

If you're using [IntelliJ IDEA](https://www.jetbrains.com/idea/), check out
[Dalesbred IDEA Plugin](https://bitbucket.org/evidentsolutions/dalesbred-idea-plugin),
which provides inspections for common errors (e.g. mismatch between query parameters
and query).

Attributions
============

Image of dalesbred used on the website is by [NicePics on Flickr](http://www.flickr.com/photos/48235612@N00/338947492)
and is used by [CC BY-SA 2.0](http://creativecommons.org/licenses/by-sa/2.0/).
