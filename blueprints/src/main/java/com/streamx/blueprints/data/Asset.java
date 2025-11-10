package com.streamx.blueprints.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.ByteBuffer;

/**
 * Represents web resource which usually is managed via Digital Asset Management (DAM) solution and
 * is not a technical resource.
 */
public class Asset extends WebResource {

  public static final String TYPE_PUBLISHED = "com.streamx.blueprints.asset.published.v1";
  public static final String TYPE_UNPUBLISHED = "com.streamx.blueprints.asset.unpublished.v1";

  @JsonCreator
  public Asset(@JsonProperty("content") ByteBuffer content, @JsonProperty("type") String type) {
    super(content, type);
  }

}
