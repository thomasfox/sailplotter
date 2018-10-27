package com.github.thomasfox.sailplotter.gui.plot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.github.thomasfox.sailplotter.gui.PolarScatterRenderer;
import com.github.thomasfox.sailplotter.gui.XYSailDataItem;
import com.github.thomasfox.sailplotter.gui.XYTooltipFromLabelGenerator;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.Tack;

public class TackVelocityBearingPolarPlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final XYSeriesCollection dataset = new XYSeriesCollection();

  public TackVelocityBearingPolarPlotPanel(Data data, int zoomWindowLocationStartIndex, int zoomWindowLocationSize)
  {
    super(data, zoomWindowLocationStartIndex, zoomWindowLocationSize);
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
    XYSeries tackVelocity = new XYSeries("tackVelocity", false, true);
    for (Tack tack : data.getTackList())
    {
      if (tack.end.getLocalDateTime().isAfter(getLocationDataStartTime())
          && tack.start.getLocalDateTime().isBefore(getLocationDataEndTime())
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
    dataset.removeAllSeries();
    dataset.addSeries(tackVelocity);  }

  @Override
  protected void onDataChanged()
  {
  }
}
