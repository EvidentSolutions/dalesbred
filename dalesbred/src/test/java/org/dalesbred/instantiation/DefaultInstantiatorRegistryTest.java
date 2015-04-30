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

package org.dalesbred.instantiation;

import org.dalesbred.annotation.DalesbredIgnore;
import org.dalesbred.annotation.Reflective;
import org.dalesbred.dialect.DefaultDialect;
import org.dalesbred.instantiation.test.InaccessibleClassRef;
import org.dalesbred.internal.utils.TypeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class DefaultInstantiatorRegistryTest {

    private final DefaultInstantiatorRegistry instantiatorRegistry = new DefaultInstantiatorRegistry(new DefaultDialect());

    @Test
    public void everyClassIsAssignableFromItself() {
        assertAssignable(int.class, int.class);
        assertAssignable(Integer.class, Integer.class);
        assertAssignable(Object.class, Object.class);
        assertAssignable(String.class, String.class);
    }

    @Test
    public void primitivesAreAssignableFromWrappers() {
        assertAssignable(int.class, Integer.class);
        assertAssignable(long.class, Long.class);
    }

    @Test
    public void wrappersAreAssignableFromPrimitives() {
        assertAssignable(Integer.class, int.class);
        assertAssignable(Long.class, long.class);
    }

    @Test
    public void findDefaultConstructor() {
        TestClass result = instantiate(TestClass.class, NamedTypeList.builder(0).build());
        assertNotNull(result);
        assertThat(result.calledConstructor, is(1));
    }

    @Test
    public void findConstructedBasedOnType() {
        TestClass result = instantiate(TestClass.class, String.class, "foo");
        assertNotNull(result);
        assertThat(result.calledConstructor, is(2));
    }

    @Test
    public void findBasedOnPrimitiveType() {
        TestClass result = instantiate(TestClass.class, int.class, 3);
        assertNotNull(result);
        assertThat(result.calledConstructor, is(3));
    }

    @Test
    public void findPrimitiveTypedConstructorWithBoxedType() {
        TestClass result = instantiate(TestClass.class, Integer.class, 3);
        assertNotNull(result);
        assertThat(result.calledConstructor, is(3));
    }

    @Test(expected = InstantiationException.class)
    public void findingInstantiatorForInaccessibleClassThrowsNiceException() {
        instantiate(InaccessibleClassRef.INACCESSIBLE_CLASS, int.class, 3);
    }

    @Test(expected = InstantiationException.class)
    public void findingInstantiatorForInaccessibleConstructorThrowsNiceException() {
        instantiate(InaccessibleConstructor.class, int.class, 3);
    }

    @Test
    public void extraFieldsCanBeSpecifiedWithSettersAndFields() {
        NamedTypeList types = NamedTypeList.builder(3).add("arg", String.class).add("propertyWithAccessors", String.class).add("publicField", String.class).build();

        TestClass result = instantiate(TestClass.class, types, "foo", "bar", "baz");
        assertNotNull(result);
        assertThat(result.calledConstructor, is(2));
        assertThat(result.getPropertyWithAccessors(), is("bar"));
        assertThat(result.publicField, is("baz"));
    }

    @Test
    public void instantiationListenerForReflectionInstantiator() {
        List<Object> instantiatedObjects = new ArrayList<>();

        instantiatorRegistry.addInstantiationListener(instantiatedObjects::add);

        TestClass result = instantiate(TestClass.class, Integer.class, 3);
        assertNotNull(result);
        assertThat(instantiatedObjects, is(Collections.<Object>singletonList(result)));
    }

    @Test
    public void instantiationListenerForConversionInstantiator() {
        List<Object> instantiatedObjects = new ArrayList<>();

        instantiatorRegistry.addInstantiationListener(instantiatedObjects::add);

        Integer result = instantiate(Integer.class, Integer.class, 3);
        assertNotNull(result);
        assertThat(instantiatedObjects, is(Collections.<Object>singletonList(result)));
    }

    @Test
    public void instantiationUsingCustomInstantiator() {
        instantiatorRegistry.registerInstantiator(Integer.class, arguments -> arguments.getValues().get(0).toString().length());

        assertThat(instantiate(Integer.class, String.class, "foobar"), is(6));
    }

    @Test(expected = InstantiationException.class)
    public void dontUseIgnoredConstructor() {
        instantiate(TestClass.class, createNamedTypeList(int.class, int.class), 0, 0);
    }

    public static class TestClass {
        private final int calledConstructor;

        @Reflective
        public String publicField = "";

        private String propertyWithAccessors = "";

        @Reflective
        public TestClass() { calledConstructor = 1; }

        @Reflective
        public TestClass(String s) { calledConstructor = 2; }

        @Reflective
        public TestClass(int x) { calledConstructor = 3; }

        @SuppressWarnings("unused")
        @DalesbredIgnore
        public TestClass(int x, int y) { calledConstructor = 4; }

        public String getPropertyWithAccessors() {
            return propertyWithAccessors;
        }

        @Reflective
        public void setPropertyWithAccessors(String propertyWithAccessors) {
            this.propertyWithAccessors = propertyWithAccessors;
        }
    }

    @Nullable
    private <T,V> T instantiate(@NotNull Class<T> cl, @NotNull Class<V> type, V value) {
        return instantiate(cl, createNamedTypeList(type), value);
    }

    @Nullable
    private <T> T instantiate(Class<T> cl, NamedTypeList namedTypeList, Object... values) {
        Instantiator<T> instantiator = instantiatorRegistry.findInstantiator(cl, namedTypeList);
        InstantiatorArguments arguments = new InstantiatorArguments(namedTypeList, asList(values));
        return instantiator.instantiate(arguments);
    }

    @NotNull
    private static NamedTypeList createNamedTypeList(@NotNull Class<?>... types) {
        NamedTypeList.Builder list = NamedTypeList.builder(types.length);
        for (int i = 0; i < types.length; i++)
            list.add("name" + i, types[i]);
        return list.build();
    }

    private static void assertAssignable(@NotNull Class<?> target, @NotNull Class<?> source) {
        assertThat(TypeUtils.isAssignable(target, source), is(true));
    }

    public static class InaccessibleConstructor {

        @Reflective
        InaccessibleConstructor(int x) { }
    }
}
