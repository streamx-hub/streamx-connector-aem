package com.streamx.blueprints.data;

import java.nio.ByteBuffer;

/**
 * Represents web resource which usually is managed via Digital Asset Management (DAM) solution and
 * is not a technical resource.
 */
public class Asset extends WebResource {

  public static final String TYPE_PUBLISHED = "com.streamx.blueprints.asset.published.v1";
  public static final String TYPE_UNPUBLISHED = "com.streamx.blueprints.asset.unpublished.v1";

  public Asset(ByteBuffer content, String type) {
    super(content, type);
  }

}
