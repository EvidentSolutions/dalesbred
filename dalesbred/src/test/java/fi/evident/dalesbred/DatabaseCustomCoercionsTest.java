package fi.evident.dalesbred;

import fi.evident.dalesbred.instantiation.TypeConversion;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;

import java.util.regex.Pattern;

import static fi.evident.dalesbred.utils.Require.requireNonNull;
import static org.junit.Assert.assertEquals;

public class DatabaseCustomCoercionsTest {

    private final Database db = TestDatabaseProvider.createInMemoryHSQLDatabase();

    @Rule
    public final TransactionalTestsRule transactionalTests = new TransactionalTestsRule(db);

    @Test
    public void customLoadConversions() {
        db.getTypeConversionRegistry().registerConversionFromDatabaseType(new StringToEmailTypeConversion());

        assertEquals(new EmailAddress("user", "example.org"), db.findUnique(EmailAddress.class, "values ('user@example.org')"));
    }

    @Test
    public void customSaveConversions() {
        db.getTypeConversionRegistry().registerConversionToDatabaseType(new EmailToStringTypeConversion());

        db.update("drop table if exists custom_save_conversions_test");
        db.update("create temporary table custom_save_conversions_test (email varchar(32))");

        db.update("insert into custom_save_conversions_test (email) values (?)", new EmailAddress("user", "example.org"));

        assertEquals("user@example.org", db.findUnique(String.class, "select email from custom_save_conversions_test"));
    }

    private static class StringToEmailTypeConversion extends TypeConversion<String, EmailAddress> {
        private static final Pattern AT_SIGN = Pattern.compile("@");

        public StringToEmailTypeConversion() {
            super(String.class, EmailAddress.class);
        }

        @NotNull
        @Override
        public EmailAddress convert(@NotNull String value) {
            String[] parts = AT_SIGN.split(value);
            if (parts.length == 2)
                return new EmailAddress(parts[0], parts[1]);
            throw
                new IllegalArgumentException("invalid address: '" + value + '\'');
        }
    }

    private static class EmailToStringTypeConversion extends TypeConversion<EmailAddress, String> {
        public EmailToStringTypeConversion() {
            super(EmailAddress.class, String.class);
        }

        @NotNull
        @Override
        public String convert(@NotNull EmailAddress value) {
            return value.toString();
        }
    }

    public static class EmailAddress {

        @NotNull
        private final String user;

        @NotNull
        private final String host;

        // This constructor has two parameters so that the reflection mechanism can't coerce the type automatically.
        public EmailAddress(@NotNull String user, @NotNull String host) {
            this.user = requireNonNull(user);
            this.host = requireNonNull(host);
        }

        @NotNull
        @Override
        public String toString() {
            return user + '@' + host;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;

            if (obj instanceof EmailAddress) {
                EmailAddress rhs = (EmailAddress) obj;
                return user.equals(rhs.user) && host.equals(rhs.host);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return user.hashCode() * 31 + host.hashCode();
        }
    }
}
