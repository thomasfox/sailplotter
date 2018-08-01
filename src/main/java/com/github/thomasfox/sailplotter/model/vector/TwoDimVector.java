package com.github.thomasfox.sailplotter.model.vector;

public class TwoDimVector
{
  public Double x;

  public Double y;

  public TwoDimVector()
  {
  }

  public TwoDimVector(Double x, Double y)
  {
    this.x = x;
    this.y = y;
  }

  public TwoDimVector(TwoDimVector toCopy)
  {
    this.x = toCopy.x;
    this.y = toCopy.y;
  }

  public static TwoDimVector copy(TwoDimVector toCopy)
  {
    if (toCopy == null)
    {
      return null;
    }
    return new TwoDimVector(toCopy);
  }

  /**
   * Gets the bearing of this point to the X direction.
   *
   * @return the bearing to the X direction, in arcs, in the range [0, 2*PI[,
   *         or null if the vector has the length 0.
   */
  public Double getBearingToXInArcs()
  {
    Double result = null;
    if (y != 0)
    {
      result = Math.atan(x / y);
      if (y < 0)
      {
        result += Math.PI;
      }
    }
    else if (x > 0)
    {
      result = Math.PI / 2;
    }
    else if (x < 0)
    {
      result =  3 * Math.PI / 2;
    }
    while (result != null && result < 0)
    {
      result += 2 * Math.PI;
    }
    return result;
  }

  @Override
  public String toString()
  {
    return "(" + x + ", " + y + ")";
  }
}
