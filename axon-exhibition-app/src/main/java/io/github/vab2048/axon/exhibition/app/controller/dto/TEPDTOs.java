package io.github.vab2048.axon.exhibition.app.controller.dto;

import java.util.Map;
import java.util.OptionalLong;

public class TEPDTOs {

    public record TEPSegmentProcessingStatus(
            boolean isCaughtUp,
            boolean isReplaying,
            boolean isMerging,
            boolean isErrorState,
            OptionalLong currentPosition,
            OptionalLong resetPosition) {}

    public record TEPDescription(String name, boolean supportsReset, Map<Integer, TEPSegmentProcessingStatus> segmentIdToProcessingStatus) {
    }
}
