package com.github.thomasfox.sailplotter.model.vector;

public class ThreeDimMatrix
{
  /** first row, first column. */
  public double a11 = 0d;
  /** first row, second column. */
  public double a12 = 0d;
  /** first row, third column. */
  public double a13 = 0d;
  /** second row, first column. */
  public double a21 = 0d;
  /** second row, second column. */
  public double a22 = 0d;
  /** second row, third column. */
  public double a23 = 0d;
  /** third row, first column. */
  public double a31 = 0d;
  /** third row, second column. */
  public double a32 = 0d;
  /** third row, third column. */
  public double a33 = 0d;

  public static ThreeDimMatrix getIdentityMatrix()
  {
    ThreeDimMatrix result = new ThreeDimMatrix();
    result.a11 = 1;
    result.a22 = 1;
    result.a33 = 1;
    return result;
  }

  public ThreeDimMatrix()
  {
  }

  public ThreeDimMatrix(
      double a11, double a12, double a13,
      double a21, double a22, double a23,
      double a31, double a32, double a33)
  {
    this.a11 = a11;
    this.a12 = a12;
    this.a13 = a13;
    this.a21 = a21;
    this.a22 = a22;
    this.a23 = a23;
    this.a31 = a31;
    this.a32 = a32;
    this.a33 = a33;
  }

  public static ThreeDimMatrix getRotationMatrix(ThreeDimVector rotationStart, ThreeDimVector rotationEnd)
  {
    ThreeDimVector rotationStartUnitVector = rotationStart.normalize();
    ThreeDimVector rotationEndUnitVector = rotationEnd.normalize();
    ThreeDimVector crossProduct = rotationStartUnitVector.crossProduct(rotationEndUnitVector);
    double scalarProduct = rotationStartUnitVector.scalarProduct(rotationEndUnitVector);
    ThreeDimMatrix v = new ThreeDimMatrix(
        0d, -crossProduct.z, crossProduct.y,
        crossProduct.z, 0d, -crossProduct.x,
        -crossProduct.y, crossProduct.x, 0d);
    ThreeDimMatrix result = getIdentityMatrix()
        .add(v)
        .add(v.multiply(v).multiply(1d/(1d + scalarProduct)));
    return result;
  }


  public ThreeDimVector multiply(ThreeDimVector toMultiply)
  {
    ThreeDimVector result = new ThreeDimVector(
        a11 * toMultiply.x + a12 * toMultiply.y + a13 * toMultiply.z,
        a21 * toMultiply.x + a22 * toMultiply.y + a23 * toMultiply.z,
        a31 * toMultiply.x + a32 * toMultiply.y + a33 * toMultiply.z);
    return result;
  }

  public ThreeDimMatrix multiply(double toMultiply)
  {
    ThreeDimMatrix result = new ThreeDimMatrix(
        a11 * toMultiply, a12 * toMultiply, a13 * toMultiply,
        a21 * toMultiply, a22 * toMultiply, a23 * toMultiply,
        a31 * toMultiply, a32 * toMultiply, a33 * toMultiply);
    return result;
  }


  public ThreeDimMatrix add(ThreeDimMatrix toAdd)
  {
    ThreeDimMatrix result = new ThreeDimMatrix();
    result.a11 = this.a11 + toAdd.a11;
    result.a12 = this.a12 + toAdd.a12;
    result.a13 = this.a13 + toAdd.a13;
    result.a21 = this.a21 + toAdd.a21;
    result.a22 = this.a22 + toAdd.a22;
    result.a23 = this.a23 + toAdd.a23;
    result.a31 = this.a31 + toAdd.a31;
    result.a32 = this.a32 + toAdd.a32;
    result.a33 = this.a33 + toAdd.a33;
    return result;
  }

  public ThreeDimMatrix multiply(ThreeDimMatrix toMultiply)
  {
    ThreeDimMatrix result = new ThreeDimMatrix();
    result.a11 = this.a11 * toMultiply.a11 + this.a12 * toMultiply.a21 + this.a13 * toMultiply.a31;
    result.a12 = this.a11 * toMultiply.a12 + this.a12 * toMultiply.a22 + this.a13 * toMultiply.a32;
    result.a13 = this.a11 * toMultiply.a13 + this.a12 * toMultiply.a23 + this.a13 * toMultiply.a33;
    result.a21 = this.a21 * toMultiply.a11 + this.a22 * toMultiply.a21 + this.a23 * toMultiply.a31;
    result.a22 = this.a21 * toMultiply.a12 + this.a22 * toMultiply.a22 + this.a23 * toMultiply.a32;
    result.a23 = this.a21 * toMultiply.a13 + this.a22 * toMultiply.a23 + this.a23 * toMultiply.a33;
    result.a31 = this.a31 * toMultiply.a11 + this.a32 * toMultiply.a21 + this.a33 * toMultiply.a31;
    result.a32 = this.a31 * toMultiply.a12 + this.a32 * toMultiply.a22 + this.a33 * toMultiply.a32;
    result.a33 = this.a31 * toMultiply.a13 + this.a32 * toMultiply.a23 + this.a33 * toMultiply.a33;
    return result;
  }

  public boolean isCloseTo(ThreeDimMatrix other, double tolerance)
  {
    if (Math.abs(a11 - other.a11) > tolerance)
    {
      return false;
    }
    if (Math.abs(a12 - other.a12) > tolerance)
    {
      return false;
    }
    if (Math.abs(a13 - other.a13) > tolerance)
    {
      return false;
    }
    if (Math.abs(a21 - other.a21) > tolerance)
    {
      return false;
    }
    if (Math.abs(a22 - other.a22) > tolerance)
    {
      return false;
    }
    if (Math.abs(a23 - other.a23) > tolerance)
    {
      return false;
    }
    if (Math.abs(a31 - other.a31) > tolerance)
    {
      return false;
    }
    if (Math.abs(a32 - other.a32) > tolerance)
    {
      return false;
    }
    if (Math.abs(a33 - other.a33) > tolerance)
    {
      return false;
    }
    return true;
  }

}
