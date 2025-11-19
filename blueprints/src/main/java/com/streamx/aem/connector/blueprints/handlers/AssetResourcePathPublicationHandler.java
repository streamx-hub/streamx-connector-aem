package com.streamx.aem.connector.blueprints.handlers;

import com.streamx.aem.connector.blueprints.tools.ByteBuffers;
import com.streamx.blueprints.data.Asset;
import com.streamx.sling.connector.PublicationHandler;
import com.streamx.sling.connector.handlers.resourcepath.ResourcePathPublicationHandler;
import com.streamx.sling.connector.handlers.resourcepath.ResourcePathPublicationHandlerConfig;
import java.io.InputStream;
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
    service = {
        ResourcePathPublicationHandler.class, PublicationHandler.class, AssetResourcePathPublicationHandler.class
    },
    immediate = true,
    configurationPolicy = ConfigurationPolicy.REQUIRE
)
@Designate(ocd = AssetResourcePathPublicationHandlerConfig.class)
public class AssetResourcePathPublicationHandler extends ResourcePathPublicationHandler<Asset> {

  private final AtomicReference<AssetResourcePathPublicationHandlerConfig> config;

  @Activate
  public AssetResourcePathPublicationHandler(
      @Reference ResourceResolverFactory resourceResolverFactory,
      @Reference SlingRequestProcessor slingRequestProcessor,
      AssetResourcePathPublicationHandlerConfig config
  ) {
    super(resourceResolverFactory, slingRequestProcessor);
    this.config = new AtomicReference<>(config);
  }

  @Modified
  void configure(AssetResourcePathPublicationHandlerConfig config) {
    this.config.set(config);
  }

  @Override
  public ResourcePathPublicationHandlerConfig configuration() {
    return new ResourcePathPublicationHandlerConfig() {
      @Override
      public String resourcePathRegex() {
        return config.get().assets_path_regexp();
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
  protected Asset payloadForPublish(InputStream inputStream) {
    return new Asset(ByteBuffers.fromInputStream(inputStream), null);
  }

  @Override
  protected Asset payloadForUnpublish() {
    return new Asset(null, null);
  }

  @Override
  public String getId() {
    return this.getClass().getSimpleName();
  }
}
