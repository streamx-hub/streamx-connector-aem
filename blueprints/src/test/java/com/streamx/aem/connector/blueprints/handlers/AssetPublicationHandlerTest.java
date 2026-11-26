package com.streamx.aem.connector.blueprints.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.streamx.aem.connector.test.util.FixedResponseSlingRequestProcessor;
import com.streamx.aem.connector.test.util.OsgiConfigUtils;
import com.streamx.aem.connector.test.util.ResourceInfoFactory;
import com.streamx.blueprints.data.Asset;
import com.streamx.sling.connector.PublishData;
import com.streamx.sling.connector.ResourceInfo;
import com.streamx.sling.connector.UnpublishData;
import com.streamx.sling.connector.selectors.content.ResourceContentRelatedResourcesSelector;
import com.streamx.sling.connector.selectors.content.ResourceContentRelatedResourcesSelectorConfig;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Objects;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
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
    AssetPublicationHandler enabled = createHandler(Map.of(
        "enabled", true,
        "should.add.namespace.prefix", false
    ));
    AssetPublicationHandler disabled = createHandler(Map.of("enabled", false));
    assertThat(resourceResolver.getResource(irrelevantPath)).isNotNull();
    assertThat(resourceResolver.getResource(mountainPath)).isNotNull();
    assertThat(resourceResolver.getResource(mountainContent)).isNotNull();
    assertThat(enabled.canHandle(ResourceInfoFactory.asset(mountainPath))).isTrue();
    assertThat(disabled.canHandle(ResourceInfoFactory.asset(mountainPath))).isFalse();
    assertThat(enabled.canHandle(ResourceInfoFactory.page(irrelevantPath))).isFalse();
    assertThat(enabled.canHandle(ResourceInfoFactory.file(mountainContent))).isFalse();
  }

  @Test
  void publishesDamAssetDiscoveredByPathOnly() {
    String folder = "/content/dam/core-components-examples/library/sample-assets";
    String assetPath = folder + "/lava-rock-formation.jpg";
    ResourceInfo relatedAsset = new ResourceInfo(assetPath);
    AssetPublicationHandler handler = createHandler(Map.of(
        "enabled", true,
        "should.add.namespace.prefix", false
    ));

    assertThat(handler.canHandle(relatedAsset)).isTrue();
    PublishData<Asset> publishData = handler.getPublishData(relatedAsset);
    assertThat(publishData.getSubject()).isEqualTo(assetPath);
    assertThat(publishData.getData().getContent().array()).hasSize(DATA_SIZE);

    assertThat(handler.canHandle(new ResourceInfo(folder))).isFalse();
    assertThat(handler.canHandle(new ResourceInfo(folder + "/missing.jpg"))).isFalse();
    assertThat(handler.canHandle(new ResourceInfo(assetPath + "/jcr:content"))).isFalse();
    assertThat(handler.canHandle(new ResourceInfo("/conf/irrelevant-resource"))).isFalse();

    ResourceInfo fragment = new ResourceInfo("/content/dam/productssite/my-content-fragment");
    assertThat(handler.getPublishData(fragment)).isNull();
  }

  @Test
  void publishesDamAssetDiscoveredInOpenGraphMetadata() {
    String pagePath = "/content/test/article";
    String assetPath = "/content/dam/core-components-examples/library/sample-assets"
        + "/lava-rock-formation.jpg";
    String html = "<html><head><meta property=\"og:image\" content=\"" + assetPath
        + "\"></head><body>Article without inline images</body></html>";
    var config = mock(ResourceContentRelatedResourcesSelectorConfig.class);
    when(config.references_search$_$regexes()).thenReturn(
        new String[]{"(/content/dam/[^\\\"'\\s<>?#]+?\\.(?:jpe?g|png|gif))(?=$|[\\\"'\\s<>&?#])"}
    );
    when(config.references_exclude$_$from$_$result_regex()).thenReturn(".*\\{\\.width\\}.*");
    when(config.resource$_$path_postfix$_$to$_$append()).thenReturn(".html");
    when(config.resource_required$_$path_regex()).thenReturn("^/content/.*");
    when(config.resource_required$_$primary$_$node$_$type_regex()).thenReturn("cq:Page");
    when(config.related$_$resource_processable$_$path_regex()).thenReturn(".*\\.(css|js)$");

    var selector = new ResourceContentRelatedResourcesSelector(
        config,
        new FixedResponseSlingRequestProcessor(Map.of(pagePath + ".html", html)),
        context.getService(ResourceResolverFactory.class)
    );
    var handler = createHandler(Map.of("enabled", true, "should.add.namespace.prefix", false));

    assertThat(selector.getRelatedResources(ResourceInfoFactory.page(pagePath))).singleElement()
        .satisfies(resource -> {
          assertThat(resource.getPath()).isEqualTo(assetPath);
          assertThat(resource.getProperties()).isEmpty();
          assertThat(handler.canHandle(resource)).isTrue();
          PublishData<Asset> publication = handler.getPublishData(resource);
          assertThat(publication.getSubject()).isEqualTo(assetPath);
          assertThat(publication.getData().getContent().array()).hasSize(DATA_SIZE);
        });
  }

  @Test
  void canGetPublishData() {
    String assetPath = "/content/dam/core-components-examples/library/sample-assets/lava-rock-formation.jpg";
    AssetPublicationHandler handler = createHandler(Map.of(
        "enabled", true,
        "jcr.prop.name.for.streamx.resource.type", "jcr:primaryType",
        "should.add.namespace.prefix", false
    ));
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
  void canGetPublishDataWithNamespace() {
    String assetPath = "/content/dam/core-components-examples/library/sample-assets/lava-rock-formation.jpg";
    AssetPublicationHandler handler = createHandler(Map.of(
        "enabled", true,
        "jcr.prop.name.for.streamx.resource.type", "jcr:primaryType",
        "should.add.namespace.prefix", true,
        "namespace.prefix", "test-images"
    ));
    ResourceInfo resourceInfo = ResourceInfoFactory.asset(assetPath);
    assertThat(handler.canHandle(resourceInfo)).isTrue();

    PublishData<Asset> publishData = handler.getPublishData(resourceInfo);
    assertThat(publishData.getData().getContent().array()).hasSize(DATA_SIZE);
    assertThat(publishData.getSubject()).isEqualTo("test-images:" + assetPath);
    assertThat(publishData.getData()).isInstanceOf(Asset.class);
    assertThat(publishData.getData().getType()).isEqualTo("dam:Asset");

    UnpublishData<Asset> unpublishData = handler.getUnpublishData(resourceInfo);
    assertThat(unpublishData.getSubject()).isEqualTo("test-images:" + assetPath);
    assertThat(unpublishData.getData().getType()).isEqualTo("dam:Asset");

    OsgiConfigUtils.disableHandler(handler, context);
    assertThat(handler.canHandle(resourceInfo)).isFalse();
  }

  @Test
  void shouldSkipHandlingContentFragmentAsset() {
    String contentFragmentPath = "/content/dam/productssite/my-content-fragment";
    AssetPublicationHandler handler = createHandler(Map.of(
        "enabled", true,
        "should.add.namespace.prefix", false
    ));

    ResourceInfo resourceInfo = ResourceInfoFactory.asset(contentFragmentPath);
    assertThat(handler.canHandle(resourceInfo)).isTrue();

    PublishData<Asset> publishData = handler.getPublishData(resourceInfo);
    assertThat(publishData).isNull();

    UnpublishData<Asset> unpublishData = handler.getUnpublishData(resourceInfo);
    assertThat(unpublishData.getSubject()).isEqualTo(contentFragmentPath);
    assertThat(unpublishData.getData().getType()).isNull();
  }

  private AssetPublicationHandler createHandler(Map<String, Object> properties) {
    return context.registerInjectActivateService(
        AssetPublicationHandler.class, properties
    );
  }
}
