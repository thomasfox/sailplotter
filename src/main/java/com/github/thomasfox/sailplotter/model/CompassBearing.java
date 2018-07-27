package com.github.thomasfox.sailplotter.model;

public class CompassBearing
{
  public double fieldStrengthX;

  public double fieldStrengthY;

  public double fieldStrengthZ;

  public CompassBearing()
  {
  }

  public CompassBearing(CompassBearing toCopy)
  {
    this.fieldStrengthX = toCopy.fieldStrengthX;
    this.fieldStrengthY = toCopy.fieldStrengthY;
    this.fieldStrengthZ = toCopy.fieldStrengthZ;
  }

  public static CompassBearing copy(CompassBearing toCopy)
  {
    if (toCopy == null)
    {
      return null;
    }
    return new CompassBearing(toCopy);
  }
}
