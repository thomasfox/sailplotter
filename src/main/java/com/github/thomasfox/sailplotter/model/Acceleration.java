package com.github.thomasfox.sailplotter.model;

public class Acceleration
{
  /** Acceleration in device x direction in Nm/s^2. */
  public Double accelerationX;

  /** Acceleration in device y direction in Nm/s^2. */
  public Double accelerationY;

  /** Acceleration in device z direction in Nm/s^2. */
  public Double accelerationZ;

  public Acceleration()
  {
  }

  public Acceleration(Acceleration toCopy)
  {
    this.accelerationX = toCopy.accelerationX;
    this.accelerationY = toCopy.accelerationY;
    this.accelerationZ = toCopy.accelerationZ;
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
