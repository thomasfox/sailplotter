package com.github.thomasfox.sailplotter.model.vector;

public class ThreeDimVector
{
  public double x;

  public double y;

  public double z;

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
    return Math.sqrt(x * x + y * y + z * z);
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

  public ThreeDimVector multiplyBy(Double factor)
  {
    ThreeDimVector result = new ThreeDimVector(
        x * factor,
        y * factor,
        z * factor);
    return result;
  }

  public ThreeDimVector crossProduct(ThreeDimVector other)
  {
    return new ThreeDimVector(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x);
  }

  public double scalarProduct(ThreeDimVector other)
  {
    return x * other.x + y * other.y + z * other.z;
  }

  public boolean coordinatesAreCloseTo(ThreeDimVector other, double tolerance)
  {
    if (Math.abs(x - other.x) > tolerance)
    {
      return false;
    }
    if (Math.abs(y - other.y) > tolerance)
    {
      return false;
    }
    if (Math.abs(z - other.z) > tolerance)
    {
      return false;
    }
    return true;
  }

  public static ThreeDimVector weightedAdd(
      ThreeDimVector vector1, double weight1,
      ThreeDimVector vector2, double weight2)
  {
    if (weight1 < 0)
    {
      throw new IllegalArgumentException("weight1 must be >= 0, was " + weight1);
    }
    if (weight2 < 0)
    {
      throw new IllegalArgumentException("weight2 must be >= 0, was " + weight2);
    }
    double normalizedWeight1 = weight1 / (weight1 + weight2);
    double normalizedWeight2 = weight2 / (weight1 + weight2);

    ThreeDimVector result = vector1.multiplyBy(normalizedWeight1);
    result.add(vector2.multiplyBy(normalizedWeight2));

    return result;
  }

  @Override
  public String toString()
  {
    return "(" + x + ", " + y + ", " + z + ")";
  }

  public String toString(int decimalPlaces)
  {
    return "(" + formatNumber(x, decimalPlaces) + ", "
        + formatNumber(y, decimalPlaces) + ", "
        + formatNumber(z, decimalPlaces) + ")";
  }

  private String formatNumber(double number, int decimalPlaces)
  {
    return String.format("%." + decimalPlaces + "f", number);
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(x);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(y);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(z);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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

    ThreeDimVector other = (ThreeDimVector) obj;
    if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
    {
      return false;
    }

    if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
    {
      return false;
    }

    if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
    {
      return false;
    }
    return true;
  }
}
