package com.github.thomasfox.sailplotter.gui;

import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import com.github.thomasfox.sailplotter.model.TackSeries;

public class TackSeriesTablePanel extends JScrollPane
{
  /** SerialVersionUID. */
  private static final long serialVersionUID = 1L;

  private final DefaultTableModel tackSeriesTableModel;

  private final JTable tackSeriesTable;

  public TackSeriesTablePanel(List<TackSeries> tackSeriesList, ListSelectionListener listener)
  {
    tackSeriesTableModel = new DefaultTableModel(
        new String[] {
            "Tacks",
            "Type",
            "Wind direction [°]",
            "Angle to Wind [°]",
            "Velocity Main Parts Starboard [knots]",
            "Velocity Main Parts Port [knots]"},
        0);

    updateContent(tackSeriesList);
    tackSeriesTable = new JTable(tackSeriesTableModel);
    tackSeriesTable.setFillsViewportHeight(true);
    tackSeriesTable.getSelectionModel().addListSelectionListener(listener);
    setViewportView(tackSeriesTable);
  }

  public void updateContent(List<TackSeries> tackSeriesList)
  {
    while (tackSeriesTableModel.getRowCount() > 0)
    {
      tackSeriesTableModel.removeRow(0);
    }
    for (TackSeries tackSeries : tackSeriesList)
    {
      tackSeriesTableModel.addRow(new Object[] {
          tackSeries.startTackIndex + " - " + tackSeries.endTackIndex,
          tackSeries.type,
          tackSeries.getAverageWindDirectionInDegrees(),
          tackSeries.getAverageAngleToWindInDegrees(),
          new DecimalFormat("0.0").format(tackSeries.getAverageMainPartVelocityStarboard()),
          new DecimalFormat("0.0").format(tackSeries.getAverageMainPartVelocityPort())});
    }
  }

  public int getSelectedTackSeriesIndex()
  {
    ListSelectionModel model = tackSeriesTable.getSelectionModel();
    int index = model.getAnchorSelectionIndex();
    return index;
  }
}
