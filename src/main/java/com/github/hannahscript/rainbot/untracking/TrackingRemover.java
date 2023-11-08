package com.github.hannahscript.rainbot.untracking;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrackingRemover {
    private static final List<TrackingDefinition> trackingDefinitions = new ArrayList<>();
    private static final List<String> forbiddenQueryParamPrefixes = new ArrayList<>();
    
    static {
        trackingDefinitions.add(new TrackingDefinition(
                List.of("youtu.be", "youtube.com", "youtube.de"), 
                List.of("si")));

        forbiddenQueryParamPrefixes.add("utm_");
    }
    
    public static Optional<String> removeTracking(String text) {
        if (!isValidURL(text)) {
            return Optional.empty();
        }
        
        try {
            URIBuilder builder = new URIBuilder(text);
            int initialQueryParamLength = builder.getQueryParams().size();

            removeGeneralQueryParams(builder);
            findDefinition(builder).ifPresent(definition -> removeSiteSpecificTracking(builder, definition));

            return builder.getQueryParams().size() < initialQueryParamLength ?
                    Optional.of(builder.build().toURL().toString())
                    : Optional.empty();
        } catch (URISyntaxException | MalformedURLException e) {
            // This ought to never happen because we start with a validated URL and manipulate it through
            // the URIBuilder ...
            System.err.println("Exception when rebuilding untracked URL");
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private static boolean isValidURL(String text) {
        try {
            URI.create(text).toURL();
        } catch (MalformedURLException | IllegalArgumentException e) {
            return false;
        }
        
        return true;
    }

    private static void removeGeneralQueryParams(URIBuilder uriBuilder) {
        List<NameValuePair> params = uriBuilder.getQueryParams();
        boolean foundTrackingParams = params.removeIf(pair -> forbiddenQueryParamPrefixes.stream().anyMatch(prefix -> pair.getName().startsWith(prefix)));

        if (foundTrackingParams) {
            uriBuilder.setParameters(params);
        }
    }

    private static Optional<TrackingDefinition> findDefinition(URIBuilder uriBuilder) {
        return trackingDefinitions.stream()
                .filter(d -> d.hosts().contains(uriBuilder.getHost()))
                .findFirst();
    }

    private static void removeSiteSpecificTracking(URIBuilder uriBuilder, TrackingDefinition definition) {
        List<NameValuePair> params = uriBuilder.getQueryParams();
        boolean foundTrackingParams = params.removeIf(pair -> definition.queryParams().contains(pair.getName()));

        if (foundTrackingParams) {
            uriBuilder.setParameters(params);
        }
    }
}
