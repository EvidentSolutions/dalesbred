## Queries

### SqlQuery vs. query parameters

All methods come in two variants: there's an implementation that takes
an [SqlQuery](https://dalesbred.org/docs/api/org/dalesbred/query/SqlQuery.html) as a parameter and another
implementation that takes a [String](https://docs.oracle.com/javase/8/docs/api/java/lang/String.html) and a variable number of parameters.
The latter is just a convenience method for the former, meaning that the following code fragments are
identical in functionality:

```java
import static org.dalesbred.query.SqlQuery.query;

SqlQuery query = query("select id, name from department where update_timestamp > ?", date);
db.findAll(Department.class, query);

db.findAll(Department.class,
    "select id, name from department where update_timestamp > ?", date);
```

Normally you want to use the latter form, but every once in a while it's
useful to be able to pass the query around with its parameters. In those
cases you'd want to use the first form. An example is when you build
the query dynamically:

```java
db.findAll(Department.class, buildDepartmentQuery(form));
```

### Named queries

In addition to using positional parameters in your SQL statements, you can also use named parameters:

```java
import static org.dalesbred.query.SqlQuery.namedQuery;

Map<String,Object> values = new HashMap<>();
values.put("firstName", "John");
values.put("lastName", "Doe");

db.findAll(Department.class, namedQuery("select id from employee " +
                                        " where first_name = :firstName " +
                                        "   and last_name = :lastName", values));
```

Instead of [Map](https://docs.oracle.com/javase/8/docs/api/java/util/Map.html)s, you can also pass just regular objects to `namedQuery` as
the source of values. The parameter names are mapped to properties or fields of the objects. Finally, if you want
detailed control, you can pass your own implementation of
[VariableResolver](https://dalesbred.org/docs/api/org/dalesbred/query/VariableResolver.html) to resolve the variables.

### Building queries dynamically

There's no high-level API for building queries, but [QueryBuilder](https://dalesbred.org/docs/api/org/dalesbred/query/QueryBuilder.html)
helps you construct dynamic queries. It's basically just a [StringBuilder](https://docs.oracle.com/javase/8/docs/api/java/lang/StringBuilder.html)
that also keeps track of parameters. Therefore you can write code like:

```java
QueryBuilder qb = new QueryBuilder("select id, name, status from document");
if (status != null)
    qb.append(" where status=?", status);

db.findAll(Document.class, qb.build());
```

!!! note
    The benefit of using static queries is that IDEA and the Dalesbred IDEA plugin know how to analyze them: they can
    be validated against the database schema and result classes. When building queries dynamically, you lose these
    benefits. Consider building a higher level abstraction on top of QueryBuilder if you need many dynamic queries.
