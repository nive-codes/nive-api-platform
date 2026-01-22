package com.nive.web.filter.ratelimiter.enums;

import java.util.Arrays;

/**
 * @author nive
 * @class ApiMethodWeight
 * @desc [클래스 설명]
 * @since 2025-07-01
 */
public enum ApiMethodWeight {
  GET(1),
  POST(2),
  PUT(3),
  PATCH(3),
  DELETE(4);

  private final int weight;

  ApiMethodWeight(int weight) {
    this.weight = weight;
  }

  public int getWeight() {
    return weight;
  }

  public static int resolve(String method) {
    return Arrays.stream(values())
            .filter(w -> w.name().equalsIgnoreCase(method))
            .findFirst()
            .map(ApiMethodWeight::getWeight)
            .orElse(1); // default weight
  }
}
