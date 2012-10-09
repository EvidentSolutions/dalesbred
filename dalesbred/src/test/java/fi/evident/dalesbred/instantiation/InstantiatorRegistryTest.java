package fi.evident.dalesbred.instantiation;

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

    @Test
    public void preferConstructorWithoutBoxing() throws Exception {
        Instantiator<TestClass> ctor = findInstantiator(TestClass.class, long.class);
        assertThat(ctor.instantiate(new Object[] { 3L }).calledConstructor, is(4));
    }

    @Test
    public void preferConstructorWithoutUnboxing() throws Exception {
        Instantiator<TestClass> ctor = findInstantiator(TestClass.class, Long.class);
        assertThat(ctor.instantiate(new Object[] { 3L }).calledConstructor, is(5));
    }

    @SuppressWarnings("unused")
    static class TestClass {
        private final int calledConstructor;
        public TestClass() { calledConstructor = 1; }
        public TestClass(String s) { calledConstructor = 2; }
        public TestClass(int x) { calledConstructor = 3; }
        public TestClass(long x) { calledConstructor = 4; }
        public TestClass(Long x) { calledConstructor = 5; }
    }

    private <T> Instantiator<T> findInstantiator(Class<T> cl, Class<?>... types) throws NoSuchMethodException {
        NamedTypeList.Builder list = NamedTypeList.builder(types.length);
        for (int i = 0; i < types.length; i++)
            list.add("name" + i, types[i]);

        return instantiatorRegistry.findInstantiator(cl, list.build());
    }

    private static void assertAssignable(Class<?> target, Class<?> source) {
        assertThat(isAssignableByBoxing(target, source), is(true));
    }
}
