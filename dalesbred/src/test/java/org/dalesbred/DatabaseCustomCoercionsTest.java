/*
 * Copyright (c) 2015 Evident Solutions Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.dalesbred;

import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;

import java.util.Objects;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;

public class DatabaseCustomCoercionsTest {

    private final Database db = TestDatabaseProvider.createInMemoryHSQLDatabase();

    @Rule
    public final TransactionalTestsRule transactionalTests = new TransactionalTestsRule(db);

    @Test
    public void customLoadConversions() {
        db.getTypeConversionRegistry().registerConversionFromDatabase(String.class, EmailAddress.class, EmailAddress::parse);

        assertEquals(new EmailAddress("user", "example.org"), db.findUnique(EmailAddress.class, "values ('user@example.org')"));
    }

    @Test
    public void customSaveConversions() {
        db.getTypeConversionRegistry().registerConversionToDatabase(EmailAddress.class, EmailAddress::toString);

        db.update("drop table if exists custom_save_conversions_test");
        db.update("create temporary table custom_save_conversions_test (email varchar(32))");

        db.update("insert into custom_save_conversions_test (email) values (?)", new EmailAddress("user", "example.org"));

        assertEquals("user@example.org", db.findUnique(String.class, "select email from custom_save_conversions_test"));
    }

    public static class EmailAddress {

        @NotNull
        private final String user;

        @NotNull
        private final String host;

        private static final Pattern AT_SIGN = Pattern.compile("@");

        // This constructor has two parameters so that the reflection mechanism can't coerce the type automatically.
        public EmailAddress(@NotNull String user, @NotNull String host) {
            this.user = requireNonNull(user);
            this.host = requireNonNull(host);
        }

        @NotNull
        public static EmailAddress parse(@NotNull String value) {
            String[] parts = AT_SIGN.split(value);
            if (parts.length == 2)
                return new EmailAddress(parts[0], parts[1]);
            throw
                    new IllegalArgumentException("invalid address: '" + value + '\'');
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
            return Objects.hash(user, host);
        }
    }
}
