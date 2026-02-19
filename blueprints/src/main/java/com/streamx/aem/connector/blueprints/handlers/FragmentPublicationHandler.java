package com.streamx.aem.connector.blueprints.handlers;

import com.streamx.aem.connector.blueprints.pagedata.PageDataService;
import com.streamx.aem.connector.blueprints.tools.ResourcePrimaryNodeTypeChecker;
import com.streamx.blueprints.data.Fragment;
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
@Designate(ocd = FragmentPublicationHandlerConfig.class)
@ServiceDescription("Publication handler for Experience Fragments")
public class FragmentPublicationHandler extends BasePublicationHandler<Fragment> {

  private final PageDataService pageDataService;
  private final AtomicReference<String> relPathToNodeWithJcrPropForStreamxResourceType = new AtomicReference<>();
  private final AtomicReference<String> jcrPropNameForStreamxResourceType = new AtomicReference<>();

  @Activate
  public FragmentPublicationHandler(
      @Reference PageDataService pageDataService,
      @Reference ResourceResolverFactory resourceResolverFactory,
      FragmentPublicationHandlerConfig config
  ) {
    super(resourceResolverFactory);
    this.pageDataService = pageDataService;
    configure(config);
  }

  @Modified
  void configure(FragmentPublicationHandlerConfig config) {
    assignConfigValues(
        config.enabled(),
        config.publish_event_type(),
        config.unpublish_event_type()
    );
    relPathToNodeWithJcrPropForStreamxResourceType.set(config.rel_path_to_node_with_jcr_prop_for_streamx_resource_type());
    jcrPropNameForStreamxResourceType.set(config.jcr_prop_name_for_streamx_resource_type());
  }

  @Override
  protected boolean canHandleResource(ResourceInfo resource) {
    try (ResourceResolver resourceResolver = createResourceResolver()) {
      return ResourcePrimaryNodeTypeChecker.isXF(resource, resourceResolver);
    }
  }

  @Override
  protected String getPublicationSubject(String resourcePath) {
    return String.format("%s.html", resourcePath);
  }

  @Override
  protected Fragment getPublishPayload(ResourceInfo resourceInfo, Resource resource, ResourceResolver resourceResolver) {
    String content = pageDataService.readContentAsHtml(resource, resourceResolver);
    return new Fragment(content, getStreamxResourceType(resourceInfo));
  }

  @Override
  protected Fragment getUnpublishPayload(ResourceInfo resourceInfo) {
    return new Fragment((ByteBuffer) null, getStreamxResourceType(resourceInfo));
  }

  private String getStreamxResourceType(ResourceInfo resourceInfo) {
    return readProperty(resourceInfo,
        relPathToNodeWithJcrPropForStreamxResourceType.get(),
        jcrPropNameForStreamxResourceType.get()
    );
  }

}
