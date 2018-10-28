package com.github.thomasfox.sailplotter.gui.component.plot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;

public class VelocityBearingPolarPlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final XYSeriesCollection dataset = new XYSeriesCollection();

  public VelocityBearingPolarPlotPanel(Data data, int zoomWindowLocationStartIndex, int zoomWindowLocationSize)
  {
    super(data, zoomWindowLocationStartIndex, zoomWindowLocationSize);
    JFreeChart chart = ChartFactory.createPolarChart("Velocity over rel. Bearing", dataset, false, false, false);

    onDataChanged();
    addPanelFor(chart);
  }

  @Override
  protected void onZoomChanged()
  {
  }

  @Override
  protected void onDataChanged()
  {
    List<List<Double>> velocityBuckets = new ArrayList<>(Constants.NUMBER_OF_BEARING_BINS);
    for (int i = 0; i < Constants.NUMBER_OF_BEARING_BINS; ++i)
    {
      velocityBuckets.add(new ArrayList<Double>());
    }
    for (DataPoint point : data.getPointsWithLocation())
    {
      if (point.getLocalDateTime().isAfter(getLocationDataStartTime())
          && point.getLocalDateTime().isBefore(getLocationDataEndTime()))
      {
        if (point.location.bearingFromLatLong != null && point.location.velocityFromLatLong != null)
        {
          int bucket = new Double(point.getRelativeBearingInArcs() * Constants.NUMBER_OF_BEARING_BINS / 2 / Math.PI).intValue();
          velocityBuckets.get(bucket).add(point.location.velocityFromLatLong);
        }
      }
    }
    int max = 0;
    for (List<Double> bucket : velocityBuckets)
    {
      if (bucket.size() > max)
      {
        max = bucket.size();
      }
    }
    for (List<Double> bucket : velocityBuckets)
    {
      if (bucket.size() < max / Constants.HISTOGRAM_IGNORE_THRESHOLD_FRACTION)
      {
        bucket.clear();
      }
    }

    XYSeries maxVelocity = new XYSeries("maxVelocity");
    XYSeries medianVelocity = new XYSeries("medianVelocity");
    for (int i = 0; i < Constants.NUMBER_OF_BEARING_BINS; ++i)
    {
      List<Double> velocityDistribution = velocityBuckets.get(i);
      Collections.sort(velocityDistribution);
      if (!velocityDistribution.isEmpty())
      {
        maxVelocity.add(i * 360d / Constants.NUMBER_OF_BEARING_BINS, velocityDistribution.get(velocityDistribution.size() - 1));
      }
      else
      {
        maxVelocity.add(i * 360d / Constants.NUMBER_OF_BEARING_BINS, 0);
      }
      if (velocityDistribution.size() >= 1)
      {
        medianVelocity.add(i * 360d / Constants.NUMBER_OF_BEARING_BINS, velocityDistribution.get(velocityDistribution.size() / 2));
      }
      else
      {
        medianVelocity.add(i * 360d / Constants.NUMBER_OF_BEARING_BINS, 0);
      }
    }
    dataset.removeAllSeries();
    dataset.addSeries(maxVelocity);
    dataset.addSeries(medianVelocity);
  }
}
