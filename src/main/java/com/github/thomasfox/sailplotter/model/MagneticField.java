package com.github.thomasfox.sailplotter.model;

import com.github.thomasfox.sailplotter.model.vector.ThreeDimVector;

public class MagneticField extends ThreeDimVector
{
  /**
   * Angle between geographic north and boat front direction, in arcs.
   *
   * This angle is not measured directly, but computed from compass and
   * gps data.
   */
  public Double compassBearing;

  public MagneticField()
  {
  }

  public MagneticField(Double x, Double y, Double z)
  {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public MagneticField(MagneticField toCopy)
  {
    super(toCopy);
    this.compassBearing = toCopy.compassBearing;
  }

  public static MagneticField copy(MagneticField toCopy)
  {
    if (toCopy == null)
    {
      return null;
    }
    return new MagneticField(toCopy);
  }
}
