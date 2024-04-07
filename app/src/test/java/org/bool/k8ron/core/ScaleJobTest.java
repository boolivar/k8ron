package org.bool.k8ron.core;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean.MethodInvokingJob;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@EnableKubernetesMockClient(crud = true)
@ExtendWith(MockitoExtension.class)
class ScaleJobTest {

    @Mock
    private JobExecutionContext context;

    @SuppressFBWarnings({ "NP_UNWRITTEN_FIELD", "UWF_UNWRITTEN_FIELD" })
    private KubernetesClient k8sClient;

    private ScaleJob job;

    @BeforeEach
    void initJob() {
        job = new ScaleJob(k8sClient);
    }

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
        k8sClient.apps().deployments().inNamespace("namespace").resource(new DeploymentBuilder()
                .withMetadata(new ObjectMetaBuilder().withName("name").build()).build()).create();

        job.setReplicas(replicas);
        job.execute(context);

        assertThat(k8sClient.apps().deployments().inNamespace("namespace").withName("name").get().getSpec().getReplicas())
            .isEqualTo(replicas);
        assertThat(job.getReplicas())
            .isEqualTo(replicas);
    }
}
