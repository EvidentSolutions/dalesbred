package fi.evident.dalesbred.instantiation;

import fi.evident.dalesbred.Reflective;
import fi.evident.dalesbred.dialects.DefaultDialect;
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

    static class TestClass {
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
}
