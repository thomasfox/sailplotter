package com.github.thomasfox.sailplotter.gui;

import org.jfree.data.xy.XYDataItem;

public class XYSailDataItem extends XYDataItem
{
  /** SerialVersionUID. */
  private static final long serialVersionUID = 1L;

  private final String label;

  private Integer startOfTack;

  private Integer endOfTack;

  private boolean tackMainPartLimit;

  public XYSailDataItem(double x, double y, String label)
  {
    super(x, y);
    this.label = label;
  }

  public XYSailDataItem(Number x, Number y, String label)
  {
    super(x, y);
    this.label = label;
  }

  public String getLabel()
  {
    return label;
  }

  public Integer getStartOfTack()
  {
    return startOfTack;
  }

  public boolean isStartOfTack()
  {
    return startOfTack != null;
  }

  public void setStartOfTack(int tackIndex)
  {
    this.startOfTack = tackIndex;
  }

  public Integer getEndOfTack()
  {
    return endOfTack;
  }

  public boolean isEndOfTack()
  {
    return endOfTack != null;
  }

  public void setEndOfTack(int tackIndex)
  {
    this.endOfTack = tackIndex;
  }

  public boolean isTackMainPartLimit()
  {
    return tackMainPartLimit;
  }

  public void setTackMainPartLimit(boolean tackMainPartLimit)
  {
    this.tackMainPartLimit = tackMainPartLimit;
  }

  @Override
  public String toString()
  {
    StringBuilder result = new StringBuilder(getLabel());
    if (isStartOfTack())
    {
      result.append(" Start of Tack ").append(startOfTack);
    }
    if (isEndOfTack())
    {
      result.append(" End of Tack ").append(endOfTack);
    }
    return result.toString();
  }
}
