package com.github.thomasfox.sailplotter.gui.component.plot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeriesCollection;

import com.github.thomasfox.sailplotter.gui.component.panel.TimeWindowPosition;
import com.github.thomasfox.sailplotter.model.vector.ThreeDimVector;

public class ZoomedMagneticFieldPlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final TimeSeriesCollection dataset = new TimeSeriesCollection();

  private final int coordinateIndex ;

  public ZoomedMagneticFieldPlotPanel(
      int zoomWindowLocationStartIndex,
      int zoomWindowLocationSize,
      int coordinateIndex)
  {
    super(zoomWindowLocationStartIndex, zoomWindowLocationSize);
    this.coordinateIndex = coordinateIndex;
    JFreeChart chart = ChartFactory.createTimeSeriesChart(
        "Magnetic Field " + ThreeDimVector.getCoordinateNameByIndex(coordinateIndex),
        "Time",
        "Magnetic Field " + ThreeDimVector.getCoordinateNameByIndex(coordinateIndex) + " [µT]",
        dataset,
        false,
        false,
        false);

    onZoomChanged();
    addPanelFor(chart);
  }

  @Override
  protected void onZoomChanged()
  {
    dataset.removeAllSeries();
    dataset.addSeries(zoomedData.getMagneticFieldTimeSeries(coordinateIndex, TimeWindowPosition.IN));
  }

  @Override
  protected void onDataChanged()
  {
  }
}
