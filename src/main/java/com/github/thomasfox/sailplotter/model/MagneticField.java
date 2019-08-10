package com.github.thomasfox.sailplotter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.thomasfox.sailplotter.model.vector.ThreeDimVector;

/**
 * Magnetic field at boat position.
 * The x,y and z components of the field are measured in microtesla,
 * in arbitrary but constant orientation (because we do not know
 * how the mobile phone is fixed on the boat, but we assume
 * that its orientation does not change over time).
 */
public class MagneticField extends ThreeDimVector
{
  /**
   * Computed angle between geographic north and boat front direction, in arcs.
   *
   * This angle is not measured directly, but computed from compass (and
   * gps) data.
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

  @JsonIgnore
  public Double getCompassBearingAs360Degrees()
  {
    if (compassBearing != null)
    {
      return compassBearing / 2 / Math.PI * 360;
    }
    return null;
  }


}
