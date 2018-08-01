package com.github.thomasfox.sailplotter.model.vector;

public class ThreeDimVector
{
  public Double x;

  public Double y;

  public Double z;

  public ThreeDimVector()
  {
  }

  public ThreeDimVector(Double x, Double y, Double z)
  {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public ThreeDimVector(ThreeDimVector toCopy)
  {
    this.x = toCopy.x;
    this.y = toCopy.y;
    this.z = toCopy.z;
  }

  public static ThreeDimVector copy(ThreeDimVector toCopy)
  {
    if (toCopy == null)
    {
      return null;
    }
    return new ThreeDimVector(toCopy);
  }

  public double length()
  {
    return Math.sqrt(x*x + y*y + z*z);
  }

  public ThreeDimVector normalize()
  {
    double length = length();
    return new ThreeDimVector(x / length, y / length, z / length);
  }

  public void add(ThreeDimVector other)
  {
    this.x += other.x;
    this.y += other.y;
    this.z += other.z;
  }

  public void multiplyBy(Double factor)
  {
    this.x *= factor;
    this.y *= factor;
    this.z *= factor;
  }

  public ThreeDimVector crossProduct(ThreeDimVector other)
  {
    return new ThreeDimVector(
        y*other.z - z*other.y,
        z*other.x - x*other.z,
        x*other.y - y*other.x);
  }

  public double scalarProduct(ThreeDimVector other)
  {
    return x*other.x + y*other.y + z*other.z;
  }

  @Override
  public String toString()
  {
    return "(" + x + ", " + y + ", " + z + ")";
  }
}
