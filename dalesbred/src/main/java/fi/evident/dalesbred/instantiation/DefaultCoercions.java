package fi.evident.dalesbred.instantiation;

import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

final class DefaultCoercions {

    public static void register(@NotNull Coercions coercions) {
        coercions.registerLoadConversion(new StringToUrlCoercion());
        coercions.registerLoadConversion(new StringToUriCoercion());

        coercions.registerStoreConversion(new ToStringCoercion<URL>(URL.class));
        coercions.registerStoreConversion(new ToStringCoercion<URI>(URI.class));
    }

    private static class StringToUrlCoercion extends CoercionBase<String,URL> {

        StringToUrlCoercion() {
            super(String.class, URL.class);
        }

        @NotNull
        @Override
        public URL coerce(@NotNull String value) {
            try {
                return new URL(value);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    private static class ToStringCoercion<S> extends CoercionBase<S,String> {

        ToStringCoercion(Class<S> source) {
            super(source, String.class);
        }

        @NotNull
        @Override
        public String coerce(@NotNull S value) {
            return value.toString();
        }
    }

    private static class StringToUriCoercion extends CoercionBase<String,URI> {

        StringToUriCoercion() {
            super(String.class, URI.class);
        }

        @NotNull
        @Override
        public URI coerce(@NotNull String value) {
            try {
                return new URI(value);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
