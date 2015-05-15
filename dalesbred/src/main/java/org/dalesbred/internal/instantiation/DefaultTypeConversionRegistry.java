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

import org.dalesbred.conversion.TypeConversionRegistry;
import org.dalesbred.dialect.Dialect;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;

/**
 * The used implementation of TypeConversionRegistry.
 */
final class DefaultTypeConversionRegistry implements TypeConversionRegistry {

    @NotNull
    private final Dialect dialect;

    @NotNull
    private final ConversionMap loadConversions = new ConversionMap();

    @NotNull
    private final ConversionMap storeConversions = new ConversionMap();

    public DefaultTypeConversionRegistry(@NotNull Dialect dialect) {
        this.dialect = dialect;
    }

    @Override
    public <T extends Enum<T>> void registerEnumConversion(@NotNull Class<T> enumType, @NotNull Function<T, ?> keyFunction) {
        registerConversionFromDatabase(Object.class, enumType, value -> {
            for (T enumConstant : enumType.getEnumConstants()) {
                if (value.equals(keyFunction.apply(enumConstant)))
                    return enumConstant;
            }

            throw new IllegalArgumentException("could not find enum constant of type " + enumType.getName() + " for " + value);
        });

        registerConversionToDatabase(enumType, keyFunction::apply);
    }

    @Override
    public <T extends Enum<T>> void registerNativeEnumConversion(@NotNull Class<T> enumType, @NotNull String typeName) {
        registerConversions(Object.class, enumType,
                dialect.createNativeEnumFromDatabaseConversion(enumType),
                dialect.createNativeEnumToDatabaseConversion(typeName)::apply);
    }

    @NotNull
    public Optional<TypeConversion> findCoercionFromDbValue(@NotNull Type source, @NotNull Type target) {
        return loadConversions.findConversion(source, target);
    }

    @NotNull
    public Optional<TypeConversion> findConversionToDb(@NotNull Type type) {
        return storeConversions.findConversion(type, Object.class);
    }

    @Override
    public <S, T> void registerConversionFromDatabase(@NotNull Class<S> source, @NotNull Class<T> target, @NotNull Function<S, T> conversion) {
        loadConversions.register(source, target, TypeConversion.fromNonNullFunction(conversion));
    }

    @Override
    public <S> void registerConversionToDatabase(@NotNull Class<S> source, @NotNull Function<S, ?> conversion) {
        storeConversions.register(source, Object.class, TypeConversion.fromNonNullFunction(conversion));
    }
}
