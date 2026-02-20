package com.streamx.aem.connector.blueprints.tools;

import java.util.Optional;

public final class NamespacePrefixAdder {

  private NamespacePrefixAdder() {
    // no instances
  }

  public static String add(String input, String namespacePrefix) {
    String prefixToAdd = Optional.ofNullable(namespacePrefix).orElse("");
    return prefixToAdd + ":" + input;
  }
}
