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

package fi.evident.dalesbred;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static fi.evident.dalesbred.utils.Require.requireNonNull;

public final class TransactionSettings {

    private Propagation propagation = Propagation.REQUIRED;
    private Isolation isolation;
    private int retries = 0;

    @NotNull
    public Propagation getPropagation() {
        return propagation;
    }

    public void setPropagation(@NotNull Propagation propagation) {
        this.propagation = requireNonNull(propagation);
    }

    @Nullable
    public Isolation getIsolation() {
        return isolation;
    }

    public void setIsolation(@Nullable Isolation isolation) {
        this.isolation = isolation;
    }

    public int getRetries() {
        return retries;
    }

    /**
     * Sets the maximum amount of automatic retries if transaction fails due to serialization issues.
     * Default value is 0, meaning no retries.
     */
    public void setRetries(int retries) {
        if (retries < 0) throw new IllegalArgumentException("negative retries: " + retries);

        this.retries = retries;
    }

    @Override
    public String toString() {
        return "[propagation=" + propagation + ", isolation=" + isolation + "]";
    }
}
