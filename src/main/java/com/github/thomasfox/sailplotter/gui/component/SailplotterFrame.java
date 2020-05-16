package com.github.thomasfox.sailplotter.gui.component;

import java.awt.BorderLayout;
import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.github.thomasfox.sailplotter.model.Data;

public class SailplotterFrame extends JFrame
{
  private static final long serialVersionUID = 1L;

  private static final String FRAME_NAME = "SailPlotter";

  public SailplotterFrame()
  {
    super(FRAME_NAME);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setIconImages(createIconImageLsit());
  }

  public void setViews(JPanel views)
  {
    getContentPane().add(views, BorderLayout.CENTER);
  }

  public void setTitleFromData(Data data)
  {
    String framePortion = "";
    if (data != null && data.getFile() != null)
    {
      framePortion = data.getFile().getName() + " - ";
    }
    setTitle(framePortion + FRAME_NAME);
  }



  private List<Image> createIconImageLsit()
  {
    List<Image> iconImages = new ArrayList<>();
    URL iconURL = getClass().getResource("/icon/ic_sailplotter_128.png");
    iconImages.add(new ImageIcon(iconURL).getImage());
    iconURL = getClass().getResource("/icon/ic_sailplotter_64.png");
    iconImages.add(new ImageIcon(iconURL).getImage());
    iconURL = getClass().getResource("/icon/ic_sailplotter_32.png");
    iconImages.add(new ImageIcon(iconURL).getImage());
    return iconImages;
  }
}
