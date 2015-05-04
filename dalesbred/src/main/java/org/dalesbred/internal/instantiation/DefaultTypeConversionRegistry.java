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

import org.dalesbred.conversion.TypeConversion;
import org.dalesbred.conversion.TypeConversionRegistry;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Optional;

/**
 * The used implementation of TypeConversionRegistry.
 */
final class DefaultTypeConversionRegistry implements TypeConversionRegistry {

    private final ConversionMap loadConversions = new ConversionMap();
    private final ConversionMap storeConversions = new ConversionMap();

    @NotNull
    public Optional<TypeConversion<?,?>> findCoercionFromDbValue(@NotNull Type source, @NotNull Type target) {
        return loadConversions.findConversion(source, target);
    }

    @NotNull
    public Optional<TypeConversion<?,?>> findCoercionToDb(@NotNull Type type) {
        return storeConversions.findConversion(type, Object.class);
    }

    @Override
    public void registerConversionFromDatabaseType(@NotNull TypeConversion<?, ?> conversion) {
        loadConversions.register(conversion);
    }

    @Override
    public void registerConversionToDatabaseType(@NotNull TypeConversion<?, ?> conversion) {
        storeConversions.register(conversion);
    }
}
