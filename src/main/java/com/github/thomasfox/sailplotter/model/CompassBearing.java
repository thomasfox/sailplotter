package com.github.thomasfox.sailplotter.model;

public class CompassBearing
{
  /** Field strength in device x direction in microtesla. */
  public Double fieldStrengthX;

  /** Field strength in device y direction in microtesla. */
  public Double fieldStrengthY;

  /** Field strength in device z direction in microtesla. */
  public Double fieldStrengthZ;

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
