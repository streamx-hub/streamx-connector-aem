package com.streamx.aem.connector.blueprints;

import static org.assertj.core.api.Assertions.assertThat;

import com.streamx.aem.connector.test.util.OsgiConfigUtils;
import com.streamx.aem.connector.test.util.RandomBytesSlingRequestProcessor;
import com.streamx.aem.connector.test.util.ResourceInfoFactory;
import com.streamx.blueprints.data.RenderingContext;
import com.streamx.blueprints.data.RenderingContext.OutputFormat;
import com.streamx.sling.connector.PublishData;
import com.streamx.sling.connector.ResourceInfo;
import com.streamx.sling.connector.UnpublishData;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.engine.SlingRequestProcessor;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AemContextExtension.class)
class RenderingContextPublicationHandlerTest {

  private static final int DATA_SIZE = 888;

  private final AemContext context = new AemContext(ResourceResolverType.JCR_OAK);

  private final SlingRequestProcessor requestProcessor = new RandomBytesSlingRequestProcessor(
      "/content/experience-fragments/templates/sample-rendering-context.html",
      DATA_SIZE
  );

  @BeforeEach
  void setup() {
    context.registerService(SlingRequestProcessor.class, requestProcessor);
    context.registerInjectActivateService(PageDataService.class);
    context.load().json(
        "/com/streamx/aem/connector/blueprints/sample-rendering-context.json",
        "/content/experience-fragments/templates/sample-rendering-context"
    );
  }

  @SuppressWarnings("resource")
  @Test
  void mustHandle() {
    String resourcePath = "/content/experience-fragments/templates/sample-rendering-context";
    assertThat(context.resourceResolver().getResource(resourcePath)).isNotNull();
    ResourceInfo resource = ResourceInfoFactory.page(resourcePath);
    String expectedSubject = "/content/experience-fragments/templates/sample-rendering-context";
    RenderingContextPublicationHandler handler = context.registerInjectActivateService(
        RenderingContextPublicationHandler.class
    );
    assertThat(handler.canHandle(resource)).isTrue();

    PublishData<RenderingContext> publishData = handler.getPublishData(resource);
    assertThat(publishData.getSubject()).isEqualTo(expectedSubject);
    RenderingContext renderingContext = publishData.getData();
    assertThat(renderingContext.getRendererKey()).isEqualTo(expectedSubject);
    assertThat(renderingContext.getDataKeyMatchPattern()).isEqualTo("data.*");
    assertThat(renderingContext.getDataTypeMatchPattern()).isEqualTo("test-type/.*");
    assertThat(renderingContext.getOutputKeyTemplate()).isEqualTo("key-1");
    assertThat(renderingContext.getOutputTypeTemplate()).isEqualTo("data-type3-output-type-pattern-{{id}}");
    assertThat(renderingContext.getOutputFormat()).isSameAs(OutputFormat.PAGE);

    UnpublishData<RenderingContext> unpublishData = handler.getUnpublishData(resource);
    assertThat(unpublishData.getSubject()).isEqualTo(expectedSubject);

    OsgiConfigUtils.disableHandler(handler, context);
    assertThat(handler.canHandle(resource)).isFalse();
  }
}
