package com.github.thomasfox.sailplotter.importer.saillogger;

public final class SailLoggerTrackPoint
{
  public Long locT;
  public Float locAcc;
  public Double locLat;
  public Double locLong;
  public Float locBear;
  public Float locVel;
  public float locAlt;
  public Long locDevT;
  public Long magT;
  public Double magX;
  public Double magY;
  public Double magZ;
  public Long accT;
  public Double accX;
  public Double accY;
  public Double accZ;

  public boolean hasGpsData()
  {
    return (locT != null);
  }

  public boolean hasCompassData()
  {
    return (magX != null && magY != null && magZ != null && magT != null);
  }

  public boolean hasAccelerationData()
  {
    return (accX != null && accY != null && accZ != null && accT != null);
  }
}