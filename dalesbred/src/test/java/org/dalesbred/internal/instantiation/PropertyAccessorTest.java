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

package org.dalesbred.internal.instantiation;

import org.dalesbred.annotation.DalesbredIgnore;
import org.dalesbred.annotation.Reflective;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class PropertyAccessorTest {

    @Test
    public void findingSetters() {
        assertThat(PropertyAccessor.findAccessor(DepartmentWithSetters.class, "department_name"), is(not(Optional.empty())));
    }

    @Test
    public void findingFields() {
        assertThat(PropertyAccessor.findAccessor(DepartmentWithFields.class, "department_name"), is(not(Optional.empty())));
    }

    @Test
    public void ignoredSetters() {
        assertThat(PropertyAccessor.findAccessor(IgnoredValues.class, "ignoredMethod"), is(Optional.empty()));
    }

    @Test
    public void ignoredFields() {
        assertThat(PropertyAccessor.findAccessor(IgnoredValues.class, "ignoredField"), is(Optional.empty()));
    }

    @Test
    public void nestedPathsWithIntermediateFields() {
        PropertyAccessor accessor = PropertyAccessor.findAccessor(NestedPaths.class, "namedField.name").orElse(null);

        assertNotNull(accessor);

        NestedPaths paths = new NestedPaths();
        accessor.set(paths, "foo");

        assertThat(paths.namedField.name, is("foo"));
    }

    @Test
    public void nestedPathsWithIntermediateGetters() {
        PropertyAccessor accessor = PropertyAccessor.findAccessor(NestedPaths.class, "namedGetter.name").orElse(null);

        assertNotNull(accessor);

        NestedPaths paths = new NestedPaths();
        accessor.set(paths, "foo");

        assertThat(paths.getNamedGetter().name, is("foo"));
    }

    @Test(expected = InstantiationFailureException.class)
    public void nestedPathsWithNullFields() {
        PropertyAccessor accessor = PropertyAccessor.findAccessor(NestedPaths.class, "nullField.name").orElse(null);

        assertNotNull(accessor);

        accessor.set(new NestedPaths(), "foo");
    }

    @Test(expected = InstantiationFailureException.class)
    public void nestedPathsWithNullGetters() {
        PropertyAccessor accessor = PropertyAccessor.findAccessor(NestedPaths.class, "nullGetter.name").orElse(null);

        assertNotNull(accessor);

        accessor.set(new NestedPaths(), "foo");
    }

    @Test
    public void invalidPathElements() {
        assertThat(PropertyAccessor.findAccessor(Named.class, "foo.name"), is(Optional.empty()));
    }

    private static class DepartmentWithFields {
        @Reflective
        public String departmentName;
    }

    public static class DepartmentWithSetters {
        private String departmentName;

        @Reflective
        public String getDepartmentName() {
            return departmentName;
        }

        @Reflective
        public void setDepartmentName(String departmentName) {
            this.departmentName = departmentName;
        }
    }

    public static class NestedPaths {
        public final Named namedField = new Named();

        @Reflective
        public final Named nullField = null;

        @Reflective
        public Named getNamedGetter() {
            return namedField;
        }

        @Nullable
        @Reflective
        public Named getNullGetter() {
            return null;
        }
    }

    public static class Named {
        @Reflective
        public String name;
    }

    @SuppressWarnings("unused")
    public static class IgnoredValues {

        @DalesbredIgnore
        public String ignoredField;

        @SuppressWarnings({"UnusedParameters", "EmptyMethod"})
        @DalesbredIgnore
        public void setIgnoredMethod(String s) {
        }
    }
}
