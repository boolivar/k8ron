package org.bool.k8ron.service;

import org.bool.k8ron.core.Quartz;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    private final Flux<TriggerKey> keys = Flux.just(new TriggerKey("test"));

    @Mock
    private Quartz quartz;

    @InjectMocks
    private ScheduleService service;

    @Captor
    private ArgumentCaptor<GroupMatcher<TriggerKey>> captor;

    @BeforeEach
    void setupKeys() {
        given(quartz.retrieveKeys(any()))
            .willReturn(keys);
    }

    @ValueSource(booleans = { true, false })
    @ParameterizedTest
    void testSchedule(boolean unscheduleResponse) {
        var triggers = Flux.just(TriggerBuilder.newTrigger().build());
        given(quartz.unschedule(keys))
            .willReturn(Mono.just(unscheduleResponse));
        given(quartz.schedule(triggers))
            .willReturn(Mono.just(5L));

        assertThat(service.schedule(triggers).block())
            .isEqualTo(5L);
    }

    @Test
    void testKeys() {
        assertThat(service.retrieveKeys())
            .isSameAs(keys);

        then(quartz).should().retrieveKeys(captor.capture());
        assertThat(List.of(new TriggerKey("k", "k/v"), new TriggerKey("any", "namespace/name")))
            .allMatch(key -> captor.getValue().isMatch(key));
        assertThat(List.of(new TriggerKey("k"), new TriggerKey("k", "v")))
            .noneMatch(key -> captor.getValue().isMatch(key));
    }

    @Test
    void testNamespaceKeys() {
        assertThat(service.retrieveKeys("ns"))
            .isSameAs(keys);

        then(quartz).should().retrieveKeys(captor.capture());
        assertThat(List.of(new TriggerKey("key", "ns/a"), new TriggerKey("any", "ns/b")))
            .allMatch(key -> captor.getValue().isMatch(key));
        assertThat(List.of(new TriggerKey("key"), new TriggerKey("any", "k/v")))
            .noneMatch(key -> captor.getValue().isMatch(key));
    }

    @Test
    void testNameKeys() {
        assertThat(service.retrieveKeys("ns", "n"))
            .isSameAs(keys);

        then(quartz).should().retrieveKeys(captor.capture());
        assertThat(List.of(new TriggerKey("key", "ns/n"), new TriggerKey("any", "ns/n")))
            .allMatch(key -> captor.getValue().isMatch(key));
        assertThat(List.of(new TriggerKey("key"), new TriggerKey("any", "ns/name")))
            .noneMatch(key -> captor.getValue().isMatch(key));
    }
}
