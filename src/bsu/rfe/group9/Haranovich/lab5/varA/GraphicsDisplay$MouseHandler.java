package bsu.rfe.group9.Haranovich.lab5.varA;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

public class MouseHandler extends MouseAdapter
{
    @Override
    public void mouseClicked(final MouseEvent ev) {
        if (ev.getButton() == 3) {
            if (GraphicsDisplay.access$0(GraphicsDisplay.this).size() > 0) {
                GraphicsDisplay.access$1(GraphicsDisplay.this, GraphicsDisplay.access$0(GraphicsDisplay.this).get(GraphicsDisplay.access$0(GraphicsDisplay.this).size() - 1));
                GraphicsDisplay.access$0(GraphicsDisplay.this).remove(GraphicsDisplay.access$0(GraphicsDisplay.this).size() - 1);
            }
            else {
                GraphicsDisplay.this.zoomToRegion(GraphicsDisplay.access$2(GraphicsDisplay.this), GraphicsDisplay.access$3(GraphicsDisplay.this), GraphicsDisplay.access$4(GraphicsDisplay.this), GraphicsDisplay.access$5(GraphicsDisplay.this));
            }
            GraphicsDisplay.this.repaint();
        }
    }
    
    @Override
    public void mousePressed(final MouseEvent ev) {
        if (ev.getButton() != 1) {
            return;
        }
        GraphicsDisplay.access$6(GraphicsDisplay.this, GraphicsDisplay.this.findSelectedPoint(ev.getX(), ev.getY()));
        GraphicsDisplay.access$7(GraphicsDisplay.this, GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY()));
        if (GraphicsDisplay.access$8(GraphicsDisplay.this) >= 0) {
            GraphicsDisplay.access$9(GraphicsDisplay.this, true);
            GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(8));
        }
        else {
            GraphicsDisplay.access$10(GraphicsDisplay.this, true);
            GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(5));
            GraphicsDisplay.access$11(GraphicsDisplay.this).setFrame(ev.getX(), ev.getY(), 1.0, 1.0);
        }
    }
    
    @Override
    public void mouseReleased(final MouseEvent ev) {
        if (ev.getButton() != 1) {
            return;
        }
        GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(0));
        if (GraphicsDisplay.access$12(GraphicsDisplay.this)) {
            GraphicsDisplay.access$9(GraphicsDisplay.this, false);
        }
        else {
            GraphicsDisplay.access$10(GraphicsDisplay.this, false);
            final double[] finalPoint = GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY());
            GraphicsDisplay.access$0(GraphicsDisplay.this).add(GraphicsDisplay.access$13(GraphicsDisplay.this));
            GraphicsDisplay.access$1(GraphicsDisplay.this, new double[2][2]);
            GraphicsDisplay.this.zoomToRegion(GraphicsDisplay.access$14(GraphicsDisplay.this)[0], GraphicsDisplay.access$14(GraphicsDisplay.this)[1], finalPoint[0], finalPoint[1]);
            GraphicsDisplay.this.repaint();
        }
    }
}