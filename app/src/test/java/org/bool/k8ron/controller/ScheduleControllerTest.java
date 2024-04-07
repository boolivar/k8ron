package org.bool.k8ron.controller;

import org.bool.k8ron.service.ScheduleService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.quartz.TriggerKey;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

class ScheduleControllerTest {

    private static final ParameterizedTypeReference<Map<String, List<String>>> RESPONSE_TYPE = new ParameterizedTypeReference<>() {
    };

    private final ScheduleService service = mock(ScheduleService.class);

    private final WebTestClient webClient = WebTestClient.bindToController(new ScheduleController(service)).build();

    private final AtomicReference<Thread> executionThread = new AtomicReference<>();

    private final TriggerKey triggerKey = new TriggerKey("key", "namespace/name");

    private final Flux<TriggerKey> flux = Flux.just(triggerKey)
            .doOnNext(key -> executionThread.set(Thread.currentThread()));

    @AfterEach
    void verifyExecutionThread() {
        assertThat(executionThread)
            .doesNotHaveValue(Thread.currentThread());
    }

    @Test
    void testSchedule() {
        given(service.retrieveKeys())
            .willReturn(flux);

        var response = webClient.get().uri("/schedule").accept(MediaType.APPLICATION_JSON).exchange()
            .expectStatus().is2xxSuccessful()
            .expectBody(RESPONSE_TYPE).returnResult().getResponseBody();

        assertThat(response)
            .isEqualTo(Map.of(triggerKey.getGroup(), List.of(triggerKey.getName())));
    }

    @Test
    void testNamespaceSchedule() {
        given(service.retrieveKeys("ns"))
            .willReturn(flux);

        var response = webClient.get().uri("/schedule/ns").accept(MediaType.APPLICATION_JSON).exchange()
            .expectStatus().is2xxSuccessful()
            .expectBody(RESPONSE_TYPE).returnResult().getResponseBody();

        assertThat(response)
            .isEqualTo(Map.of(triggerKey.getGroup(), List.of(triggerKey.getName())));
    }

    @Test
    void testNameSchedule() {
        given(service.retrieveKeys("ns", "name"))
            .willReturn(flux);

        var response = webClient.get().uri("/schedule/ns/name").accept(MediaType.APPLICATION_JSON).exchange()
            .expectStatus().is2xxSuccessful()
            .expectBody(RESPONSE_TYPE).returnResult().getResponseBody();

        assertThat(response)
            .isEqualTo(Map.of(triggerKey.getGroup(), List.of(triggerKey.getName())));
    }
}
