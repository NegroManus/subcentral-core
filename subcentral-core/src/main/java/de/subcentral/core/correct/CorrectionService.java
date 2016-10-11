package de.subcentral.core.correct;

import java.util.List;
import java.util.function.Function;

import de.subcentral.core.util.Service;

public interface CorrectionService extends Service, Function<Object, List<Correction>> {
    public List<Correction> correct(Object bean);

    @Override
    default List<Correction> apply(Object t) {
        return correct(t);
    }
}
