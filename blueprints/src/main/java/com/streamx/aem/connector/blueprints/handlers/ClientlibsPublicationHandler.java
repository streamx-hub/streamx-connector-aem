package com.streamx.aem.connector.blueprints.handlers;

import com.streamx.aem.connector.blueprints.tools.ByteBuffers;
import com.streamx.blueprints.data.WebResource;
import com.streamx.sling.connector.PublicationHandler;
import com.streamx.sling.connector.handlers.resourcepath.ResourcePathPublicationHandlerConfig;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.engine.SlingRequestProcessor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

@Component(
    service = PublicationHandler.class,
    immediate = true,
    configurationPolicy = ConfigurationPolicy.REQUIRE
)
@Designate(ocd = ClientlibsPublicationHandlerConfig.class)
public class ClientlibsPublicationHandler extends BaseNamespaceAwareResourcePathPublicationHandler<WebResource> {

  private final AtomicReference<ClientlibsPublicationHandlerConfig> config = new AtomicReference<>();

  @Activate
  public ClientlibsPublicationHandler(
      @Reference ResourceResolverFactory resourceResolverFactory,
      @Reference SlingRequestProcessor slingRequestProcessor,
      ClientlibsPublicationHandlerConfig config
  ) {
    super(resourceResolverFactory, slingRequestProcessor);
    configure(config);
    assignConfigValues(
        config.should_add_namespace_prefix(),
        config.namespace_prefix()
    );
  }

  @Modified
  void configure(ClientlibsPublicationHandlerConfig config) {
    this.config.set(config);
  }

  @Override
  public ResourcePathPublicationHandlerConfig configuration() {
    return new ResourcePathPublicationHandlerConfig() {
      @Override
      public String resourcePathRegex() {
        return config.get().clientlibs_path_regexp();
      }

      @Override
      public String publishEventType() {
        return config.get().publish_event_type();
      }

      @Override
      public String unpublishEventType() {
        return config.get().unpublish_event_type();
      }

      @Override
      public boolean isEnabled() {
        return config.get().enabled();
      }
    };
  }

  @Override
  protected WebResource payloadForPublish(InputStream inputStream) {
    return new WebResource(ByteBuffers.fromInputStream(inputStream), null);
  }

  @Override
  protected WebResource payloadForUnpublish() {
    return new WebResource((ByteBuffer) null, null);
  }

  @Override
  public String getId() {
    return this.getClass().getSimpleName();
  }
}
