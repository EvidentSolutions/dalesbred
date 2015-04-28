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

import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class DatabaseReflectiveInstantiationTest {

    private final Database db = TestDatabaseProvider.createInMemoryHSQLDatabase();

    @Rule
    public final TransactionalTestsRule rule = new TransactionalTestsRule(db);

    @Test
    public void fetchSimpleFieldsAndProperties() {
        MyResult result = db.findUnique(MyResult.class, "select 42, 43 as field, 44 as property from (values (1))");

        assertThat(result.constructor, is(42));
        assertThat(result.field, is(43));
        assertThat(result.getProperty(), is(44));
    }

    @Test
    public void fieldsAndPropertiesWithoutMatchingCase() {
        MyResult result = db.findUnique(MyResult.class, "select 42, 43 as fiELD, 44 as proPERty from (values (1))");

        assertThat(result.constructor, is(42));
        assertThat(result.field, is(43));
        assertThat(result.getProperty(), is(44));
    }

    @Test
    public void constructorBindingWithNullValuesAndConversions() {
        ConstructorNeedingConversion result = db.findUnique(ConstructorNeedingConversion.class, "values (cast(null as datetime))");
        assertThat(result.dateTime, is(nullValue()));
    }

    @Test
    public void fieldBindingNullValuesAndConversions() {
        FieldNeedingConversion result = db.findUnique(FieldNeedingConversion.class, "select (cast(null as datetime)) as date_time from (values (1))");
        assertThat(result.dateTime, is(nullValue()));
    }

    @Test
    public void setterBindingNullValuesAndConversions() {
        SetterNeedingConversion result = db.findUnique(SetterNeedingConversion.class, "select (cast(null as datetime)) as date_time from (values (1))");
        assertThat(result.getDateTime(), is(nullValue()));
    }

    public static class MyResult {
        public final int constructor;

        @Reflective
        public int field;
        public int propertyBackingField;

        public MyResult(int constructor) {
            this.constructor = constructor;
        }

        @Reflective
        public void setProperty(int property) {
            this.propertyBackingField = property;
        }

        public int getProperty() {
            return propertyBackingField;
        }
    }

    public static class ConstructorNeedingConversion {
        private final DateTime dateTime;

        public ConstructorNeedingConversion(DateTime dateTime) {
            this.dateTime = dateTime;
        }
    }

    public static class FieldNeedingConversion {

        @Reflective
        public DateTime dateTime;
    }

    public static class SetterNeedingConversion {
        private DateTime dateTime;

        public DateTime getDateTime() {
            return dateTime;
        }

        @Reflective
        public void setDateTime(DateTime dateTime) {
            this.dateTime = dateTime;
        }
    }
}
