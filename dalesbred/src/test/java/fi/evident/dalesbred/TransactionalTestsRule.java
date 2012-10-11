package fi.evident.dalesbred;

import org.jetbrains.annotations.NotNull;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.sql.SQLException;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

final class TransactionalTestsRule implements TestRule {

    private final Database db;

    public TransactionalTestsRule(@NotNull Database db) {
        this.db = requireNonNull(db);
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Throwable throwable =
                    db.withTransaction(new TransactionCallback<Throwable>() {
                        @Override
                        public Throwable execute(@NotNull TransactionContext tx) throws SQLException {
                            try {
                                base.evaluate();
                                return null;
                            } catch (Throwable throwable) {
                                tx.setRollbackOnly();
                                return throwable;
                            }
                        }
                    });
                if (throwable != null)
                    throw throwable;
            }
        };
    }
}
