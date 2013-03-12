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

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class NamedParameterValueProvidersTest {

    @Test
    public void testProviderForMap() {
        Map<String, Object> parameterMap = new HashMap<String, Object>();

        Object foo = new Object();
        Object bar = new Object();
        Object baz = new Object();

        parameterMap.put("foo", foo);
        parameterMap.put("bar", bar);
        parameterMap.put("baz", baz);

        NamedParameterValueProvider namedParameterValueProvider = NamedParameterValueProviders.providerForMap(parameterMap);

        assertEquals(foo, namedParameterValueProvider.getValue("foo"));
        assertEquals(bar, namedParameterValueProvider.getValue("bar"));
        assertEquals(baz, namedParameterValueProvider.getValue("baz"));
    }

    @Test
    public void testProviderForBean() {

        TestBean bean = new TestBean();
        NamedParameterValueProvider namedParameterValueProvider = NamedParameterValueProviders.providerForBean(bean);

        assertEquals(bean.foo, namedParameterValueProvider.getValue("foo"));
        assertEquals(bean.bar, namedParameterValueProvider.getValue("bar"));
        assertEquals(bean.baz, namedParameterValueProvider.getValue("baz"));
    }

    @SuppressWarnings({"UnusedDeclaration", "FieldMayBeFinal"})
    private static class TestBean {
        public Object foo = new Object();
        private boolean bar = true;
        private String baz = "qwerty";

        public boolean isBar() {
            return bar;
        }

        public String getBaz() {
            return baz;
        }
    }
}
