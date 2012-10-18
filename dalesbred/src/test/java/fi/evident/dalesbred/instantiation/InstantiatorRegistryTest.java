/*
 * Copyright (c) 2012 Evident Solutions Oy
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

import fi.evident.dalesbred.Reflective;
import fi.evident.dalesbred.dialects.DefaultDialect;
import fi.evident.dalesbred.instantiation.test.InaccessibleClassRef;
import org.junit.Test;

import static fi.evident.dalesbred.utils.Primitives.isAssignableByBoxing;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class InstantiatorRegistryTest {

    private final InstantiatorRegistry instantiatorRegistry = new InstantiatorRegistry(new DefaultDialect());

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
    public void findDefaultConstructor() throws Exception {
        Instantiator<TestClass> ctor = findInstantiator(TestClass.class);
        assertThat(ctor.instantiate(new Object[] { }).calledConstructor, is(1));
    }

    @Test
    public void findConstructedBasedOnType() throws Exception {
        Instantiator<TestClass> ctor = findInstantiator(TestClass.class, String.class);
        assertThat(ctor.instantiate(new Object[] { "foo", }).calledConstructor, is(2));
    }

    @Test
    public void findBasedOnPrimitiveType() throws Exception {
        Instantiator<TestClass> ctor = findInstantiator(TestClass.class, int.class);
        assertThat(ctor.instantiate(new Object[] { 3 }).calledConstructor, is(3));
    }

    @Test
    public void findPrimitiveTypedConstructorWithBoxedType() throws Exception {
        Instantiator<TestClass> ctor = findInstantiator(TestClass.class, Integer.class);
        assertThat(ctor.instantiate(new Object[] { 3 }).calledConstructor, is(3));
    }

    @Test(expected = InstantiationException.class)
    public void findingInstantiatorForInaccessibleClassThrowsNiceException() {
        findInstantiator(InaccessibleClassRef.INACCESSIBLE_CLASS, int.class).instantiate(new Object[]{3});
    }

    @Test(expected = InstantiationException.class)
    public void findingInstantiatorForInaccessibleConstructorThrowsNiceException() {
        findInstantiator(InaccessibleConstructor.class, int.class).instantiate(new Object[] { 3 });
    }

    public static class TestClass {
        private final int calledConstructor;

        @Reflective
        public TestClass() { calledConstructor = 1; }

        @Reflective
        public TestClass(String s) { calledConstructor = 2; }

        @Reflective
        public TestClass(int x) { calledConstructor = 3; }
    }

    private <T> Instantiator<T> findInstantiator(Class<T> cl, Class<?>... types) {
        NamedTypeList.Builder list = NamedTypeList.builder(types.length);
        for (int i = 0; i < types.length; i++)
            list.add("name" + i, types[i]);

        return instantiatorRegistry.findInstantiator(cl, list.build());
    }

    private static void assertAssignable(Class<?> target, Class<?> source) {
        assertThat(isAssignableByBoxing(target, source), is(true));
    }

    public static class InaccessibleConstructor {

        @Reflective
        InaccessibleConstructor(int x) { }
    }
}
