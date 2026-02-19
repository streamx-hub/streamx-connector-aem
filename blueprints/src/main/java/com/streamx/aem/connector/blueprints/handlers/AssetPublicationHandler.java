package com.streamx.aem.connector.blueprints.handlers;

import com.day.cq.dam.api.Rendition;
import com.streamx.aem.connector.blueprints.tools.ByteBuffers;
import com.streamx.aem.connector.blueprints.tools.ResourcePrimaryNodeTypeChecker;
import com.streamx.blueprints.data.Asset;
import com.streamx.sling.connector.PublicationHandler;
import com.streamx.sling.connector.ResourceInfo;
import com.streamx.sling.connector.util.SimpleInternalRequest;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.uri.SlingUri;
import org.apache.sling.api.uri.SlingUriBuilder;
import org.apache.sling.engine.SlingRequestProcessor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = PublicationHandler.class)
@Designate(ocd = AssetPublicationHandlerConfig.class)
@ServiceDescription("Publication handler for AEM Assets")
public class AssetPublicationHandler extends BasePublicationHandler<Asset> {

  private static final Logger LOG = LoggerFactory.getLogger(AssetPublicationHandler.class);

  private final SlingRequestProcessor slingRequestProcessor;
  private final AtomicReference<String> assetsPathRegexp = new AtomicReference<>();
  private final AtomicReference<String> jcrPropNameForStreamxResourceType = new AtomicReference<>();

  @Activate
  public AssetPublicationHandler(
      @Reference ResourceResolverFactory resolverFactory,
      @Reference SlingRequestProcessor slingRequestProcessor,
      AssetPublicationHandlerConfig config
  ) {
    super(resolverFactory);
    this.slingRequestProcessor = slingRequestProcessor;
    configure(config);
  }

  @Modified
  void configure(AssetPublicationHandlerConfig config) {
    assignConfigValues(
        config.enabled(),
        config.publish_event_type(),
        config.unpublish_event_type()
    );
    assetsPathRegexp.set(config.assets_path_regexp());
    jcrPropNameForStreamxResourceType.set(config.jcr_prop_name_for_streamx_resource_type());
  }

  @Override
  protected boolean canHandleResource(ResourceInfo resource) {
    String resourcePath = resource.getPath();
    if (!resourcePath.matches(assetsPathRegexp.get())) {
      return false;
    }

    try (ResourceResolver resourceResolver = createResourceResolver()) {
      return ResourcePrimaryNodeTypeChecker.isAsset(resource, resourceResolver);
    }
  }

  @Override
  protected Asset getPublishPayload(ResourceInfo resourceInfo, Resource resource, ResourceResolver resourceResolver) {
    if (isContentFragment(resource)) {
      LOG.info("Resource is a Content Fragment, skipping publication: {}", resource.getPath());
      return null;
    }

    return createAssetInstance(resourceInfo, resource, resourceResolver);
  }

  @Override
  protected Asset getUnpublishPayload(ResourceInfo resourceInfo) {
    return new Asset(null, getStreamxResourceType(resourceInfo));
  }

  private static boolean isContentFragment(Resource existingAssetResource) {
    return Optional.ofNullable(existingAssetResource.getChild("jcr:content"))
        .map(Resource::getValueMap)
        .map(properties -> Boolean.TRUE.equals(properties.get("contentFragment")))
        .orElse(false);
  }

  private Asset createAssetInstance(ResourceInfo resourceInfo, Resource resource, ResourceResolver resourceResolver) {
    LOG.trace("Generating {} out of {}", Asset.class, resource.getPath());
    SlingUri slingUri = SlingUriBuilder.parse(resource.getPath(), resourceResolver).build();

    InputStream assetContentStream = Optional
        .ofNullable(resource.adaptTo(com.day.cq.dam.api.Asset.class))
        .map(com.day.cq.dam.api.Asset::getOriginal)
        .map(Rendition::getStream)
        .or(() -> new SimpleInternalRequest(slingUri, slingRequestProcessor, resourceResolver).getResponseAsInputStream())
        .orElseThrow();

    return new Asset(ByteBuffers.fromInputStream(assetContentStream), getStreamxResourceType(resourceInfo));
  }

  private String getStreamxResourceType(ResourceInfo resourceInfo) {
    return readProperty(resourceInfo, jcrPropNameForStreamxResourceType.get());
  }

}
