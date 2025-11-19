package com.streamx.blueprints.data;

import java.nio.ByteBuffer;

/**
 * Represents web page.
 */
public class Page extends WebResource {

  public static final String TYPE_PUBLISHED = "com.streamx.blueprints.page.published.v1";
  public static final String TYPE_UNPUBLISHED = "com.streamx.blueprints.page.unpublished.v1";

  public Page(ByteBuffer content, String type) {
    super(content, type);
  }

  public Page(String content, String type) {
    super(content, type);
  }
}
