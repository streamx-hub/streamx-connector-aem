package com.streamx.aem.connector.blueprints;

import static org.assertj.core.api.Assertions.assertThat;

import com.streamx.aem.connector.test.util.OsgiConfigUtils;
import com.streamx.aem.connector.test.util.RandomBytesSlingRequestProcessor;
import com.streamx.aem.connector.test.util.ResourceInfoFactory;
import com.streamx.blueprints.data.Fragment;
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
class FragmentPublicationHandlerTest {

  private static final int TEXT_DATA_LENGTH = 888;
  private static final int BINARY_DATA_LENGTH = 1625;

  private final AemContext context = new AemContext(ResourceResolverType.JCR_OAK);

  private final SlingRequestProcessor requestProcessor = new RandomBytesSlingRequestProcessor(
      "/content/experience-fragments/fragment.html", TEXT_DATA_LENGTH
  );

  @BeforeEach
  void setup() {
    context.registerService(SlingRequestProcessor.class, requestProcessor);
    context.registerInjectActivateService(PageDataService.class);

    context.load().json(
        "/com/streamx/aem/connector/blueprints/franklin-page.json",
        "/content/franklin-page"
    );
    context.load().json(
        "/com/streamx/aem/connector/blueprints/usual-aem-page.json",
        "/content/usual-aem-page"
    );
    context.load().json(
        "/com/streamx/aem/connector/blueprints/usual-aem-page.json",
        "/content/experience-fragments/fragment"
    );
    context.build().resource("/content/random-page").commit();
  }

  @Test
  void mustHandle() throws RepositoryException {
    String fragmentPath = "/content/experience-fragments/fragment";
    ResourceInfo fragmentResourceInfo = ResourceInfoFactory.createWithProperties(
        context, fragmentPath, "jcr:primaryType", "jcr:content/cq:template"
    );

    String expectedSubject = "/content/experience-fragments/fragment.html";
    FragmentPublicationHandler handler = context.registerInjectActivateService(FragmentPublicationHandler.class);
    assertThat(handler.canHandle(fragmentResourceInfo)).isTrue();

    PublishData<Fragment> publishData = handler.getPublishData(fragmentResourceInfo);
    assertThat(publishData.getData().getContent().array()).hasSize(BINARY_DATA_LENGTH);
    assertThat(publishData.getSubject()).isEqualTo(expectedSubject);
    assertThat(publishData.getData().getType()).isEqualTo("/conf/firsthops/settings/wcm/templates/page-content");

    UnpublishData<Fragment> unpublishData = handler.getUnpublishData(fragmentResourceInfo);
    assertThat(unpublishData.getSubject()).isEqualTo(expectedSubject);
    assertThat(unpublishData.getData().getType()).isEqualTo("/conf/firsthops/settings/wcm/templates/page-content");

    OsgiConfigUtils.disableHandler(handler, context);
    assertThat(handler.canHandle(fragmentResourceInfo)).isFalse();
  }

  @Test
  void shouldNotHandleUsualPage() {
    String pagePath = "/content/usual-aem-page";
    ResourceInfo pageResourceInfo = ResourceInfoFactory.page(pagePath);

    FragmentPublicationHandler handler = context.registerInjectActivateService(FragmentPublicationHandler.class);
    assertThat(handler.canHandle(pageResourceInfo)).isFalse();
  }
}
