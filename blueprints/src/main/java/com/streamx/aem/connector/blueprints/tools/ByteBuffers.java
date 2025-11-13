package com.streamx.aem.connector.blueprints.tools;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ByteBuffers {

  private static final Logger LOG = LoggerFactory.getLogger(ByteBuffers.class);

  private ByteBuffers() {
    // no instances
  }

  public static ByteBuffer fromString(String content) {
    if (content == null) {
      return null;
    }
    return fromBytes(content.getBytes(UTF_8));
  }

  public static ByteBuffer fromInputStream(InputStream inputStream) {
    try {
      byte[] bytes = IOUtils.toByteArray(inputStream);
      return fromBytes(bytes);
    } catch (IOException exception) {
      LOG.error("Cannot convert input stream to byte array", exception);
      return fromBytes(new byte[0]);
    }
  }

  private static ByteBuffer fromBytes(byte[] bytes) {
    return ByteBuffer.wrap(bytes);
  }

}
