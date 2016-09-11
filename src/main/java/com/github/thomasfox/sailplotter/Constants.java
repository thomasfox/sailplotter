package com.github.thomasfox.sailplotter;

import java.time.ZoneId;

public class Constants
{
  public static final ZoneId timeZoneId = ZoneId.of("Europe/Berlin");

  /** radius of the earth in meters. */
  public static final double EARTH_RADIUS = 6371000;

  public static final double NAUTICAL_MILE = 1852;

  public static final int NUMBER_OF_BEARING_BINS = 50;

  public static final int NUMER_OF_ZOOM_TICKS = 10000;

  public static final double MINIMAL_TACK_LENGTH = 50d;
}
