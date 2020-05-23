package com.github.thomasfox.sailplotter.gui.component.plot;

import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.gui.component.panel.TimeWindowPosition;
import com.github.thomasfox.sailplotter.model.DataPoint;

public class ZoomedBearingHistogramPlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final SimpleHistogramDataset dataset
      = new SimpleHistogramDataset("Relative Bearing");

  List<SimpleHistogramBin> bearingHistogramBins = new ArrayList<>();

  public ZoomedBearingHistogramPlotPanel(int zoomWindowLocationStartIndex, int zoomWindowLocationSize)
  {
    super(zoomWindowLocationStartIndex, zoomWindowLocationSize);
    dataset.setAdjustForBinSize(false);
    for (int i = 0; i < Constants.NUMBER_OF_BEARING_BINS; ++i)
    {
      SimpleHistogramBin bin = new SimpleHistogramBin(
          (i * 360d / Constants.NUMBER_OF_BEARING_BINS) - 180d,
          ((i + 1) * 360d / Constants.NUMBER_OF_BEARING_BINS) - 180d,
          true,
          false);
      bearingHistogramBins.add(bin);
      dataset.addBin(bin);
    }

    JFreeChart chart = ChartFactory.createHistogram(
        "Relative Bearing",
        "Relative Bearing [°]",
        "Occurances",
        dataset,
        PlotOrientation.VERTICAL,
        false,
        false,
        false);

    onZoomChanged();
    addPanelFor(chart);
  }

  @Override
  protected void onZoomChanged()
  {
    for (SimpleHistogramBin bin : bearingHistogramBins)
    {
      bin.setItemCount(0);
    }
    for (DataPoint point : getLocationSubset(TimeWindowPosition.IN))
    {
      if (point.location.bearingFromLatLong != null)
      {
        double relativeBearing = point.getRelativeBearingAs360Degrees();
        dataset.addObservation(relativeBearing);
      }
    }
  }

  @Override
  protected void onDataChanged()
  {
  }
}
