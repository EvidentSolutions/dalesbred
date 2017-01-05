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

import org.dalesbred.datatype.SqlArray;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DatabaseArraysTest {

    private final Database db = TestDatabaseProvider.createPostgreSQLDatabase();

    @Rule
    public final TransactionalTestsRule rule = new TransactionalTestsRule(db);

    @Test
    public void databaseArraysAsPrimitiveArrays() {
        assertThat(db.findUnique(int[].class, "values (cast ('{1,5,3}' as numeric array))"), is(new int[] { 1, 5, 3 }));
        assertThat(db.findUnique(long[].class, "values (cast ('{1,6,3}' as numeric array))"), is(new long[] { 1, 6, 3 }));
        assertThat(db.findUnique(short[].class, "values (cast ('{1,6,3}' as numeric array))"), is(new short[] { 1, 6, 3 }));
    }

    @Test
    public void databaseArraysAsWrapperArrays() {
        assertThat(db.findUnique(Integer[].class, "values (cast ('{1,5,3}' as numeric array))"), is(new Integer[] { 1, 5, 3 }));
        assertThat(db.findUnique(Long[].class, "values (cast ('{1,6,3}' as numeric array))"), is(new Long[] { 1L, 6L, 3L }));
        assertThat(db.findUnique(Short[].class, "values (cast ('{1,6,3}' as numeric array))"), is(new Short[]{1, 6, 3}));
    }

    @Test
    public void databaseArraysForBigNumbers() {
        assertThat(db.findUnique(BigInteger[].class, "values (cast ('{1,5,3}' as numeric array))"), is(new BigInteger[]{BigInteger.valueOf(1), BigInteger.valueOf(5L), BigInteger.valueOf(3L)}));
        assertThat(db.findUnique(BigDecimal[].class, "values (cast ('{1,5,3}' as numeric array))"), is(new BigDecimal[]{BigDecimal.valueOf(1), BigDecimal.valueOf(5L), BigDecimal.valueOf(3L)}));
    }

    @Test
    public void databaseArraysForStrings() {
        assertThat(db.findUnique(String[].class, "values (cast ('{foo,bar,baz}' as varchar array))"), is(new String[] { "foo", "bar", "baz" }));
    }

    @Test
    public void databaseArraysAsLists() {
        ListContainer object = db.findUnique(ListContainer.class, "select (cast ('{1,5,3}' as numeric array)) as intList, (cast ('{foo, bar, baz}' as varchar array)) as stringList");
        assertThat(object.intList, is(asList(1, 5, 3)));
        assertThat(object.stringList, is(asList("foo", "bar", "baz")));
    }

    @Test
    public void databaseArraysAsCollections() {
        CollectionContainer object = db.findUnique(CollectionContainer.class, "select (cast ('{1,5,3}' as numeric array)) as intCollection, (cast ('{foo, bar, baz}' as varchar array)) as stringCollection");
        assertThat(object.intCollection, is(asList(1, 5, 3)));
        assertThat(object.stringCollection, is(asList("foo", "bar", "baz")));
    }

    @Test
    public void databaseArraysAsSets() {
        SetContainer object = db.findUnique(SetContainer.class, "select (cast ('{1,5,3}' as numeric array)) as intSet, (cast ('{foo, bar, baz}' as varchar array)) as stringSet");
        assertThat(object.intSet, is(setOf(1, 5, 3)));
        assertThat(object.stringSet, is(setOf("foo", "bar", "baz")));
    }

    @Test
    public void bindArray() {
        db.update("drop table if exists array_test");
        db.update("create table array_test (string_array VARCHAR array)");

        db.update("insert into array_test (string_array) values (?)", SqlArray.varchars("foo", "bar"));

        assertThat(db.findUnique(String[].class, "select string_array from array_test"), is(new String[] { "foo", "bar" }));
    }

    @SuppressWarnings("unused")
    public static final class ListContainer {
        public List<Integer> intList;
        public List<String> stringList;
    }

    @SuppressWarnings("unused")
    public static final class SetContainer {
        public Set<Integer> intSet;
        public Set<String> stringSet;
    }

    @SuppressWarnings("unused")
    public static final class CollectionContainer {
        public Collection<Integer> intCollection;
        public Collection<String> stringCollection;
    }

    @NotNull
    @SafeVarargs
    private static <T> Set<T> setOf(T... items) {
        return new HashSet<>(asList(items));
    }
}
