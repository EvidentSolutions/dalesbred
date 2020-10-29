Dalesbred
=========

Dalesbred is a breed of domestic sheep originating in the United Kingdom.

It is also a library that strives to make database access from Java nicer.
Dalesbred assumes that SQL is a great way to access database, but that JDBC
as an API causes pain. Therefore it wraps JDBC with a set of helpers
while still providing access to low-level functionality.

Visit the [Dalesbred website](https://dalesbred.org/) for details.

[![Build Status](https://teamcity.evident.fi/app/rest/builds/buildType:(id:Evident_Dalesbred_Build)/statusIcon)](https://teamcity.evident.fi/viewType.html?buildTypeId=Evident_Dalesbred_Build&guest=1)

Quick-start
===========

Add dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.dalesbred</groupId>
    <artifactId>dalesbred</artifactId>
    <version>1.3.3</version>
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

Improve your experience with Kotlin and bundled extension methods:

```kotlin
val departments = db.findAll<Department>("""
    select id, name 
      from department
      where name like ?""", 
    "%foo")
```

Documentation
=============

  - [Reference Documentation](https://dalesbred.org/docs/reference/)
  - [Javadoc](https://dalesbred.org/docs/api/)

IDEA-integration
================

If you're using [IntelliJ IDEA](https://www.jetbrains.com/idea/), check out
[Dalesbred IDEA Plugin](https://github.com/EvidentSolutions/dalesbred-idea-plugin),
which provides inspections for common errors (e.g. mismatch between query parameters
and query).
