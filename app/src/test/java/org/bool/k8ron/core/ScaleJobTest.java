package org.bool.k8ron.core;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean.MethodInvokingJob;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ScaleJobTest {

    @Mock
    private JobExecutionContext context;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private KubernetesClient k8sClient;

    @Mock
    private NonNamespaceOperation<Deployment, DeploymentList, RollableScalableResource<Deployment>> deployments;

    @Mock
    private RollableScalableResource<Deployment> resource;

    @InjectMocks
    private ScaleJob job;

    @Test
    void testInitialState() {
        assertThat(job.getReplicas())
            .isEqualTo(0);
    }

    @ValueSource(ints = { 0, 1, 5, 100 })
    @ParameterizedTest
    void testScale(int replicas) {
        given(context.getJobDetail())
            .willReturn(JobBuilder.newJob(MethodInvokingJob.class).withIdentity("test", "namespace/name").build());

        given(k8sClient.apps().deployments().inNamespace("namespace"))
            .willReturn(deployments);
        given(deployments.withName("name"))
            .willReturn(resource);
        given(resource.scale(replicas))
            .willReturn(new DeploymentBuilder().withMetadata(new ObjectMeta()).build());

        job.setReplicas(replicas);
        job.execute(context);

        then(deployments.withName("name"))
            .should().scale(replicas);
    }
}
