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

  /** MAP_X_LENGTH / MAP_Y_LENGTH */
  public static final double MAP_ASPECT_RATIO = 2d;

  /**  How many data points should be added in the zoom plots when a tack is clicked. */
  public static final int NUM_DATAPOINTS_TACK_EXTENSION = 5;

  /** How many Tacks should be the minimum to constitute a tack series. */
  public static final int MIN_TACK_SERIES_SIZE = 4;

  /** If histogram counts are smaller than one divided by this value, the histogram is ignored. */
  public static final int HISTOGRAM_IGNORE_THRESHOLD_FRACTION = 5;
}
