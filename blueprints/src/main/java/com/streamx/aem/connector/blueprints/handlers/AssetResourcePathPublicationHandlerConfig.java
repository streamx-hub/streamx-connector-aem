package com.streamx.aem.connector.blueprints.handlers;

import com.streamx.blueprints.data.Asset;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition
public @interface AssetResourcePathPublicationHandlerConfig {

  @AttributeDefinition(
      name = "Is Enabled?",
      description = "Indicates whether the configured service is enabled "
          + "and must undertake the supposed actions or not.",
      type = AttributeType.BOOLEAN,
      defaultValue = "true"
  )
  boolean enabled() default true;

  @AttributeDefinition(
      name = "Assets Path - RexExp",
      description = "RegExp to match paths of assets that should be published to StreamX.",
      type = AttributeType.STRING,
      defaultValue = "^/content/.+\\.coreimg\\..+$"
  )
  String assets_path_regexp() default "^/content/.+\\.coreimg\\..+$";

  @AttributeDefinition(
      name = "Publish Event Type",
      description = "Publish event type of Assets to be ingested by StreamX.",
      type = AttributeType.STRING,
      defaultValue = Asset.TYPE_PUBLISHED
  )
  String publish_event_type() default Asset.TYPE_PUBLISHED;

  @AttributeDefinition(
      name = "Unpublish Event Type",
      description = "Unpublish event type of Assets to be ingested by StreamX.",
      type = AttributeType.STRING,
      defaultValue = Asset.TYPE_UNPUBLISHED
  )
  String unpublish_event_type() default Asset.TYPE_UNPUBLISHED;
}
