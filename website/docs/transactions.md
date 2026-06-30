## Transactions

### Transaction callbacks

To perform a bunch of operations in a transaction, use [TransactionCallback](https://dalesbred.org/docs/api/?org/dalesbred/transaction/TransactionCallback.html)
or [VoidTransactionCallback](https://dalesbred.org/docs/api/?org/dalesbred/transaction/VoidTransactionCallback.html):

```java
db.withTransaction(tx -> {
    // transactional operations
    ...
    return result;
});

db.withVoidTransaction(tx -> {
    // transactional operations
    ...
});
```

Optionally, you can also pass [Isolation](https://dalesbred.org/docs/api/?org/dalesbred/transaction/Isolation.html) or
[Propagation](https://dalesbred.org/docs/api/?org/dalesbred/transaction/Propagation.html) for these calls.

### External transaction manager

If you are using [Spring Framework](https://spring.io/projects/spring-framework), Dalesbred can integrate with
Spring's transaction manager. Consult the [Spring](integrations.md#spring) section for details.

### Implicit transactions

If you make calls to [Database](https://dalesbred.org/docs/api/?org/dalesbred/Database.html) without an explicit transaction, by default
a new transaction is started for each call. You can disallow this: in this case exceptions are thrown for calls
without an active transaction:

```java
db.setAllowImplicitTransactions(false);
```

### Nested transactions

Nested transactions are supported if your database supports them:

```java
db.withTransaction(Propagation.NESTED, tx -> {
    ...
});
```
