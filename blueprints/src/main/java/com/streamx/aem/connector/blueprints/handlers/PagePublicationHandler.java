package com.streamx.aem.connector.blueprints.handlers;

import com.streamx.aem.connector.blueprints.pagedata.PageDataService;
import com.streamx.aem.connector.blueprints.tools.ResourcePrimaryNodeTypeChecker;
import com.streamx.blueprints.data.Page;
import com.streamx.sling.connector.PublicationHandler;
import com.streamx.sling.connector.ResourceInfo;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;
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
@Designate(ocd = PagePublicationHandlerConfig.class)
@ServiceDescription("Publication handler for AEM Pages")
public class PagePublicationHandler extends BasePublicationHandler<Page> {

  private final PageDataService pageDataService;
  private final AtomicReference<String> relPathToNodeWithJcrPropForStreamxResourceType = new AtomicReference<>();
  private final AtomicReference<String> jcrPropNameForStreamxResourceType = new AtomicReference<>();

  @Activate
  public PagePublicationHandler(
      @Reference PageDataService pageDataService,
      @Reference ResourceResolverFactory resolverFactory,
      PagePublicationHandlerConfig config
  ) {
    super(resolverFactory);
    this.pageDataService = pageDataService;
    configure(config);
  }

  @Modified
  void configure(PagePublicationHandlerConfig config) {
    assignConfigValues(
        config.enabled(),
        config.publish_event_type(),
        config.unpublish_event_type(),
        config.should_add_namespace_prefix(),
        config.namespace_prefix()
    );
    relPathToNodeWithJcrPropForStreamxResourceType.set(config.rel_path_to_node_with_jcr_prop_for_streamx_resource_type());
    jcrPropNameForStreamxResourceType.set(config.jcr_prop_name_for_streamx_resource_type());
  }

  @Override
  protected boolean canHandleResource(ResourceInfo resource) {
    if (ResourcePrimaryNodeTypeChecker.isXFPath(resource.getPath())) {
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
  protected Page getPublishPayload(ResourceInfo resourceInfo, Resource resource, ResourceResolver resourceResolver) {
    String content = pageDataService.readContentAsHtml(resource, resourceResolver);
    return new Page(content, getStreamxResourceType(resourceInfo));
  }

  @Override
  protected Page getUnpublishPayload(ResourceInfo resourceInfo) {
    return new Page((ByteBuffer) null, getStreamxResourceType(resourceInfo));
  }

  private String getStreamxResourceType(ResourceInfo resourceInfo) {
    return readProperty(resourceInfo,
        relPathToNodeWithJcrPropForStreamxResourceType.get(),
        jcrPropNameForStreamxResourceType.get()
    );
  }

  @Override
  protected String getPublicationSubject(String resourcePath) {
    return resourcePath + ".html";
  }

}
