/*
 * Copyright (c) 2017 Evident Solutions Oy
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

package org.dalesbred.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

/**
 * A very simple builder that can be used to build queries dynamically.
 * Operates on a slightly higher level than StringBuilder in that it tracks
 * the query-variables and can be used as a building block for higher level
 * abstractions.
 */
public final class QueryBuilder {

    /** Placeholder to be used in queries for values */
    public static final String PLACEHOLDER = "?";

    /** The SQL query gathered so far */
    private final StringBuilder query = new StringBuilder(100);

    /** The arguments gathered so far */
    private final List<Object> arguments = new ArrayList<>();

    /**
     * Constructs a new empty QueryBuilder.
     */
    public QueryBuilder() {
    }

    /**
     * Constructs a QueryBuilder with given initial SQL-fragment.
     */
    public QueryBuilder(@NotNull String sql) {
        append(sql);
    }

    /**
     * Constructs a QueryBuilder with given initial SQL-fragment and arguments.
     */
    public QueryBuilder(@NotNull String sql, Object... arguments) {
        append(sql, arguments);
    }

    /**
     * Appends given fragment to this query.
     */
    public @NotNull QueryBuilder append(@NotNull String sql) {
        return append(sql, emptyList());
    }

    /**
     * Appends given fragment and arguments to this query.
     */
    public @NotNull QueryBuilder append(@NotNull String sql, Object... args) {
        return append(sql, asList(args));
    }

    /**
     * Appends given fragment and arguments to this query.
     */
    public @NotNull QueryBuilder append(@NotNull String sql, @NotNull Collection<?> args) {
        query.append(requireNonNull(sql));
        addArguments(args);
        return this;
    }

    /**
     * Appends given query and its arguments to this query.
     */
    public @NotNull QueryBuilder append(@SuppressWarnings("ParameterHidesMemberVariable") @NotNull SqlQuery query) {
        this.query.append(query.getSql());
        arguments.addAll(query.getArguments());
        return this;
    }

    /**
     * Adds a given amount of comma-separated place-holders. The amount must be at last 1.
     */
    public @NotNull QueryBuilder appendPlaceholders(int count) {
        if (count <= 0) throw new IllegalArgumentException("count must be positive, but was: " + count);

        query.append('?');
        for (int i = 1; i < count; i++)
            query.append(",?");

        return this;
    }

    /**
     * Adds placeholders for all elements of collection and then adds then values
     * of collection itself.
     */
    public @NotNull QueryBuilder appendPlaceholders(@NotNull Collection<?> args) {
        appendPlaceholders(args.size());
        addArguments(args);

        return this;
    }

    /**
     * Is the query string empty?
     */
    public boolean isEmpty() {
       return query.length() == 0;
    }

    /**
     * Does this query have any arguments?
     */
    public boolean hasArguments() {
        return !arguments.isEmpty();
    }

    /**
     * Adds an argument to this query.
     */
    public @NotNull QueryBuilder addArgument(@Nullable Object argument) {
        arguments.add(argument);
        return this;
    }

    /**
     * Adds given arguments to this query.
     */
    public @NotNull QueryBuilder addArguments(Object... args) {
        return addArguments(asList(args));
    }

    /**
     * Adds given arguments to this query.
     */
    public @NotNull QueryBuilder addArguments(@NotNull Collection<?> args) {
        arguments.addAll(args);
        return this;
    }

    /**
     * Builds an SQL query from the current state of this builder.
     *
     * @throws IllegalStateException if the builder is empty
     */
    public @NotNull SqlQuery build() {
        if (query.length() == 0)
            throw new IllegalStateException("empty query");

        return SqlQuery.query(query.toString(), new ArrayList<>(arguments));
    }
}
