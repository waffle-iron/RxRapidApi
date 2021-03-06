package com.gatebuzz.rapidapi.rx.internal;

import android.support.annotation.VisibleForTesting;
import android.support.v4.util.Pair;

import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;

public class InvocationHandler implements java.lang.reflect.InvocationHandler {
    private Map<String, CallConfiguration> callConfigurationMap;
    private EngineYard engineYard;

    public InvocationHandler(Map<String, CallConfiguration> callConfigurationMap) {
        this(callConfigurationMap, new DefaultEngineYard());
    }

    @VisibleForTesting
    InvocationHandler(Map<String, CallConfiguration> callConfigurationMap, EngineYard engineYard) {
        this.callConfigurationMap = callConfigurationMap;
        this.engineYard = engineYard;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] parameterValues) throws Throwable {
        final CallConfiguration configuration = callConfigurationMap.get(method.getName());

        final Map<String, Pair<String, String>> body = new HashMap<>();
        for (int i = 0; i < configuration.parameters.size(); i++) {
            String value = String.valueOf(parameterValues[i]);
            if (configuration.urlEncoded.contains(configuration.parameters.get(i))) {
                value = URLEncoder.encode(value, "UTF-8");
            }
            body.put(configuration.parameters.get(i), new Pair<>("data", value));
        }

        return Observable.create(engineYard.create(configuration, body));
    }

    interface EngineYard {
        Engine create(CallConfiguration configuration, Map<String, Pair<String, String>> body);
    }

    private static class DefaultEngineYard implements EngineYard {
        @Override
        public Engine create(CallConfiguration configuration, Map<String, Pair<String, String>> body) {
            return new Engine(configuration, body);
        }
    }
}
