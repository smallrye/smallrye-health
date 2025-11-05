package io.smallrye.health.test;

import java.util.List;
import java.util.Map;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.smallrye.health.deployment.ContextHealthCheck;
import io.smallrye.health.deployment.ContextInfo;
import io.smallrye.health.deployment.RequestFilter;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

@RunAsClient
class ContextPropagatedHealthTest extends TCKBase {

    @Deployment
    public static Archive<WebArchive> getDeployment() {
        return DeploymentUtils
                .createWarFileWithClasses(ContextPropagatedHealthTest.class.getSimpleName(),
                        ContextHealthCheck.class, ContextInfo.class, RequestFilter.class, TCKBase.class);
    }

    // got to repeat this test a couple of times to trigger potential race condition issue at least once.
    @Test(invocationCount = 20)
    void twoParallelRequests_shouldNotMixupContext() {
        String contextValue = "test-value";
        String otherContextValue = "other-test-value";
        Uni<Response> response = Uni.createFrom().voidItem().emitOn(Infrastructure.getDefaultExecutor()).onItem()
                .transform(v -> getUrlHealthContents(Map.of(RequestFilter.TEST_CONTEXT_HEADER, contextValue)));
        Uni<Response> otherResponse = Uni.createFrom().voidItem().emitOn(Infrastructure.getDefaultExecutor()).onItem()
                .transform(v -> getUrlHealthContents(Map.of(RequestFilter.TEST_CONTEXT_HEADER, otherContextValue)));

        List<Response> responses = Uni.join().all(response, otherResponse).andCollectFailures().await().indefinitely();

        String responseBody = responses.get(0).getBody().orElseThrow();
        String otherResponseBody = responses.get(1).getBody().orElseThrow();
        Assert.assertNotEquals(responseBody, otherResponseBody);
        Assert.assertTrue(responseBody.contains(contextValue), responseBody);
        Assert.assertTrue(otherResponseBody.contains(otherContextValue), otherResponseBody);
    }
}
