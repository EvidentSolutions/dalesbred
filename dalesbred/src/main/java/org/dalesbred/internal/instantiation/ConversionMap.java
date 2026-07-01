package org.dalesbred.internal.instantiation;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.*;

import static java.util.Collections.emptyList;
import static org.dalesbred.internal.utils.Primitives.wrap;
import static org.dalesbred.internal.utils.TypeUtils.*;

final class ConversionMap {

    private final @NotNull Map<Type, List<ConversionRegistration>> mappings = new HashMap<>();

    void register(@NotNull Type source, @NotNull Type target, @NotNull TypeConversion conversion) {
        mappings.computeIfAbsent(wrap(source), a -> new ArrayList<>()).add(new ConversionRegistration(target, conversion));
    }

    @NotNull
    Optional<TypeConversion> findConversion(@NotNull Type source, @NotNull Type target) {
        for (Type cl = wrap(source); cl != null; cl = genericSuperClass(cl)) {
            Optional<TypeConversion> conversion = findConversionsRegisteredFor(cl, target);
            if (conversion.isPresent())
                return conversion;
        }

        for (Type cl : genericInterfaces(source)) {
            Optional<TypeConversion> conversion = findConversionsRegisteredFor(cl, target);
            if (conversion.isPresent())
                return conversion;
        }

        return Optional.empty();
    }

    private @NotNull Optional<TypeConversion> findConversionsRegisteredFor(@NotNull Type source, @NotNull Type target) {
        List<ConversionRegistration> candidates = mappings.getOrDefault(source, emptyList());

        for (int i = candidates.size() - 1; i >= 0; i--) {
            ConversionRegistration conversion = candidates.get(i);
            if (isAssignable(target, conversion.target))
                return Optional.of(conversion.conversion);
        }

        return Optional.empty();
    }

    private record ConversionRegistration(@NotNull Type target, @NotNull TypeConversion conversion) {
    }
}
