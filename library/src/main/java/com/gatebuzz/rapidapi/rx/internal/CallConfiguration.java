package com.gatebuzz.rapidapi.rx.internal;

import java.util.List;
import java.util.Set;

public class CallConfiguration {
    public final String project;
    public final String key;
    final String pack;
    final String block;
    final List<String> parameters;
    final Set<String> urlEncoded;

    public CallConfiguration(String project, String key, String pack, String block, List<String> parameters, Set<String> urlEncoded) {
        this.project = project;
        this.key = key;
        this.pack = pack;
        this.block = block;
        this.parameters = parameters;
        this.urlEncoded = urlEncoded;
    }
}
