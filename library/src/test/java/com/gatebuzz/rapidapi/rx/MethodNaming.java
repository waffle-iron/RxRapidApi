package com.gatebuzz.rapidapi.rx;

import org.junit.Test;

import java.util.Map;

import rx.Observable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MethodNaming {
    @Test
    public void overriddenMethodNameMissing() {
        try {
            RxRapidApiBuilder.from(MethodNameMissing.class);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
            assertEquals("API method name not found on someMethod() (check the @Named annotation on your interface method).", e.getMessage());
        }
    }

    @Application(project = "a", key = "a")
    private interface MethodNameMissing {
        @Named("")
        Observable<Map<String, Object>> someMethod();
    }
}
