package com.cargasafe.auth.shared.infrastructure.jpa;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.Locale;
import java.util.regex.Pattern;

public class SnakeCasePhysicalNamingStrategy implements PhysicalNamingStrategy {

    private static final Pattern CAMEL_CASE = Pattern.compile("([a-z])([A-Z])");

    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment env) {
        return apply(name);
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment env) {
        return apply(name);
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment env) {
        return apply(name);
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment env) {
        return apply(name);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment env) {
        return apply(name);
    }

    private Identifier apply(Identifier name) {
        if (name == null) return null;
        String snake = CAMEL_CASE.matcher(name.getText()).replaceAll("$1_$2").toLowerCase(Locale.ROOT);
        return Identifier.toIdentifier(snake, name.isQuoted());
    }
}
