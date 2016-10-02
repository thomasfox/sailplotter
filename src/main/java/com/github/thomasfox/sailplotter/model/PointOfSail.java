package com.github.thomasfox.sailplotter.model;

public enum PointOfSail
{
//  INTO_WIND_STARBOARD(0,20),
  CLOSE_HAULED_STARBOARD(0, 70),
//  CLOSE_REACH_STARBOARD(60,80),
  BEAM_REACH_STARBOARD(70,110),
  BROAD_REACH_STARBOARD(110,180),
//  RUNNING(160,200),
  BROAD_REACH_PORT(180, 250),
  BEAM_REACH_PORT(250,290),
//  CLOSE_REACH_PORT(280, 300),
  CLOSE_HAULED_PORT(290,360);
//  INTO_WIND_PORT(340, 360);

  private double startRelativeBearing;

  private double endRelativeBearing;

  private PointOfSail(int startDegrees, int endDegrees)
  {
    this.startRelativeBearing = 2 * Math.PI * startDegrees / 360d;
    this.endRelativeBearing = 2 * Math.PI * endDegrees / 360d;
  }

  public static PointOfSail ofRelativeBearing(Double relativeBearing)
  {
    if (relativeBearing == null)
    {
      return null;
    }
    for (PointOfSail candidate: values())
    {
      if (candidate.startRelativeBearing <= relativeBearing && candidate.endRelativeBearing > relativeBearing)
      {
        return candidate;
      }
    }
    throw new IllegalArgumentException("Not found: " + relativeBearing);
  }
}
