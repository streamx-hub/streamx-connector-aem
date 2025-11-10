package com.streamx.aem.connector.blueprints;

import static org.assertj.core.api.Assertions.assertThat;

import com.streamx.aem.connector.test.util.OsgiConfigUtils;
import com.streamx.aem.connector.test.util.RandomBytesSlingRequestProcessor;
import com.streamx.aem.connector.test.util.ResourceInfoFactory;
import com.streamx.blueprints.data.Page;
import com.streamx.sling.connector.PublishData;
import com.streamx.sling.connector.ResourceInfo;
import com.streamx.sling.connector.UnpublishData;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import javax.jcr.RepositoryException;
import org.apache.sling.engine.SlingRequestProcessor;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AemContextExtension.class)
class PagePublicationHandlerTest {

  private static final int TEXT_DATA_LENGTH = 888;
  private static final int BINARY_DATA_LENGTH = 1625;

  private final AemContext context = new AemContext(ResourceResolverType.JCR_OAK);

  private final SlingRequestProcessor requestProcessor = new RandomBytesSlingRequestProcessor(
      "/content/pages/usual-aem-page.html",
      TEXT_DATA_LENGTH
  );

  @BeforeEach
  void setup() {
    context.registerService(SlingRequestProcessor.class, requestProcessor);
    context.registerInjectActivateService(PageDataService.class);
    context.load().json(
        "/com/streamx/aem/connector/blueprints/usual-aem-page.json",
        "/content/pages/usual-aem-page"
    );
  }

  @Test
  void mustHandle() throws RepositoryException {
    String pagePath = "/content/pages/usual-aem-page";
    ResourceInfo resourceInfo = ResourceInfoFactory.createWithProperties(
        context, pagePath, "jcr:primaryType", "jcr:content/cq:template"
    );

    String expectedSubject = "/content/pages/usual-aem-page.html";
    PagePublicationHandler handler = context.registerInjectActivateService(PagePublicationHandler.class);
    PublishData<Page> publishData = handler.getPublishData(resourceInfo);
    UnpublishData<Page> unpublishData = handler.getUnpublishData(resourceInfo);
    assertThat(handler.canHandle(resourceInfo)).isTrue();
    assertThat(publishData.getData().getContent().array()).hasSize(BINARY_DATA_LENGTH);
    assertThat(publishData.getSubject()).isEqualTo(expectedSubject);
    assertThat(publishData.getData().getType()).isEqualTo("/conf/firsthops/settings/wcm/templates/page-content");
    assertThat(unpublishData.getSubject()).isEqualTo(expectedSubject);
    assertThat(unpublishData.getData().getType()).isEqualTo("/conf/firsthops/settings/wcm/templates/page-content");

    OsgiConfigUtils.disableHandler(handler, context);
    assertThat(handler.canHandle(resourceInfo)).isFalse();
  }

  @Test
  void shouldNotFailWhenPropertiesForLoadingStreamxResourceTypeAreNotPresentInReceivedResourceInfoObject() throws RepositoryException {
    String pagePath = "/content/pages/usual-aem-page";
    ResourceInfo resourceInfo = ResourceInfoFactory.createWithProperties(
        context, pagePath, "jcr:primaryType"
    );

    PagePublicationHandler handler = context.registerInjectActivateService(PagePublicationHandler.class);
    assertThat(handler.canHandle(resourceInfo)).isTrue();

    PublishData<Page> publishData = handler.getPublishData(resourceInfo);
    assertThat(publishData.getData().getType()).isNull();

    UnpublishData<Page> unpublishData = handler.getUnpublishData(resourceInfo);
    assertThat(unpublishData.getData().getType()).isNull();
  }
}
