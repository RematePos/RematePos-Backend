package com.corhuila.microservices.customer_microservice.customer.validation;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public enum DocumentType {
    CC("CC", Set.of("CEDULA", "CEDULA_CIUDADANIA", "CEDULA_DE_CIUDADANIA")),
    DNI("DNI", Set.of()),
    TI("TI", Set.of("TARJETA_IDENTIDAD", "TARJETA_DE_IDENTIDAD")),
    PAS("PAS", Set.of("PASAPORTE")),
    RC("RC", Set.of("REGISTRO_CIVIL")),
    CE("CE", Set.of("CEDULA_EXTRANJERIA", "CEDULA_DE_EXTRANJERIA", "EXTRANJERIA"));

    private final String code;
    private final Set<String> aliases;

    DocumentType(String code, Set<String> aliases) {
        this.code = code;
        this.aliases = aliases;
    }

    public String code() {
        return code;
    }

    public static Optional<DocumentType> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String normalized = normalize(raw);
        return Arrays.stream(values())
                .filter(type -> type.matches(normalized))
                .findFirst();
    }

    public static Set<String> allowedCodes() {
        Set<String> codes = new HashSet<>();
        Arrays.stream(values()).forEach(type -> codes.add(type.code));
        return codes;
    }

    public boolean isDocumentNumberValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String trimmed = value.trim();

        // Keep rules explicit per type to preserve business intent.
        return switch (this) {
            case CC, DNI, TI, RC, CE -> trimmed.matches("^[0-9]{5,15}$");
            case PAS -> trimmed.matches("^[A-Za-z0-9]{6,12}$");
        };
    }

    private boolean matches(String normalized) {
        return code.equals(normalized) || aliases.contains(normalized);
    }

    private static String normalize(String raw) {
        String noAccents = Normalizer.normalize(raw, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noAccents
                .trim()
                .replace('-', ' ')
                .replaceAll("\\s+", "_")
                .toUpperCase(Locale.ROOT);
    }
}

