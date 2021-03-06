package de.subcentral.core.parse;

import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

import de.subcentral.core.util.SimplePropDescriptor;

public class ParsingUtil {
    private static final Logger log = LogManager.getLogger(ParsingUtil.class);

    private ParsingUtil() {
        throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }

    public static final <T> T reflectiveMapping(Class<T> targetType, Map<SimplePropDescriptor, String> props, ParsePropService parsePropService) throws InstantiationException, IllegalAccessException {
        Objects.requireNonNull(targetType, "targetType");
        T bean = targetType.newInstance();
        for (Map.Entry<SimplePropDescriptor, String> p : props.entrySet()) {
            SimplePropDescriptor simplePropDescr = p.getKey();
            if (targetType.equals(simplePropDescr.getBeanClass())) {
                try {
                    PropertyDescriptor propDescr = simplePropDescr.toPropertyDescriptor();
                    TypeToken<?> type = TypeToken.of(propDescr.getReadMethod().getGenericParameterTypes()[0]);
                    if (Collection.class.isAssignableFrom(type.getRawType())) {
                        ParameterizedType genericType = (ParameterizedType) type.getType();
                        Class<?> itemClass = (Class<?>) genericType.getActualTypeArguments()[0];
                        List<?> value = parsePropService.parseList(p.getValue(), simplePropDescr, itemClass);
                        if (Set.class.isAssignableFrom(type.getRawType())) {
                            simplePropDescr.toPropertyDescriptor().getWriteMethod().invoke(bean, ImmutableSet.copyOf(value));
                        }
                        else {
                            simplePropDescr.toPropertyDescriptor().getWriteMethod().invoke(bean, value);
                        }
                    }
                    else {
                        simplePropDescr.toPropertyDescriptor().getWriteMethod().invoke(bean, parsePropService.parse(p.getValue(), simplePropDescr, type.wrap().getRawType()));
                    }

                }
                catch (Exception e) {
                    log.warn("Failed to map value " + p.getValue() + " to property " + p.getKey() + " of bean of type " + targetType, e);
                }
            }
        }
        return bean;
    }

    public static List<ParsingService> filterByTargetTypes(Iterable<ParsingService> parsingServices, Set<Class<?>> targetTypes) {
        ImmutableList.Builder<ParsingService> filteredServices = ImmutableList.builder();
        for (ParsingService service : parsingServices) {
            if (!Collections.disjoint(service.getSupportedTargetTypes(), targetTypes)) {
                filteredServices.add(service);
            }
        }
        return filteredServices.build();
    }
}
