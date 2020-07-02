package com.github.thomasfox.sailplotter.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.Test;

public class LocationTest
{
  @Test
  public void testSetXY()
  {
    // prepare
    Location location = new Location();

    // execute
    location.setXY(137345, 2500000);

    // verify
    assertThat(location.getX()).isCloseTo(137345d, within(0.00001d));
    assertThat(location.getY()).isCloseTo(2500000d, within(0.00001d));
  }

}
