package com.github.thomasfox.sailplotter.gui.component.plot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.model.DataPoint;

public class VelocityBearingPolarPlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final XYSeriesCollection dataset = new XYSeriesCollection();

  public VelocityBearingPolarPlotPanel()
  {
    JFreeChart chart = ChartFactory.createPolarChart("Velocity over rel. Bearing", dataset, false, false, false);

    onDataChanged();
    setChart(chart);
  }

  @Override
  protected void onZoomChanged()
  {
    resetDataSeries();
  }

  private void resetDataSeries()
  {
    dataset.removeAllSeries();
    if (zoomedData.getData() == null)
    {
      return;
    }
    List<List<Double>> velocityBuckets = new ArrayList<>(Constants.NUMBER_OF_BEARING_BINS);
    for (int i = 0; i < Constants.NUMBER_OF_BEARING_BINS; ++i)
    {
      velocityBuckets.add(new ArrayList<Double>());
    }
    for (DataPoint point : zoomedData.getPointsWithLocation())
    {
      if (point.getLocalDateTime().isAfter(zoomedData.getLocationDataStartTime())
          && point.getLocalDateTime().isBefore(zoomedData.getLocationDataEndTime()))
      {
        if (point.location.bearingFromLatLong != null && point.location.velocityFromLatLong != null)
        {
          int bucket = Double.valueOf(
              point.getRelativeBearingInArcs() * Constants.NUMBER_OF_BEARING_BINS / 2 / Math.PI).intValue();
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
    dataset.addSeries(maxVelocity);
    dataset.addSeries(medianVelocity);
  }

  @Override
  protected void onDataChanged()
  {
    resetDataSeries();
  }
}
