package com.gatebuzz.rapidapi.rx;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import com.gatebuzz.rapidapi.rx.internal.CallConfiguration;
import com.gatebuzz.rapidapi.rx.internal.InvocationHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Keep
public abstract class RxRapidApiBuilder {

    @SuppressWarnings("unchecked")
    public static <I> I from(Class<I> interfaceClass) {
        Application applicationAnnotation = interfaceClass.getAnnotation(Application.class);
        String project = applicationAnnotation != null ? applicationAnnotation.project() : null;
        String key = applicationAnnotation != null ? applicationAnnotation.key() : null;

        Map<String, CallConfiguration> callConfigurationMap = new HashMap<>();
        for (Method method : interfaceClass.getMethods()) {
            callConfigurationMap.put(method.getName(), configureCall(project, key, method));
        }

        return (I) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class[]{interfaceClass}, new InvocationHandler(callConfigurationMap));
    }

    @NonNull
    private static CallConfiguration configureCall(String project, String key, Method method) {
        Application methodAppAnnotation = method.getAnnotation(Application.class);
        if (methodAppAnnotation != null) {
            project = methodAppAnnotation.project();
            key = methodAppAnnotation.key();
        }

        if (project == null || project.trim().isEmpty()) {
            throw new IllegalArgumentException("Project name not found (check the @Application annotation).");
        }

        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("API key not found (check the @Application annotation).");
        }

        String name = collectRemoteMethodName(method);
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("API method name not found on " + method.getName() + "() " +
                    "(check the @Named annotation on your interface method).");
        }

        List<String> parameters = collectParameterNames(method);
        if (parameters.size() != method.getParameterTypes().length) {
            throw new IllegalArgumentException("Incorrect number of @Named parameters on " +
                    method.getName() + "() - expecting " + method.getParameterTypes().length +
                    ", found " + parameters.size() + ".");
        }

        ApiPackage packageAnnotation = method.getAnnotation(ApiPackage.class);
        String pack = packageAnnotation != null ? packageAnnotation.value() : null;
        if (pack == null || pack.trim().isEmpty()) {
            throw new IllegalArgumentException("API package not found on " + method.getName() + "() " +
                    "(check the @ApiPackage annotation).");
        }

        Set<String> urlEncodedParameters = collectUrlEncodedParameters(method, parameters);
        return new CallConfiguration(project, key, pack, name, parameters, urlEncodedParameters);
    }

    @NonNull
    private static String collectRemoteMethodName(Method method) {
        Named methodNameAnnotation = method.getAnnotation(Named.class);
        return (methodNameAnnotation != null) ? methodNameAnnotation.value() : method.getName();
    }

    @NonNull
    private static List<String> collectParameterNames(Method method) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        List<String> parameters = new ArrayList<>();
        for (Annotation[] annotations : parameterAnnotations) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(Named.class)) {
                    parameters.add(((Named) annotation).value());
                }
            }
        }
        return parameters;
    }

    @NonNull
    private static Set<String> collectUrlEncodedParameters(Method method, List<String> names) {
        Set<String> parameters = new HashSet<>();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(UrlEncoded.class)) {
                    parameters.add(names.get(i));
                }
            }
        }
        return parameters;
    }
}
