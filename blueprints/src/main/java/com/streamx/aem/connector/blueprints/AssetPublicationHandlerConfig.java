package com.streamx.aem.connector.blueprints;

import com.streamx.blueprints.data.Asset;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "StreamX Blueprints - Asset Publication Handler Config")
public @interface AssetPublicationHandlerConfig {

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
      defaultValue = "^/content/dam/.*$"
  )
  String assets_path_regexp() default "^/content/dam/.*$";

  @AttributeDefinition(
      name = "Publish Event Type",
      description = "Publish event type of data to be ingested by StreamX.",
      type = AttributeType.STRING,
      defaultValue = Asset.TYPE_PUBLISHED
  )
  String publish_event_type() default Asset.TYPE_PUBLISHED;

  @AttributeDefinition(
      name = "Unpublish Event Type",
      description = "Unpublish event type of data to be ingested by StreamX.",
      type = AttributeType.STRING,
      defaultValue = Asset.TYPE_UNPUBLISHED
  )
  String unpublish_event_type() default Asset.TYPE_UNPUBLISHED;

  @AttributeDefinition(
      name = "Name of JCR property for StreamX resource type",
      description = "If the resource that is being published has a JCR property with the specified "
          + "name, the value of that property will be set as "
          + "the type of the Asset resource ingested into StreamX.",
      type = AttributeType.STRING,
      defaultValue = StringUtils.EMPTY
  )
  String jcr_prop_name_for_streamx_resource_type() default StringUtils.EMPTY;
}
