package com.github.thomasfox.sailplotter.model.vector;

public class CoordinateSystem
{
  /** The x direction for this coordinate system. */
  public ThreeDimVector x;

  /** The y direction for this coordinate system. */
  public ThreeDimVector y;

  /** The z direction for this coordinate system. */
  public ThreeDimVector z;

  public CoordinateSystem()
  {
  }

  public CoordinateSystem(ThreeDimVector x, ThreeDimVector y, ThreeDimVector z)
  {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public CoordinateSystem(CoordinateSystem toCopy)
  {
    this.x = toCopy.x;
    this.y = toCopy.y;
    this.z = toCopy.z;
  }

  public double getX(ThreeDimVector in)
  {
    return x.scalarProduct(in);
  }

  public double getY(ThreeDimVector in)
  {
    return y.scalarProduct(in);
  }

  public double getZ(ThreeDimVector in)
  {
    return z.scalarProduct(in);
  }

  @Override
  public String toString()
  {
    return "[" + x + ", " + y + ", " + z + "]";
  }

  public String toString(int decimalPlaces)
  {
    return "[" + x.toString(decimalPlaces) + ", "
        + y.toString(decimalPlaces) + ", "
        + z.toString(decimalPlaces) + "]";
  }


  public CoordinateSystem getRotatedAroundZ(double arcs)
  {
    ThreeDimVector rotatedX = x.multiplyBy(Math.cos(arcs));
    rotatedX.add(y.multiplyBy(Math.sin(arcs)));
    ThreeDimMatrix rotationMatrix = ThreeDimMatrix.getRotationMatrix(x, rotatedX);
    CoordinateSystem result = new CoordinateSystem(
        rotationMatrix.multiply(x),
        rotationMatrix.multiply(y),
        rotationMatrix.multiply(z));
    return result;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((x == null) ? 0 : x.hashCode());
    result = prime * result + ((y == null) ? 0 : y.hashCode());
    result = prime * result + ((z == null) ? 0 : z.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }

    CoordinateSystem other = (CoordinateSystem) obj;
    if (x == null)
    {
      if (other.x != null)
      {
        return false;
      }
    }
    else if (!x.equals(other.x))
    {
      return false;
    }

    if (y == null)
    {
      if (other.y != null)
      {
        return false;
      }
    }
    else if (!y.equals(other.y))
    {
      return false;
    }

    if (z == null)
    {
      if (other.z != null)
      {
        return false;
      }
    }
    else if (!z.equals(other.z))
    {
      return false;
    }
    return true;
  }

  public boolean coordinatesAreCloseTo(CoordinateSystem other, double tolerance)
  {
    return (
        x.coordinatesAreCloseTo(other.x, tolerance)
        && y.coordinatesAreCloseTo(other.y, tolerance)
        && z.coordinatesAreCloseTo(other.z, tolerance));
  }
}
