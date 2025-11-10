package com.streamx.aem.connector.blueprints;

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
  private final AtomicReference<FragmentPublicationHandlerConfig> config;

  @Activate
  public FragmentPublicationHandler(
      @Reference PageDataService pageDataService,
      @Reference ResourceResolverFactory resourceResolverFactory,
      FragmentPublicationHandlerConfig config
  ) {
    super(resourceResolverFactory);
    this.pageDataService = pageDataService;
    this.config = new AtomicReference<>(config);
  }

  @Modified
  void configure(FragmentPublicationHandlerConfig config) {
    this.config.set(config);
  }

  @Override
  public boolean canHandle(ResourceInfo resource) {
    if (!config.get().enabled()) {
      return false;
    }

    try (ResourceResolver resourceResolver = createResourceResolver()) {
      return ResourcePrimaryNodeTypeChecker.isXF(resource, resourceResolver);
    }
  }

  @Override
  protected String getPublishEventType() {
    return config.get().publish_event_type();
  }

  @Override
  protected String getUnpublishEventType() {
    return config.get().unpublish_event_type();
  }

  @Override
  protected String getPublicationSubject(String resourcePath) {
    return String.format("%s.html", resourcePath);
  }

  @Override
  protected Fragment getPublishPayload(ResourceInfo resourceInfo, Resource resource, ResourceResolver resourceResolver) {
    String content = pageDataService.readContentAsHtml(resource, resourceResolver);
    return new Fragment(content, getResourceType(resourceInfo));
  }

  @Override
  protected Fragment getUnpublishPayload(ResourceInfo resourceInfo) {
    return new Fragment((ByteBuffer) null, getResourceType(resourceInfo));
  }

  private String getResourceType(ResourceInfo resourceInfo) {
    return readProperty(resourceInfo,
        config.get().rel_path_to_node_with_jcr_prop_for_streamx_resource_type(),
        config.get().jcr_prop_name_for_streamx_resource_type()
    );
  }

}
