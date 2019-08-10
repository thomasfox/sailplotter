package com.github.thomasfox.sailplotter.gui.component.view;

import com.github.thomasfox.sailplotter.gui.CommentPanel;
import com.github.thomasfox.sailplotter.gui.SwingGui;
import com.github.thomasfox.sailplotter.gui.ZoomPanelChangeEvent;
import com.github.thomasfox.sailplotter.model.Data;

public class CommentsView extends AbstractView
{
  private static final long serialVersionUID = 1L;

  private final CommentPanel commentPanel;

  private Data data;

  public CommentsView(SwingGui gui)
  {
    commentPanel = new CommentPanel();
    createLayout()
        .withGridx(0).withGridy(0)
        .withWeightx(1).withWeighty(0.9)
        .add(commentPanel);
  }

  public void redisplay()
  {
    if (data == null)
    {
      commentPanel.setText(null);
    }
    else
    {
      commentPanel.setText(data.comment);
    }
  }

  public void dataChanged(Data data)
  {
    this.data = data;
    commentPanel.setTextConsumer(data::setComment);
  }

  @Override
  public void alignZoomPanelToChangeEvent(ZoomPanelChangeEvent e)
  {
    // no action needed as we have no zoom panel
  }

}
