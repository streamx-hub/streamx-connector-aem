package com.streamx.aem.connector.blueprints;

import com.streamx.blueprints.data.Data;
import com.streamx.sling.connector.PublicationHandler;
import com.streamx.sling.connector.ResourceInfo;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.metatype.annotations.Designate;

@Component(service = PublicationHandler.class)
@Designate(ocd = PageModelPublicationHandlerConfig.class)
@ServiceDescription("Publication handler for AEM Page Models")
public class PageModelPublicationHandler extends BasePublicationHandler<Data> {

  private final PageDataService pageDataService;

  private final AtomicReference<Boolean> enabled = new AtomicReference<>();
  private final AtomicReference<String> publishEventType = new AtomicReference<>();
  private final AtomicReference<String> unpublishEventType = new AtomicReference<>();
  private final AtomicReference<Pattern> pageResourcePathRegex = new AtomicReference<>();
  private final AtomicReference<String[]> selectorsToAppend = new AtomicReference<>();
  private final AtomicReference<String> extensionToAppend = new AtomicReference<>();
  private final AtomicReference<String> relPathToNodeWithJcrPropForResourceType = new AtomicReference<>();
  private final AtomicReference<String> jcrPropNameForResourceType = new AtomicReference<>();

  @Activate
  public PageModelPublicationHandler(
      @Reference PageDataService pageDataService,
      @Reference ResourceResolverFactory resolverFactory,
      PageModelPublicationHandlerConfig config
  ) {
    super(resolverFactory);
    this.pageDataService = pageDataService;
    configure(config);
  }

  @Modified
  void configure(PageModelPublicationHandlerConfig config) {
    enabled.set(config.enabled());
    publishEventType.set(config.publish_event_type());
    unpublishEventType.set(config.unpublish_event_type());

    String pathRegex = config.page_resource_path_regex();
    pageResourcePathRegex.set(Optional.ofNullable(pathRegex).map(Pattern::compile).orElse(null));

    selectorsToAppend.set(Optional.ofNullable(config.selectors_to_append()).orElse(new String[0]));
    extensionToAppend.set(config.extension_to_append());
    relPathToNodeWithJcrPropForResourceType.set(config.rel_path_to_node_with_jcr_prop_for_streamx_resource_type());
    jcrPropNameForResourceType.set(config.jcr_prop_name_for_streamx_resource_type());
  }

  @Override
  public boolean canHandle(ResourceInfo resource) {
    if (!enabled.get()) {
      return false;
    }

    if (ResourcePrimaryNodeTypeChecker.isXFPath(resource.getPath())) {
      return false;
    }

    Pattern pathPattern = pageResourcePathRegex.get();
    if (pathPattern == null || !pathPattern.matcher(resource.getPath()).matches()) {
      return false;
    }

    if (!pageDataService.isPageByResourcePath(resource)) {
      return false;
    }

    try (ResourceResolver resourceResolver = createResourceResolver()) {
      return ResourcePrimaryNodeTypeChecker.isPage(resource, resourceResolver);
    }
  }

  @Override
  protected String getPublishEventType() {
    return publishEventType.get();
  }

  @Override
  protected String getUnpublishEventType() {
    return unpublishEventType.get();
  }

  @Override
  protected Data getPublishPayload(ResourceInfo resourceInfo, Resource resource, ResourceResolver resourceResolver) {
    String content = pageDataService.readContent(
        resource,
        selectorsToAppend.get(),
        extensionToAppend.get(),
        resourceResolver
    );
    return new Data(content, getStreamxResourceType(resourceInfo));
  }

  @Override
  protected Data getUnpublishPayload(ResourceInfo resourceInfo) {
    return new Data((ByteBuffer) null, getStreamxResourceType(resourceInfo));
  }

  private String getStreamxResourceType(ResourceInfo resourceInfo) {
    return readProperty(resourceInfo,
        relPathToNodeWithJcrPropForResourceType.get(),
        jcrPropNameForResourceType.get()
    );
  }

  @Override
  protected String getPublicationSubject(String resourcePath) {
    return SlingUriCreator.create(
        resourcePath,
        selectorsToAppend.get(),
        extensionToAppend.get()
    ).getPath();
  }

}
