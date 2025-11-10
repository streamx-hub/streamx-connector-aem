package com.streamx.aem.connector.blueprints;

import com.streamx.blueprints.data.WebResource;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition
public @interface ClientlibsPublicationHandlerConfig {

  @AttributeDefinition(
      name = "Is Enabled?",
      description = "Indicates whether the configured service is enabled "
          + "and must undertake the supposed actions or not.",
      type = AttributeType.BOOLEAN,
      defaultValue = "true"
  )
  boolean enabled() default true;

  @AttributeDefinition(
      name = "Clientlibs Path - RexExp",
      description = "RegExp to match paths of clientlibs that should be published to StreamX.",
      type = AttributeType.STRING,
      defaultValue = "^/etc\\.clientlibs/.+"
  )
  String clientlibs_path_regexp() default "^/etc\\.clientlibs/.+";

  @AttributeDefinition(
      name = "Publish Event Type",
      description = "Publish event type of data to be ingested by StreamX.",
      type = AttributeType.STRING,
      defaultValue = WebResource.TYPE_PUBLISHED
  )
  String publish_event_type() default WebResource.TYPE_PUBLISHED;

  @AttributeDefinition(
      name = "Unpublish Event Type",
      description = "Unpublish event type of data to be ingested by StreamX.",
      type = AttributeType.STRING,
      defaultValue = WebResource.TYPE_UNPUBLISHED
  )
  String unpublish_event_type() default WebResource.TYPE_UNPUBLISHED;
}
