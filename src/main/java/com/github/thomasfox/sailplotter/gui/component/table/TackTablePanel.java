package com.github.thomasfox.sailplotter.gui.component.table;

import java.awt.Rectangle;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import com.github.thomasfox.sailplotter.model.Tack;

public class TackTablePanel extends JScrollPane
{
  /** SerialVersionUID. */
  private static final long serialVersionUID = 1L;

  private final DefaultTableModel tackTableModel;

  private final JTable tacksTable;

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
    tacksTable.getSelectionModel().addListSelectionListener(listener);
    setViewportView(tacksTable);
  }

  public void updateContent(List<Tack> tackList)
  {
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

  public int getSelectedTackIndex()
  {
    ListSelectionModel model = tacksTable.getSelectionModel();
    int index = model.getAnchorSelectionIndex();
    return index;
  }

  public void selectInterval(int startIndex, int endIndex)
  {
    tacksTable.getSelectionModel().setSelectionInterval(startIndex, endIndex);
    Rectangle firstRectangleToSelect = tacksTable.getCellRect(startIndex, 0, true);
    Rectangle lastRectangleToSelect = tacksTable.getCellRect(endIndex, 0, true);
    tacksTable.scrollRectToVisible(lastRectangleToSelect);
    tacksTable.scrollRectToVisible(firstRectangleToSelect);
  }
}
