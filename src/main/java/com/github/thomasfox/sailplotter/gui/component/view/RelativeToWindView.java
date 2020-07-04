package com.github.thomasfox.sailplotter.gui.component.view;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.gui.SwingGui;
import com.github.thomasfox.sailplotter.gui.component.panel.ZoomPanel;
import com.github.thomasfox.sailplotter.gui.component.panel.ZoomPanelChangeEvent;
import com.github.thomasfox.sailplotter.gui.component.plot.AbstractPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.FullMapPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.VelocityBearingPolarPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.ZoomedVelocityMadeGoodPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.ZoomedVelocityPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.ZoomedWindwardMapPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.table.TackTablePanel;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.Tack;

public class RelativeToWindView extends AbstractView
{
  private static final long serialVersionUID = 1L;

  private final SwingGui gui;

  private final ZoomedVelocityMadeGoodPlotPanel zoomedVelocityMadeGoodPlotPanel;

  private final ZoomedVelocityPlotPanel zoomedVelocityPlotPanel;

  private final ZoomPanel zoomPanel;

  private final AbstractPlotPanel fullMapPlotPanel;

  private final AbstractPlotPanel zoomedWindwardMapPlotPanel;

  private final AbstractPlotPanel velocityBearingPolarPlotPanel;

  private final JTextField windDirectionTextField;

  TackTablePanel tackTablePanel;

  private Data data;

  public RelativeToWindView(SwingGui gui)
  {
    this.gui = gui;

    zoomPanel = new ZoomPanel();
    zoomPanel.addListener(gui::zoomPanelStateChanged);

    JPanel topRightPanel = new JPanel();
    topRightPanel.add(zoomPanel);

    JPanel windDirectionPanel = new JPanel();
    JLabel windDirectionLabel = new JLabel("Wind direction");
    windDirectionPanel.add(windDirectionLabel);
    windDirectionTextField = new JTextField();
    Dimension windDirectionTextFieldSize = windDirectionTextField.getPreferredSize();
    windDirectionTextFieldSize.width=30;
    windDirectionTextField.setPreferredSize(windDirectionTextFieldSize);
    if (data == null)
    {
      windDirectionTextField.setText("0");
    }
    else
    {
      windDirectionTextField.setText(Integer.toString(data.getAverageWindDirectionInDegrees()));
    }
    windDirectionTextField.addActionListener(gui::windDirectionChanged);
    windDirectionPanel.add(windDirectionTextField);
    topRightPanel.add(windDirectionPanel);

    topRightPanel.setLayout(new BoxLayout(topRightPanel, BoxLayout.PAGE_AXIS));
    createLayout()
        .withGridxy(2, 0)
        .withWeightx(0.25).withWeighty(0.25)
        .add(topRightPanel);

    zoomedVelocityMadeGoodPlotPanel = new ZoomedVelocityMadeGoodPlotPanel(
        zoomPanel.getStartIndex(),
        zoomPanel.getZoomIndex());
    createLayout()
        .withGridxy(0, 0)
        .withWeightx(0.375).withWeighty(0.25)
        .add(zoomedVelocityMadeGoodPlotPanel);

    zoomedVelocityPlotPanel = new ZoomedVelocityPlotPanel(
        zoomPanel.getStartIndex(),
        zoomPanel.getZoomIndex());

    createLayout()
        .withGridxy(1, 0)
        .withWeightx(0.375).withWeighty(0.25)
        .add(zoomedVelocityPlotPanel);

    fullMapPlotPanel = new FullMapPlotPanel(zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    createLayout()
        .withGridxy(0, 1)
        .withWeightx(0.375).withWeighty(0.5)
        .add(fullMapPlotPanel);

    zoomedWindwardMapPlotPanel = new ZoomedWindwardMapPlotPanel(zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    createLayout()
        .withGridxy(1, 1)
        .withWeightx(0.375).withWeighty(0.5)
        .add(zoomedWindwardMapPlotPanel);

    velocityBearingPolarPlotPanel = new VelocityBearingPolarPlotPanel(zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    createLayout()
        .withGridxy(2, 1)
        .withWeightx(0.25).withWeighty(0.5)
        .add(velocityBearingPolarPlotPanel);

    tackTablePanel = new TackTablePanel(this::tackSelected);
    createLayout()
        .withGridxy(0, 2)
        .withWeightx(1).withWeighty(0.25)
        .withColumnSpan(3)
        .add(tackTablePanel);
  }

  public void redisplay(boolean updateTableContent)
  {
    int zoomWindowStartIndex = zoomPanel.getStartIndex();
    int zoomWindowZoomIndex = zoomPanel.getZoomIndex();
    zoomedVelocityMadeGoodPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    zoomedVelocityPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    fullMapPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    zoomedWindwardMapPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    velocityBearingPolarPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    if (data != null)
    {
      windDirectionTextField.setText(Integer.toString(data.getAverageWindDirectionInDegrees()));
      if (updateTableContent)
      {
        tackTablePanel.updateContent(data.getTackList());
      }
    }
  }

  public void tackSelected(ListSelectionEvent e)
  {
    if (e.getValueIsAdjusting() || gui.inUpdate)
    {
      return;
    }
    int index = tackTablePanel.getSelectedTackIndex();
    Tack tack = data.getTackList().get(index);
    zoomPanel.setStartIndex(Math.max(tack.startOfTackDataPointIndex - Constants.NUM_DATAPOINTS_TACK_EXTENSION, 0), true);
    zoomPanel.setZoomIndex(Math.min(
        Math.max(
            Constants.NUMER_OF_ZOOM_TICKS * (tack.endOfTackDataPointIndex - tack.startOfTackDataPointIndex + 2 * Constants.NUM_DATAPOINTS_TACK_EXTENSION) / (data.getPointsWithLocation().size()),
            3),
        Constants.NUMER_OF_ZOOM_TICKS),
        true);
  }

  @Override
  public void dataChanged(Data data)
  {
    this.data = data;
    zoomPanel.setDataSize(data.getPointsWithLocation().size());
    zoomedVelocityMadeGoodPlotPanel.dataChanged(data);
    zoomedVelocityPlotPanel.dataChanged(data);
    fullMapPlotPanel.dataChanged(data);
    zoomedWindwardMapPlotPanel.dataChanged(data);
    velocityBearingPolarPlotPanel.dataChanged(data);
  }

  @Override
  public void alignZoomPanelToChangeEvent(ZoomPanelChangeEvent e)
  {
    if (!e.isSource(zoomPanel))
    {
      zoomPanel.setStartIndex(e.getStartIndex(), false);
      zoomPanel.setZoomIndex(e.getZoomPosition(), false);
    }
  }
}
