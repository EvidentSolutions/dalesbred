Dalesbred
=========

Dalesbred is a breed of domestic sheep originating in the United Kingdom.

It is also a library that strives to make database access from Java nicer.
Dalesbred assumes that SQL is a great way to access database, but that JDBC
as an API causes pain. Therefore it wraps JDBC with a set of helpers
while still providing access to low-level functionality.

Visit the [Dalesbred website](http://dalesbred.org/) for details.

[![Build Status](https://drone.io/github.com/EvidentSolutions/dalesbred/status.png)](https://drone.io/github.com/EvidentSolutions/dalesbred/latest)

Quick-start
===========

Add dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.dalesbred</groupId>
    <artifactId>dalesbred</artifactId>
    <version>1.0.0</version>
</dependency>
```

Create a class with public fields, normal JavaBean accessors or a constructor matching your SQL-query. For example:

```java
public class Department {
    public int id;
    public String name;
}
```

Create a database connection:

```java
Database db = Database.forUrlAndCredentials("jdbc:example-url", "login", "password");
```

Fetch matching rows from table:

```java
List<Department> departments = db.findAll(Department.class,
        "select id, name from department where name like ?", "%foo");
```

Insert a new row:

```java
db.update("insert into user (id, name) values (?, ?)", 42, "Example User");
```

Documentation
=============

  - [Reference Documentation](http://dalesbred.org/docs/reference/)
  - [Javadoc](http://dalesbred.org/docs/api/)

IDEA-integration
================

If you're using [IntelliJ IDEA](https://www.jetbrains.com/idea/), check out
[Dalesbred IDEA Plugin](https://github.com/EvidentSolutions/dalesbred-idea-plugin),
which provides inspections for common errors (e.g. mismatch between query parameters
and query).

Attributions
============

Image of dalesbred used on the website is by [NicePics on Flickr](http://www.flickr.com/photos/48235612@N00/338947492)
and is used by [CC BY-SA 2.0](http://creativecommons.org/licenses/by-sa/2.0/).
