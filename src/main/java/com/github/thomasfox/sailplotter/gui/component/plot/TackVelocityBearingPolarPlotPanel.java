package com.github.thomasfox.sailplotter.gui.component.plot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.github.thomasfox.sailplotter.model.Tack;

public class TackVelocityBearingPolarPlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final XYSeriesCollection dataset = new XYSeriesCollection();

  public TackVelocityBearingPolarPlotPanel()
  {
    JFreeChart chart = ChartFactory.createPolarChart("Tack Velocity over rel. Bearing", dataset, false, true, false);
    PolarPlot plot = (PolarPlot) chart.getPlot();
    PolarScatterRenderer renderer = new PolarScatterRenderer();
    renderer.setBaseToolTipGenerator(new XYTooltipFromLabelGenerator());
    plot.setRenderer(renderer);

    onZoomChanged();
    addPanelFor(chart);
  }

  @Override
  protected void onZoomChanged()
  {
    dataset.removeAllSeries();
    if (zoomedData.getData() == null)
    {
      return;
    }
    XYSeries tackVelocity = new XYSeries("tackVelocity", false, true);
    for (Tack tack : zoomedData.getData().getTackList())
    {
      if (tack.end.getLocalDateTime().isAfter(zoomedData.getLocationDataStartTime())
          && tack.start.getLocalDateTime().isBefore(zoomedData.getLocationDataEndTime())
          && tack.hasMainPoints())
      {
        if (tack.getRelativeBearingInDegrees() != null && tack.getVelocityInKnots() != null)
        {
          tackVelocity.add(new XYSailDataItem(
              tack.getRelativeBearingInDegrees(),
              tack.getVelocityInKnots(),
              tack.getLabel()));
        }
      }
    }
    dataset.addSeries(tackVelocity);
  }

  @Override
  protected void onDataChanged()
  {
  }
}
