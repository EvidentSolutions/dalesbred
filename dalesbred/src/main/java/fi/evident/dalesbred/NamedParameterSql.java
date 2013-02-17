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

package fi.evident.dalesbred;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

final class NamedParameterSql {

    private final String originalSql;
    private final String traditionalSql;
    private final List<String> namedParameters;

    NamedParameterSql(@NotNull String originalSql, @NotNull String traditionalSql, @NotNull List<String> namedParameters) {
        this.originalSql = originalSql;
        this.traditionalSql = traditionalSql;
        this.namedParameters = namedParameters;
    }

    @NotNull
    List<?> toParameterValues(@NotNull NamedParameterValueProvider parameterSource) throws IllegalArgumentException {
        List<Object> result = new ArrayList<Object>(namedParameters.size());

        for (String namedParameter : namedParameters) {
            try {
                result.add(parameterSource.getValue(namedParameter));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("No value supplied for the SQL parameter '" + namedParameter + '\'', e);
            }
        }
        return result;
    }

    @NotNull
    String getTraditionalSql() {
        return traditionalSql;
    }

    @NotNull
    public List<String> getNamedParameters() {
        return namedParameters;
    }

    @Override
    public String toString() {
        return originalSql;
    }
}