package com.github.thomasfox.sailplotter.gui;

import org.jfree.data.xy.XYDataItem;

public class XYLabeledDataItem extends XYDataItem
{
  /** SerialVersionUID. */
  private static final long serialVersionUID = 1L;

  private final String label;

  public XYLabeledDataItem(double x, double y, String label)
  {
    super(x, y);
    this.label = label;
  }

  public XYLabeledDataItem(Number x, Number y, String label)
  {
    super(x, y);
    this.label = label;
  }

  public String getLabel()
  {
    return label;
  }
}
