package io.github.vab2048.axon.exhibition.app.controller;

import io.github.vab2048.axon.exhibition.app.controller.dto.TEPDTOs.TEPDescription;
import io.github.vab2048.axon.exhibition.app.controller.dto.TEPDTOs.TEPSegmentProcessingStatus;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Operations on the application's event processors.
 *
 */
@RequestMapping("/_ops/")
@RestController
public class _OperationsController {
    private static final Logger log = LoggerFactory.getLogger(_OperationsController.class);
    private final EventProcessingConfiguration eventProcessingConfiguration;
    private final TokenStore tokenStore;
    private final TransactionTemplate transactionTemplate;


    public _OperationsController(EventProcessingConfiguration eventProcessingConfiguration, TokenStore tokenStore, TransactionTemplate transactionTemplate) {
        this.eventProcessingConfiguration = eventProcessingConfiguration;
        this.tokenStore = tokenStore;
        this.transactionTemplate = transactionTemplate;
    }

    public record EPNameAndType(String name, String type) {}

    @GetMapping("/event-processor-types")
    public ResponseEntity<?> getAllEventProcessors() {
        return ResponseEntity.ok(eventProcessingConfiguration.eventProcessors().entrySet().stream()
                .map(entry -> new EPNameAndType(entry.getKey(), entry.getValue().getClass().toString()))
                .toList());
    }


    /**
     * { @return set of names of the application's tracking event processors. }
     */
    @GetMapping("/tep")
    public Set<TEPDescription> getAllTEPs() {
        Map<String, EventProcessor> eventProcessors = eventProcessingConfiguration.eventProcessors();
        return eventProcessors.values().stream()
                .filter(TrackingEventProcessor.class::isInstance)
                .map(tep -> (TrackingEventProcessor) tep)
                .map(tep -> {
                    Map<Integer, TEPSegmentProcessingStatus> segmentsCaughtUp = tep.processingStatus().entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, entry ->
                                    new TEPSegmentProcessingStatus(
                                            entry.getValue().isCaughtUp(),
                                            entry.getValue().isReplaying(),
                                            entry.getValue().isMerging(),
                                            entry.getValue().isErrorState(),
                                            entry.getValue().getCurrentPosition(),
                                            entry.getValue().getResetPosition())
                            ));
                    return new TEPDescription(tep.getName(), tep.supportsReset(), segmentsCaughtUp);
                })
                .collect(Collectors.toSet());
    }

    /**
     * Restart (shutdown, reset, and start) the given tracking event processor.
     */
    @PostMapping("/{processorName}/restart")
    public ResponseEntity<String> resetEventProcessor(@PathVariable String processorName) {
        Optional<TrackingEventProcessor> optionalTrackingEventProcessor =
                eventProcessingConfiguration.eventProcessor(processorName, TrackingEventProcessor.class);
        if (optionalTrackingEventProcessor.isPresent()) {
            TrackingEventProcessor eventProcessorToReset = optionalTrackingEventProcessor.get();

            eventProcessorToReset.shutDown();
            eventProcessorToReset.resetTokens();
            eventProcessorToReset.start();

            return ResponseEntity.ok()
                    .body(String.format("Event Processor [%s] has been reset", processorName));
        } else {
            return ResponseEntity.badRequest()
                    .body(String.format(
                            "Event Processor [%s] is not of type Tracking, hence cannot reset.",
                            processorName
                    ));
        }
    }


    private List<String> deleteTrackingEventProcessor(TEPDescription tep) {
        List<String> results = new ArrayList<>();

        // Delete the token for each segment.
        for(var entry : tep.segmentIdToProcessingStatus().entrySet()) {
            transactionTemplate.execute(status -> {
                tokenStore.deleteToken(tep.name(), entry.getKey());
                return null;
            });
            results.add("Deleted segment %s/%s of token %s"
                    .formatted(entry.getKey(), tep.segmentIdToProcessingStatus().size(), tep.name()));
        }

        return results;
    }


    @PostMapping("/restart-all")
    public ResponseEntity<?> resetAllEventProcessors() {
        Map<Boolean, List<TEPDescription>> resetVsNonReset = getAllTEPs().stream().collect(Collectors.partitioningBy(TEPDescription::supportsReset));
        List<TEPDescription> resettableTEPs = resetVsNonReset.get(true);
        List<TEPDescription> nonResettableTEPs = resetVsNonReset.get(false);

        log.info("""
        \s
          Received request to reset all tracking event processors.
          Resettable TEPs (will reset):      {}
          Non-resettable TEPs: {}""",
                resettableTEPs.stream().map(TEPDescription::name).collect(Collectors.toList()),
                nonResettableTEPs.stream().map(TEPDescription::name).collect(Collectors.toList()));

        // Reset those TEPs which are resettable.
        List<String> resetTEPsResults = resettableTEPs.stream()
                .peek(eventProcessor -> log.info("Attempting to reset event processor: {}", eventProcessor.name()))
                .map(tep -> resetEventProcessor(tep.name()))
                .map(HttpEntity::getBody)
                .collect(Collectors.toList());

        // Return the results...
        return ResponseEntity.ok(resetTEPsResults);
    }




}
