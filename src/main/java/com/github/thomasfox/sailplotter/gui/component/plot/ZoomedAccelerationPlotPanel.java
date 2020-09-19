package com.github.thomasfox.sailplotter.gui.component.plot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.github.thomasfox.sailplotter.gui.component.panel.TimeWindowPosition;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.vector.ThreeDimVector;

public class ZoomedAccelerationPlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final TimeSeriesCollection dataset = new TimeSeriesCollection();

  private final int coordinateIndex ;

  public ZoomedAccelerationPlotPanel(int coordinateIndex)
  {
    this.coordinateIndex = coordinateIndex;
    JFreeChart chart = ChartFactory.createTimeSeriesChart(
        "Acceleration " + ThreeDimVector.getCoordinateNameByIndex(coordinateIndex),
        "Time",
        "Acceleration " + ThreeDimVector.getCoordinateNameByIndex(coordinateIndex) + " [m/s^2]",
        dataset,
        false,
        false,
        false);

    onZoomChanged();
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
    dataset.addSeries(getAccelerationTimeSeries());
  }

  private TimeSeries getAccelerationTimeSeries()
  {
    return zoomedData.getTimeSeries(
        "acceleration",
        Data::getPointsWithAcceleration,
        point -> zoomedData.isInSelectedPosition(point, TimeWindowPosition.IN),
        point -> point.acceleration.getByIndex(coordinateIndex));
  }

  @Override
  protected void onDataChanged()
  {
    resetDataSeries();
  }
}
