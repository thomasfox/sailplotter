package com.github.thomasfox.sailplotter.model;

import java.text.DecimalFormat;

public class Tack
{
  public PointOfSail pointOfSail;

  public DataPoint start;

  public int startIndex;

  public DataPoint end;

  public int endIndex;

  public Double windDirection;

  public ManeuverType maneuverTypeAtStart;

  public ManeuverType maneuverTypeAtEnd;

  /**
   * @return the length of the tack in meters
   */
  public double getLength()
  {
    return end.distance(start);
  }

  /**
   * @return the duration of the tack in milliseconds
   */
  public long getDuration()
  {
    return end.time - start.time;
  }

  public Double getAverageBearingInArcs()
  {
    if (end == null)
    {
      return null;
    }
    return start.getBearingTo(end);
  }


  public Double getAverageRelativeBearingInArcs()
  {
    if (windDirection == null || end == null)
    {
      return null;
    }
    double result = start.getBearingTo(end) - windDirection;
    if (result < 0)
    {
      result += 2 * Math.PI;
    }
    return result;
  }

  public Double getAverageRelativeBearingInDegrees()
  {
    Double averageRelativeBearingInArcs = getAverageRelativeBearingInArcs();
    if (averageRelativeBearingInArcs == null)
    {
      return null;
    }
    double result = averageRelativeBearingInArcs * 360 / 2 / Math.PI;
    if (result > 180d)
    {
      result = result - 360d;
    }
    return result;
  }

  public Double getAverageVelocityInKnots()
  {
    if (end == null)
    {
      return null;
    }
    return end.getVelocityInKnotsBetween(start);
  }

  public void start(DataPoint startPoint, int dataPointIndex)
  {
    start = startPoint;
    startIndex = dataPointIndex;
    pointOfSail = startPoint.getPointOfSail();
    windDirection = startPoint.windDirection;
  }

  public void end(DataPoint point, int dataPointIndex)
  {
    end = point;
    endIndex = dataPointIndex;
    pointOfSail = PointOfSail.ofRelativeBearing(start.getRelativeBearingTo(end, windDirection));
  }


  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder();
    result.append("Tack: ").append(pointOfSail);
    if (end != null)
    {
      result.append(" Duration:").append(new DecimalFormat("0.0").format(getDuration() / 1000d));
      result.append(" Length:").append(new DecimalFormat("0").format(getLength()));
    }
    return result.toString();
  }
}
