package com.github.hannahscript.rainbot.untracking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class TrackingRemoverTest {
    @Test
    void testReturnsEmptyWhenNoURL() {
        Optional<String> urlMaybe = TrackingRemover.removeTracking("what url?");

        Assertions.assertTrue(urlMaybe.isEmpty());
    }

    @Test
    void testReturnsEmptyWhenUnknownURLWithoutKnownPrefixes() {
        Optional<String> urlMaybe = TrackingRemover.removeTracking("https://google.com");

        Assertions.assertTrue(urlMaybe.isEmpty());
    }

    @Test
    void testRemovesKnownPrefixesOnUnknownURL() {
        Optional<String> urlMaybe = TrackingRemover.removeTracking("https://google.com?utm_source=foo");

        Assertions.assertTrue(urlMaybe.isPresent());
        Assertions.assertEquals("https://google.com", urlMaybe.get());
    }

    @Test
    void testRemovesKnownPrefixesAndYoutubeTracking() {
        Optional<String> urlMaybe = TrackingRemover.removeTracking("https://youtu.be/KdXA6daqWsw?si=5WqjCyamgiEWDkIN&foo=123&utm_source=foo");

        Assertions.assertTrue(urlMaybe.isPresent());
        Assertions.assertEquals("https://youtu.be/KdXA6daqWsw?foo=123", urlMaybe.get());
    }
    
    @Test
    void testRemovesYoutubeTracking() {
        Optional<String> urlMaybe = TrackingRemover.removeTracking("https://youtu.be/KdXA6daqWsw?si=5WqjCyamgiEWDkIN&foo=123");

        Assertions.assertTrue(urlMaybe.isPresent());
        Assertions.assertEquals("https://youtu.be/KdXA6daqWsw?foo=123", urlMaybe.get());
    }

    @Test
    void testReturnsEmptyWhenKnownURLButNoTracking() {
        Optional<String> urlMaybe = TrackingRemover.removeTracking("https://youtu.be/KdXA6daqWsw?foo=123");

        Assertions.assertTrue(urlMaybe.isEmpty());
    }
}
