package com.streamx.aem.connector.blueprints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.streamx.aem.connector.test.util.OsgiConfigUtils;
import com.streamx.aem.connector.test.util.ResourceInfoFactory;
import com.streamx.blueprints.data.Asset;
import com.streamx.sling.connector.PublishData;
import com.streamx.sling.connector.ResourceInfo;
import com.streamx.sling.connector.UnpublishData;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Objects;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.engine.SlingRequestProcessor;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(AemContextExtension.class)
class AssetPublicationHandlerTest {

  private static final int DATA_SIZE = 3024;

  private final AemContext context = new AemContext(ResourceResolverType.JCR_OAK);

  @SuppressWarnings("resource")
  @BeforeEach
  void setup() throws PersistenceException {
    context.registerService(SlingRequestProcessor.class, mock(SlingRequestProcessor.class));
    context.load().json(
        "/com/streamx/aem/connector/blueprints/sample-assets.json",
        "/content/dam/core-components-examples/library/sample-assets"
    );
    context.load().json(
        "/com/streamx/aem/connector/blueprints/sample-content-fragment.json",
        "/content/dam/productssite/my-content-fragment"
    );
    context.resourceResolver().delete(
        Objects.requireNonNull(context.resourceResolver().getResource(
            "/content/dam/core-components-examples/library/sample-assets/lava-rock-formation.jpg/jcr:content/renditions/original"
        ))
    );
    context.load().binaryFile(
        new ByteArrayInputStream("1".repeat(DATA_SIZE).getBytes()),
        "/content/dam/core-components-examples/library/sample-assets/lava-rock-formation.jpg/jcr:content/renditions/original"
    );
    context.build().resource("/conf/irrelevant-resource").commit();
  }

  @Test
  void canHandleUsualAssets() {
    String mountainPath = "/content/dam/core-components-examples/library/sample-assets/mountain-range.jpg";
    String mountainContent = "/content/dam/core-components-examples/library/sample-assets/mountain-range.jpg/jcr:content";
    String irrelevantPath = "/conf/irrelevant-resource";
    @SuppressWarnings("resource")
    ResourceResolver resourceResolver = context.resourceResolver();
    AssetPublicationHandler enabled = context.registerInjectActivateService(
        AssetPublicationHandler.class, Map.of("enabled", true)
    );
    AssetPublicationHandler disabled = context.registerInjectActivateService(
        AssetPublicationHandler.class, Map.of("enabled", false)
    );
    assertThat(resourceResolver.getResource(irrelevantPath)).isNotNull();
    assertThat(resourceResolver.getResource(mountainPath)).isNotNull();
    assertThat(resourceResolver.getResource(mountainContent)).isNotNull();
    assertThat(enabled.canHandle(ResourceInfoFactory.asset(mountainPath))).isTrue();
    assertThat(disabled.canHandle(ResourceInfoFactory.asset(mountainPath))).isFalse();
    assertThat(enabled.canHandle(ResourceInfoFactory.page(irrelevantPath))).isFalse();
    assertThat(enabled.canHandle(ResourceInfoFactory.file(mountainContent))).isFalse();
  }

  @Test
  void canGetPublishData() {
    String assetPath = "/content/dam/core-components-examples/library/sample-assets/lava-rock-formation.jpg";
    AssetPublicationHandler handler = context.registerInjectActivateService(
        AssetPublicationHandler.class, Map.of(
            "enabled", true,
            "jcr.prop.name.for.streamx.resource.type", "jcr:primaryType"
        )
    );
    ResourceInfo resourceInfo = ResourceInfoFactory.asset(assetPath);
    assertThat(handler.canHandle(resourceInfo)).isTrue();
    PublishData<Asset> publishData = handler.getPublishData(resourceInfo);
    assertThat(publishData.getData().getContent().array()).hasSize(DATA_SIZE);
    assertThat(publishData.getSubject()).isEqualTo(assetPath);
    assertThat(publishData.getData()).isInstanceOf(Asset.class);
    assertThat(publishData.getData().getType()).isEqualTo("dam:Asset");

    UnpublishData<Asset> unpublishData = handler.getUnpublishData(resourceInfo);
    assertThat(unpublishData.getSubject()).isEqualTo(assetPath);
    assertThat(unpublishData.getData().getType()).isEqualTo("dam:Asset");

    OsgiConfigUtils.disableHandler(handler, context);
    assertThat(handler.canHandle(resourceInfo)).isFalse();
  }

  @Test
  void shouldSkipHandlingContentFragmentAsset() {
    String contentFragmentPath = "/content/dam/productssite/my-content-fragment";
    AssetPublicationHandler handler = context.registerInjectActivateService(AssetPublicationHandler.class);

    ResourceInfo resourceInfo = ResourceInfoFactory.asset(contentFragmentPath);
    assertThat(handler.canHandle(resourceInfo)).isTrue();

    PublishData<Asset> publishData = handler.getPublishData(resourceInfo);
    assertThat(publishData).isNull();

    UnpublishData<Asset> unpublishData = handler.getUnpublishData(resourceInfo);
    assertThat(unpublishData.getSubject()).isEqualTo(contentFragmentPath);
  }
}
