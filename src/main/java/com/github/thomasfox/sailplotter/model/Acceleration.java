package com.github.thomasfox.sailplotter.model;

import com.github.thomasfox.sailplotter.model.vector.ThreeDimVector;

/**
 * Acceleration of device.
 * The x,y and z components of the field are measured in Nm/s^2,
 * in arbitrary but constant orientation (because we do not know
 * how the mobile phone is fixed on the boat, but we assume
 * that its orientation does not change over time).
 */
public class Acceleration extends ThreeDimVector
{
  /** heel angle of Boat in arcs. */
  public Double heel;

  /** roll angle of Boat in arcs. */
  public Double roll;

  public Acceleration()
  {
  }

  public Acceleration(Double x, Double y, Double z)
  {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Acceleration(Acceleration toCopy)
  {
    super(toCopy);
    this.heel = toCopy.heel;
    this.roll = toCopy.roll;
  }

  public static Acceleration copy(Acceleration toCopy)
  {
    if (toCopy == null)
    {
      return null;
    }
    return new Acceleration(toCopy);
  }
}
