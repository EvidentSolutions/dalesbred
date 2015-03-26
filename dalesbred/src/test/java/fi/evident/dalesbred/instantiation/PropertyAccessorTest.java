/*
 * Copyright (c) 2013 Evident Solutions Oy
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

package fi.evident.dalesbred.instantiation;

import fi.evident.dalesbred.DalesbredIgnore;
import fi.evident.dalesbred.Reflective;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PropertyAccessorTest {

    @Test
    public void findingSetters() {
        assertNotNull(PropertyAccessor.findAccessor(DepartmentWithSetters.class, "department_name"));
    }

    @Test
    public void findingFields() {
        assertNotNull(PropertyAccessor.findAccessor(DepartmentWithFields.class, "department_name"));
    }

    @Test
    public void ignoredSetters() {
        assertNull(PropertyAccessor.findAccessor(IgnoredValues.class, "ignoredMethod"));
    }

    @Test
    public void ignoredFields() {
        assertNull(PropertyAccessor.findAccessor(IgnoredValues.class, "ignoredField"));
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

    @SuppressWarnings("unused")
    public static class IgnoredValues {

        @DalesbredIgnore
        public String ignoredField;

        @SuppressWarnings("UnusedParameters")
        @DalesbredIgnore
        public void setIgnoredMethod(String s) {
        }
    }
}
