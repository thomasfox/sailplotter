package com.github.thomasfox.sailplotter.gui.component.table;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.text.DecimalFormat;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import com.github.thomasfox.sailplotter.gui.component.panel.ZoomChangeEvent;
import com.github.thomasfox.sailplotter.model.Tack;
import com.github.thomasfox.sailplotter.model.TackList;

public class TackTablePanel extends JScrollPane
{
  /** SerialVersionUID. */
  private static final long serialVersionUID = 1L;

  private final DefaultTableModel tackTableModel;

  private final JTable tacksTable;

  private TackList tackList;

  private boolean notifyListeners = true;

  public TackTablePanel(ListSelectionListener listener)
  {
    tackTableModel = new DefaultTableModel(
        new String[] {
            "#",
            "Point of Sail",
            "length [m]",
            "duration [s]",
            "absolute Bearing [deg]",
            "relative Bearing [deg]",
            "Speed [kts]",
            "VMG [kts]",
            "Maneuver at Start",
            "Maneuver loss at Start (s)",
            "Tacking angle at Start",
            "Maneuver at End"},
        0);
    tacksTable = new JTable(tackTableModel);
    tacksTable.setFillsViewportHeight(true);
    tacksTable.getSelectionModel().addListSelectionListener(new DelegatingListSelectionListener(listener));
    setViewportView(tacksTable);
  }

  public void updateContent(TackList tackList)
  {
    this.tackList = tackList;
    while (tackTableModel.getRowCount() > 0)
    {
      tackTableModel.removeRow(0);
    }
    Tack lastTack = null;
    int i = 0;
    for (Tack tack : tackList)
    {
      tackTableModel.addRow(new Object[] {
          i,
          tack.pointOfSail,
          new DecimalFormat("0").format(tack.getLength()),
          new DecimalFormat("0.0").format(tack.getDuration() / 1000d),
          tack.getAbsoluteBearingInDegrees() == null
          ? ""
          : new DecimalFormat("0").format(tack.getAbsoluteBearingInDegrees()),
          tack.getRelativeBearingInDegrees() == null
            ? ""
            : new DecimalFormat("0").format(tack.getRelativeBearingInDegrees()),
          new DecimalFormat("0.0").format(tack.getVelocityInKnots()),
          tack.getAverageVMGInKnots() == null
            ? ""
            : new DecimalFormat("0.0").format(tack.getAverageVMGInKnots()),
          tack.maneuverTypeAtStart == null ? "" : tack.maneuverTypeAtStart.toString(),
          tack.getIntersectionTimeDistance(lastTack) == null
            ? ""
            : new DecimalFormat("0.0").format(tack.getIntersectionTimeDistance(lastTack)),
          tack.getIntersectionAnglesInDegrees(lastTack) == null
            ? ""
            : new DecimalFormat("0").format(Math.abs(tack.getIntersectionAnglesInDegrees(lastTack))),
          tack.maneuverTypeAtEnd == null ? "" : tack.maneuverTypeAtEnd.toString()});
      lastTack = tack;
      ++i;
    }
  }

  public int[] getSelectedTackIndices()
  {
    ListSelectionModel model = tacksTable.getSelectionModel();
    int[] indices = model.getSelectedIndices();
    return indices;
  }

  public void selectInterval(int startIndex, int endIndex)
  {
    tacksTable.getSelectionModel().setSelectionInterval(startIndex, endIndex);
    Rectangle firstRectangleToSelect = tacksTable.getCellRect(startIndex, 0, true);
    Rectangle lastRectangleToSelect = tacksTable.getCellRect(endIndex, 0, true);
    tacksTable.scrollRectToVisible(lastRectangleToSelect);
    tacksTable.scrollRectToVisible(firstRectangleToSelect);
  }

  @Override
  public Dimension getPreferredSize()
  {
    return new Dimension();
  }

  public void zoomChanged(ZoomChangeEvent zoomChangeEvent)
  {
    if (tackList == null || tackList.isEmpty())
    {
      return;
    }
    Integer startIndex = tackList.getTackIndex(zoomChangeEvent.getStartIndex());
    if (startIndex == null
        && zoomChangeEvent.getStartIndex() < tackList.get(0).startOfTackDataPointIndex)
    {
      startIndex = 0;
    }
    Integer endIndex = tackList.getTackIndex(zoomChangeEvent.getEndIndex());
    if (endIndex == null
        && zoomChangeEvent.getEndIndex() > tackList.get(tackList.size() - 1).endOfTackDataPointIndex)
    {
      endIndex = tackList.size() - 1;
    }
    if (startIndex != null && endIndex != null)
    {
      try
      {
        notifyListeners = false;
        tacksTable.getSelectionModel().setSelectionInterval(startIndex, endIndex);
      }
      finally
      {
        notifyListeners = true;
      }
    }
  }

  private class DelegatingListSelectionListener implements ListSelectionListener
  {
    private final ListSelectionListener delegate;

    public DelegatingListSelectionListener(ListSelectionListener delegate)
    {
      this.delegate = delegate;
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
      if (notifyListeners)
      {
        delegate.valueChanged(e);
      }
    }
  }
}

