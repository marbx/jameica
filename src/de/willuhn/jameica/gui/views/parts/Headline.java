/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/views/parts/Attic/Headline.java,v $
 * $Revision: 1.1 $
 * $Date: 2003/11/21 02:10:21 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.views.parts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.willuhn.jameica.GUI;
import de.willuhn.jameica.util.Style;

/**
 * Malt eine Standard-Ueberschrift in den Dialog.
 * @author willuhn
 */
public class Headline
{

  private Label dotLine;

  /**
   * Erzeugt eine neue Standardueberschrift im angegebenen Composite mit dem uebergebenen Namen.
   * @param parent das Composite in dem die Ueberschrift gemalt werden soll.
   * @param headline Name der Ueberschrift.
   */
  public Headline(Composite parent, String headline)
  {
    Composite comp = new Composite(parent, SWT.NONE);
    comp.setLayout(new GridLayout());
    comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Label title = new Label(comp, SWT.NONE);
    title.setText(headline);
    title.setLayoutData(new GridData());

    title.setFont(Style.getFont("Verdana", 8, SWT.BOLD));
    title.setForeground(new Color(GUI.display,0,0,0));
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    data.heightHint = 3;
    dotLine = new Label(comp, SWT.NONE);
    dotLine.setText("");
    dotLine.setLayoutData(data);

    dotLine.addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent e) {
        drawDottedLine(e);
      }
    });
  }
  
  /**
   * Zeichnet eine gepunktete Linie unter die Ueberschrift.
   * @param e PaintEvent
   */
  private void drawDottedLine(PaintEvent e)
  {
    Point p = dotLine.getSize();
    e.gc.setLineWidth(1); 
    e.gc.setLineStyle(SWT.LINE_SOLID); 
    e.gc.setForeground(new Color(GUI.display, 125, 125, 125)); 
    for (int i=0; i<p.x;) {
      e.gc.drawLine(i,0,i,0);
      i=i+3;
    }
  }
}

/*********************************************************************
 * $Log: Headline.java,v $
 * Revision 1.1  2003/11/21 02:10:21  willuhn
 * @N prepared Statements in AbstractDBObject
 * @N a lot of new SWT parts
 *
 **********************************************************************/