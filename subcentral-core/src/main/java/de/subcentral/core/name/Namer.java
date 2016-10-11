package de.subcentral.core.name;

import java.util.function.Function;

import de.subcentral.core.util.Context;

public interface Namer<T> extends Function<T, String> {
    public default String name(T obj) {
        return name(obj, Context.EMPTY);
    }

    /**
     * 
     * @param obj
     *            the object to name (can be null)
     * @param ctx
     *            the context in which the object should be named
     * @return the generated name of the candidate or an empty string {@code ""} if the candidate was {@code null}
     */
    public String name(T obj, Context ctx);

    @Override
    public default String apply(T obj) {
        return name(obj);
    }
}
