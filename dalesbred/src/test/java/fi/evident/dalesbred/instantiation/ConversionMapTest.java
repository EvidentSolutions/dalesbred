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

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class ConversionMapTest {

    private final ConversionMap registry = new ConversionMap();

    @Test
    public void searchingForNotExistingItemReturnsNull() {
        assertNull(registry.findConversion(Integer.class, String.class));
    }

    @Test
    public void searchBasedOnExactMatch() {
        TypeConversion<Integer, String> conversion = dummyConversion(Integer.class, String.class);
        registry.register(conversion);

        assertSame(conversion, registry.findConversion(Integer.class, String.class));
    }

    @Test
    public void searchBasedOnResultCovariance() {
        TypeConversion<Integer, String> conversion = dummyConversion(Integer.class, String.class);
        registry.register(conversion);

        assertSame(conversion, registry.findConversion(Integer.class, Object.class));
    }

    @Test
    public void searchBasedOnParamContravariance() {
        TypeConversion<Number, String> conversion = dummyConversion(Number.class, String.class);
        registry.register(conversion);

        assertSame(conversion, registry.findConversion(Integer.class, String.class));
    }

    @Test
    public void primitivesAndWrappersAreConsideredSame() {
        TypeConversion<Integer, Long> conversion = dummyConversion(Integer.class, Long.class);
        registry.register(conversion);

        assertSame(conversion, registry.findConversion(int.class, long.class));
    }

    @Test
    public void sourceContravarianceOnInterfaces() {
        TypeConversion<CharSequence, Long> conversion = dummyConversion(CharSequence.class, Long.class);
        registry.register(conversion);

        assertSame(conversion, registry.findConversion(String.class, Long.class));
    }

    @NotNull
    private static <S, T> TypeConversion<S,T> dummyConversion(@NotNull Class<S> source, @NotNull Class<T> target) {
        return new TypeConversion<S, T>(source, target) {
            @NotNull
            @Override
            public T convert(@NotNull S value) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
