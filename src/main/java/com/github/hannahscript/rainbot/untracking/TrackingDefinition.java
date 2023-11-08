package com.github.hannahscript.rainbot.untracking;

import java.util.List;

public record TrackingDefinition(List<String> hosts, List<String> queryParams) {
}
