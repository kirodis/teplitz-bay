package com.github.yafna.events.aggregate;

import com.github.yafna.events.DomainEvent;
import com.github.yafna.events.annotations.EvType;
import com.github.yafna.events.annotations.Origin;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class AggregateUtils {
    public static String resolveEventType(Class<?> type) {
        return type.getAnnotation(EvType.class).value();
    }

    public static <T extends DomainEvent> String resolveOrigin(T payload) {
        return resolveOrigin(getDomain(payload));
    }

    private static <T extends DomainEvent> Class<?> getDomain(T payload) {
        Class<? extends DomainEvent> clazz = payload.getClass();
        for (Class<?> c = clazz; c != Object.class ; c = c.getSuperclass()) {
            for (Type t : c.getGenericInterfaces()) {
                if (t instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) t;
                    Type raw = pt.getRawType();
                    if (raw instanceof Class) {
                        if (((Class<?>) raw).isAssignableFrom(DomainEvent.class)) {
                            return (Class<?>) pt.getActualTypeArguments()[0];
                        }
                    }
                }
            }
        }
        throw new IllegalArgumentException("Unable to resolve domain [" + clazz.getName() + "]");
    }

    private static String resolveOrigin(Class<?> clazz) {
        for (Class<?> c = clazz; c != Object.class ; c = c.getSuperclass()) {
            Origin annotation = c.getAnnotation(Origin.class);
            if (annotation != null) {
                return annotation.value();
            }
        }
        throw new IllegalArgumentException("Unable to resolve origin for [" + clazz.getName() + "]");
    }


}
