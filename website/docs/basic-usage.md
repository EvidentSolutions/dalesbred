## Basic usage

### Configuring the database connection

Most things in Dalesbred happen through an instance of [Database](https://dalesbred.org/docs/api/org/dalesbred/Database.html).
It takes care of managing JDBC connections, so in a typical application you should only configure a single instance --
unless you need to connect multiple databases. The easiest way to get hold of one is to specify the settings manually:

```java
Database db = Database.forUrlAndCredentials("jdbc:example-url", "login", "password");
```

Note that this performs no connection pooling and is therefore probably not
your preferred way of configuring the system in production. In a container
you'll probably want to lookup a named [DataSource](https://docs.oracle.com/javase/8/docs/api/javax/sql/DataSource.html) from JNDI:

```java
Database db = Database.forJndiDataSource("java:comp/env/jdbc/ExampleDb");
```

Alternatively, you might setup a [DataSource](https://docs.oracle.com/javase/8/docs/api/javax/sql/DataSource.html) yourself, in which case you can
just create a [Database](https://dalesbred.org/docs/api/org/dalesbred/Database.html) out of that:

```java
Database db = Database.forDataSource(myDataSource);
```

If you are using [Spring Framework](https://spring.io/projects/spring-framework), see the
[Spring](integrations.md#spring) section on how to integrate Dalesbred with it.

### Finding stuff

Running queries that return basic types is straightforward:

```java
List<Integer> newIds = db.findAll(Integer.class,
    "select id from department where created_date > ?", date);
```

There are a couple of ways to fetch results with multiple columns. First, you could just create a matching constructor:

```java
List<Department> departments =
    db.findAll(Department.class, "select id, name from department");

public final class Department {
    private final int id;
    private final String name;

    @DalesbredInstantiator
    public Department(int id, String name) {
        this.id = id;
        this.name = name;
    }

    ...
}
```

!!! note
    The [DalesbredInstantiator](https://dalesbred.org/docs/api/org/dalesbred/annotation/DalesbredInstantiator.html) annotation for
    constructor is optional, but helps Dalesbred to make an unambiguous decision when there are multiple constructors.
    It also serves as useful documentation. Finally, it can be configured as an entry-point for static analyzers so
    they don't complain about unused constructor.

Instead of constructor, you can also use `@DalesbredInstantiator` on a static method that returns an instance of the
class.

The second option is to bind values using fields or setters. The following example uses the default constructor for
instantiation, field-binding for `id` and setter for `name`:

```java
List<Department> departments =
    db.findAll(Department.class, "select id, name from department");

...

public final class Department {
    public int id;
    private String name;

    public void setName(String name) {
        this.name = name;
    }
}
```

If you have nested objects, you can bind to them as well as long as all objects in the path are instantiated:

```java
List<Employee> employees =
    db.findAll(Employee.class, "select id, first_name as \"name.first\", last_name as \"name.last\" from employee");

...

public final class Employee {
    public int id;
    public final Name name = new Name();
}

public final class Name {
    public String first;
    public String last;
}
```

You can also convert the results directly to a map:

```java
Map<Integer, String> namesByIds = db.findMap(
        Integer.class, String.class, "select id, name from department");

// first column is used for key, rest for instantiating the value
Map<Integer, Department> departmentsByIds = db.findMap(
        Integer.class, Department.class, "select id, id, name from department");
```

If for some reason you don't want to map the results into your own class, you can ask for
a [ResultTable](https://dalesbred.org/docs/api/org/dalesbred/result/ResultTable.html), which is basically a detached
representation of a [ResultSet](https://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html):

```java
ResultTable employees = db.findTable("select * from employee");
```

Alternatively, you can supply your own [RowMapper](https://dalesbred.org/docs/api/org/dalesbred/result/RowMapper.html) or
[ResultSetProcessor](https://dalesbred.org/docs/api/org/dalesbred/result/ResultSetProcessor.html)-implementation in place
of the class and handle the result sets manually, but usually this should be unnecessary.

### Updates

Normal updates are straightforward, since we don't need to do much work to map the results:

```java
int modifiedRows = db.update("delete from user where id=?", 42);
```

If you plan to return stuff from updates, they are queries as far as Dalesbred is concerned:

```java
int id = db.findUniqueInt("insert into department (name) values ('foo') returning id");
```
