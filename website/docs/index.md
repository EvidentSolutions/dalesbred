# Dalesbred

Dalesbred is a brave little database library that believes SQL is a great tool for working with relational databases, but JDBC is too painful to use directly. It provides a thin layer over JDBC that makes common operations concise and eliminates boilerplate while still giving you full control over your SQL.

- **GitHub**: [https://github.com/EvidentSolutions/dalesbred](https://github.com/EvidentSolutions/dalesbred)
- **Maven Central**: [org.dalesbred:dalesbred](https://central.sonatype.com/artifact/org.dalesbred/dalesbred)
- **Javadoc**: [https://dalesbred.org/docs/api/](https://dalesbred.org/docs/api/)

## Quick example

```java
Database db = Database.forUrlAndCredentials("jdbc:postgresql://localhost/mydb", "user", "pass");

List<Department> departments =
    db.findAll(Department.class, "select id, name from department order by name");
```

Where `Department` is just a plain class with a matching constructor:

```java
public final class Department {
    private final int id;
    private final String name;

    public Department(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
```

No annotations, no XML, no generated code. See the [Basic Usage](basic-usage.md) page to get started.
