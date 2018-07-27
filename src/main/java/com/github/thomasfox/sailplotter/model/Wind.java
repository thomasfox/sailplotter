package com.github.thomasfox.sailplotter.model;

public class Wind
{
  /** wind direction in arcs */
  public Double direction;

  /** wind velocity in m/s */
  public Double velocity;

  public Wind()
  {
  }

  public Wind(Wind toCopy)
  {
   this.direction = toCopy.direction;
    this.velocity = toCopy.velocity;
  }

  public static Wind copy(Wind toCopy)
  {
    if (toCopy == null)
    {
      return null;
    }
    return new Wind(toCopy);
  }
}
