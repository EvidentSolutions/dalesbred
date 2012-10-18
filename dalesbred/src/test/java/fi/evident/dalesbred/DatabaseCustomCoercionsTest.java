package fi.evident.dalesbred;

import fi.evident.dalesbred.instantiation.TypeConversionBase;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static fi.evident.dalesbred.utils.Require.requireNonNull;
import static org.junit.Assert.assertEquals;

public class DatabaseCustomCoercionsTest {

    private final Database db = TestDatabaseProvider.createTestDatabase();

    @Test
    public void customLoadConversions() {
        db.getTypeConversionRegistry().registerConversionFromDatabaseType(new StringToEmailTypeConversion());

        assertEquals(new EmailAddress("user", "example.org"), db.findUnique(EmailAddress.class, "select 'user@example.org'"));
    }

    @Test
    public void customSaveConversions() {
        db.getTypeConversionRegistry().registerConversionToDatabaseType(new EmailToStringTypeConversion());

        db.update("drop table if exists custom_save_conversions_test");
        db.update("create table custom_save_conversions_test (email varchar)");

        db.update("insert into custom_save_conversions_test (email) values (?)", new EmailAddress("user", "example.org"));
    }

    private static class StringToEmailTypeConversion extends TypeConversionBase<String, EmailAddress> {
        public StringToEmailTypeConversion() {
            super(String.class, EmailAddress.class);
        }

        @NotNull
        @Override
        public EmailAddress convert(@NotNull String value) {
            String[] parts = value.split("@");
            if (parts.length == 2)
                return new EmailAddress(parts[0], parts[1]);
            throw
                new IllegalArgumentException("invalid address: '" + value + "'");
        }
    }

    private static class EmailToStringTypeConversion extends TypeConversionBase<EmailAddress, String> {
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

        private final String user;
        private final String host;

        // This constructor has two parameters so that the reflection mechanism can't coerce the type automatically.
        public EmailAddress(@NotNull String user, @NotNull String host) {
            this.user = requireNonNull(user);
            this.host = requireNonNull(host);
        }

        @Override
        public String toString() {
            return user + "@" + host;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o instanceof EmailAddress) {
                EmailAddress rhs = (EmailAddress) o;
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
