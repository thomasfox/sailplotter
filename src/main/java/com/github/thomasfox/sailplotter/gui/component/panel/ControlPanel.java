package com.github.thomasfox.sailplotter.gui.component.panel;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.github.thomasfox.sailplotter.gui.SwingGui;
import com.github.thomasfox.sailplotter.listener.DataChangeListener;
import com.github.thomasfox.sailplotter.model.Data;

public class ControlPanel extends JPanel implements DataChangeListener
{
  /** SerialVersionUID. */
  private static final long serialVersionUID = 1L;

  private final ZoomPanel zoomPanel;

  private final JTextField windDirectionTextField;

  public ControlPanel(SwingGui gui)
  {
    zoomPanel = new ZoomPanel();
    zoomPanel.addListener(gui::zoomPanelStateChanged);
    add(zoomPanel);

    JPanel windDirectionPanel = new JPanel();
    JLabel windDirectionLabel = new JLabel("Wind direction");
    windDirectionPanel.add(windDirectionLabel);
    windDirectionTextField = new JTextField();
    Dimension windDirectionTextFieldSize = windDirectionTextField.getPreferredSize();
    windDirectionTextFieldSize.width=30;
    windDirectionTextField.setPreferredSize(windDirectionTextFieldSize);
    windDirectionTextField.setText("0");
    windDirectionTextField.addActionListener(gui::windDirectionChanged);
    windDirectionPanel.add(windDirectionTextField);
    add(windDirectionPanel);
    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
  }

  public int getZoomStartIndex()
  {
    return zoomPanel.getStartIndex();
  }

  public int getZoomIndex()
  {
    return zoomPanel.getZoomIndex();
  }

  public void setZoomStartIndex(int startIndex)
  {
    zoomPanel.setStartIndex(startIndex, true);
  }

  public void setZoomEndIndex(int endIndex)
  {
    zoomPanel.setEndIndex(endIndex, true);
  }

  @Override
  public void dataChanged(Data data)
  {
    zoomPanel.setDataSize(data.getPointsWithLocation().size());
    if (data != null)
    {
      windDirectionTextField.setText(Integer.toString(data.getAverageWindDirectionInDegrees()));
    }
  }

  public void processZoomPanelChangeEvent(ZoomPanelChangeEvent e)
  {
    zoomPanel.processZoomPanelChangeEvent(e);
  }
}
