package bsu.rfe.group9.Haranovich.lab5.varA;

import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.util.Iterator;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import java.awt.Color;
import java.text.NumberFormat;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.awt.Font;
import java.awt.BasicStroke;
import java.util.ArrayList;
import javax.swing.JPanel;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JPanel;

public class GraphicsDisplay extends JPanel {

    private Double[][]  graphicsData;

    private boolean     showAxis = true;
    private boolean     showMarkers = true;
    private boolean     IsSelectedMarkerSp;
    private boolean     scaleMode = false;

    private int         selectedMarker = -1;

    private double      minX;
    private double      maxX;
    private double      minY;
    private double      maxY;
    private double[][]  viewport = new double[2][2];
    private double[]    originalPoint = new double[2];
    private double      scale;

    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;
    private BasicStroke selectionStroke;

    private static DecimalFormat formatter = (DecimalFormat)NumberFormat.getInstance();

    private Font axisFont;
    private Font labelFont;

    private Rectangle2D.Double selectionRect = new Rectangle2D.Double();

    public GraphicsDisplay() {
        setBackground(Color.WHITE);

        graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 10.0f, new float[] {4,1,2,1,2,1,4,1,1}, 0.0f);

        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);

        markerStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);

        selectionStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, new float[] {8,8}, 0.0f);

        axisFont = new Font("Serif", Font.BOLD, 36);
        labelFont = new Font("Serif", 0, 20);
        formatter.setMaximumFractionDigits(5);
        addMouseListener(new MouseHandler());
        addMouseMotionListener(new MouseMotionHandler());
    }
    public void showGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData;
        minX = graphicsData[0][0];
        maxX = graphicsData[graphicsData.length-1][0];
        minY = graphicsData[0][1];
        maxY = minY;

        for (int i = 1; i < graphicsData.length; i++) {
            if (graphicsData[i][1] < minY) {
                minY = graphicsData[i][1];
            }
            if (graphicsData[i][1] > maxY) {
                maxY = graphicsData[i][1];
            }
        }

        zoomToRegion(this.minX, this.maxY, this.maxX, this.minY);
    }

    public void zoomToRegion(double x1, double y1, double x2, double y2)
    {
        this.viewport[0][0] = x1;
        this.viewport[0][1] = y1;
        this.viewport[1][0] = x2;
        this.viewport[1][1] = y2;
        repaint();
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }

    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - viewport[0][0];
        double deltaY = viewport[0][1] - y;
        return new Point2D.Double(deltaX*scale, deltaY*scale);
    }

    protected double[] translatePointToXY(int x, int y) {
        return new double[] { this.viewport[0][0] + x / this.scale,
                this.viewport[0][1] - y / this.scale };
    }

    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
        Point2D.Double dest = new Point2D.Double();
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);

        return dest;
    }

    protected void paintGraphics(Graphics2D canvas) {
        canvas.setStroke(graphicsStroke);
        canvas.setColor(Color.BLACK);
        GeneralPath graphics = new GeneralPath();

        for (int i = 0; i < graphicsData.length; i++) {
            Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
            if (i > 0) {
                graphics.lineTo(point.getX(), point.getY());
            } else {
                graphics.moveTo(point.getX(), point.getY());
            }
        }

        canvas.draw(graphics);
    }

    protected void paintAxis(Graphics2D canvas) {
        canvas.setStroke(axisStroke);
        canvas.setColor(Color.BLACK);
        canvas.setPaint(Color.BLACK);
        canvas.setFont(axisFont);
        FontRenderContext context = canvas.getFontRenderContext();

        if (viewport[0][0] <= 0.0 && viewport[1][0] >= 0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(0, viewport[0][1]), xyToPoint(0,viewport[1][1])));
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(0, viewport[1][0]);

            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() + 5, arrow.getCurrentPoint().getY() + 20);
            arrow.lineTo(arrow.getCurrentPoint().getX() - 10, arrow.getCurrentPoint().getY());
            arrow.closePath();

            canvas.draw(arrow);
            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, viewport[0][1]);
            canvas.drawString("y", (float)labelPos.getX() + 10, (float)(labelPos.getY() - bounds.getY()));
        }

        if (viewport[1][1]<=0.0 && viewport[0][1]>=0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(viewport[0][0], 0), xyToPoint(viewport[1][0], 0)));
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(viewport[1][0], 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() - 20, arrow.getCurrentPoint().getY() - 5);
            arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY() + 10);
            arrow.closePath();
            canvas.draw(arrow);
            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(viewport[1][0], 0);
            canvas.drawString("x",
                    (float)(labelPos.getX()-bounds.getWidth()-10),
                    (float)(labelPos.getY() + bounds.getY()));
        }
    }

    protected boolean isFuncValueBigger(Double[] point) {
        int valueFuncInt = point[1].intValue();
        double AverageValueSum = 0;
        double AverageValue = 0;

        for (int i = 0; i < graphicsData.length; i++) {
            AverageValueSum += graphicsData[i][1];
        }

        AverageValue=AverageValueSum/graphicsData.length;
        return valueFuncInt > AverageValue * 2 ? true : false;
    }

    protected void paintMarkers(Graphics2D canvas) {
        // Шаг 1 - Установить специальное перо для черчения контуров маркеров
        canvas.setStroke(markerStroke);
        // Выбрать красный цвета для контуров маркеров
        canvas.setColor(Color.RED);
        // Выбрать красный цвет для закрашивания маркеров внутри
        canvas.setPaint(Color.RED);
        // Шаг 2 - Организовать цикл по всем точкам графика
        for (Double[] point : graphicsData) {
            if(isFuncValueBigger(point)) {
                Point2D.Double center = xyToPoint(point[0], point[1]);
                GeneralPath path = new GeneralPath();
                path.moveTo(center.x + 0, center.y + 5);
                path.lineTo(center.x + 0, center.y - 5);
                path.lineTo(center.x + 0, center.y + 0);
                path.lineTo(center.x - 5, center.y + 0);
                path.lineTo(center.x + 5, center.y + 0);
                path.lineTo(center.x - 4, center.y + 1);
                path.lineTo(center.x + 0, center.y + 0);
                path.lineTo(center.x + 5, center.y + 5);
                path.lineTo(center.x - 5, center.y - 5);
                path.lineTo(center.x + 0, center.y + 0);
                path.lineTo(center.x - 5, center.y + 5);
                path.lineTo(center.x + 5, center.y - 5);
                canvas.draw(path);
                canvas.fill(path);
            }
        }
    }

    private void paintLabels(Graphics2D canvas) {
        canvas.setFont(labelFont);

        FontRenderContext context = canvas.getFontRenderContext();

        if (selectedMarker >= 0) {
            if(!IsSelectedMarkerSp) {
                Point2D.Double point = xyToPoint(((Double[])this.graphicsData[this.selectedMarker])[0].doubleValue(),
                        ((Double[])this.graphicsData[this.selectedMarker])[1].doubleValue());

                String label = "X = " + formatter.format(((Double[])this.graphicsData[this.selectedMarker])[0])
                        + ", Y = " + formatter.format(((Double[])this.graphicsData[this.selectedMarker])[1]);

                Rectangle2D bounds = this.labelFont.getStringBounds(label, context);
                canvas.setColor(Color.RED);
                canvas.drawString(label, (float) (point.getX() + 5.0D), (float)(point.getY() - bounds.getHeight()));
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (graphicsData == null || graphicsData.length == 0) {
            return;
        }

        double scaleX = (getSize().getWidth() / (this.viewport[1][0] - this.viewport[0][0]));
        double scaleY = (getSize().getHeight() / (this.viewport[0][1] - this.viewport[1][1]));

        scale = Math.min(scaleX, scaleY);

        if (scale == scaleX) {
            double yIncrement = (getSize().getHeight()/scale-(this.viewport[0][1] - this.viewport[1][1]))/2;
            this.viewport[0][1] += yIncrement;
            this.viewport[1][1] -= yIncrement;
        }

        if (scale == scaleY) {
            double xIncrement = (getSize().getWidth()/scale-(this.viewport[1][0] - this.viewport[0][0]))/2;
            this.viewport[1][0] += xIncrement;
            this.viewport[0][0] -= xIncrement;
        }

        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();

        if (showAxis) {
            paintAxis(canvas);
        }

        paintGraphics(canvas);

        if (showMarkers) {
            paintMarkers(canvas);
        }

        paintLabels(canvas);
        paintSelection(canvas);
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
    }

    private int findSelectedPoint(int x, int y) {
        IsSelectedMarkerSp = false;
        int pos = 0;

        if (this.graphicsData == null) {
            return -1;
        }

        for (Double[] point : this.graphicsData) {

            Point2D.Double screenPoint = xyToPoint(point[0].doubleValue(), point[1].doubleValue());
            double distance = (screenPoint.getX() - x)
                    * (screenPoint.getX() - x)
                    + (screenPoint.getY() - y)
                    * (screenPoint.getY() - y);

            if ((distance < 100.0D) && (y-screenPoint.getY() < 100.0D)) {
                IsSelectedMarkerSp = false;
                return pos;
            }
            pos++;
        }

        pos = 0;

        for (Double[] point : this.graphicsData)
        {
            Point2D.Double screenPoint = xyToPoint(point[0].doubleValue(), Math.abs(point[1].doubleValue()));
            double distance = (screenPoint.getX() - x)
                    * (screenPoint.getX() - x)
                    + (screenPoint.getY() - y)
                    * (screenPoint.getY() - y);

            if (distance < 100.0D) {
                IsSelectedMarkerSp = true;
                return pos;
            }
            pos++;
        }
        return -1;
    }

    private void paintSelection(Graphics2D canvas) {
        if (!this.scaleMode) {
            return;
        }

        canvas.setStroke(this.selectionStroke);
        canvas.setColor(Color.BLACK);
        canvas.draw(this.selectionRect);
    }
    public class MouseHandler extends MouseAdapter {

        public void mouseClicked(MouseEvent ev) {
            if (ev.getButton() == 3) {
                GraphicsDisplay.this.zoomToRegion(GraphicsDisplay.this.minX,
                        GraphicsDisplay.this.maxY,
                        GraphicsDisplay.this.maxX,
                        GraphicsDisplay.this.minY);
            }
        }

        public void mousePressed(MouseEvent ev) {
            if (ev.getButton() != 1) {
                return;
            }

            GraphicsDisplay.this.originalPoint = GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY());
            GraphicsDisplay.this.scaleMode = true;
            GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(5));
            GraphicsDisplay.this.selectionRect.setFrame(ev.getX(), ev.getY(), 1.0D, 1.0D);
        }

        public void mouseReleased(MouseEvent ev) {
            if (ev.getButton() != 1) {
                return;
            }

            GraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(0));
            GraphicsDisplay.this.scaleMode = false;

            double[] finalPoint = GraphicsDisplay.this.translatePointToXY(ev.getX(), ev.getY());

            GraphicsDisplay.this.viewport = new double[2][2];
            GraphicsDisplay.this.zoomToRegion(GraphicsDisplay.this.originalPoint[0],
                    GraphicsDisplay.this.originalPoint[1],
                    finalPoint[0],
                    finalPoint[1]);
        }
    }

    public class MouseMotionHandler implements MouseMotionListener {
        public void mouseMoved(MouseEvent ev) {
            GraphicsDisplay.this.selectedMarker = GraphicsDisplay.this.findSelectedPoint(ev.getX(), ev.getY());
            GraphicsDisplay.this.repaint();
        }

        public void mouseDragged(MouseEvent ev) {
            double width = ev.getX() - GraphicsDisplay.this.selectionRect.getX();
            double height = ev.getY() - GraphicsDisplay.this.selectionRect.getY();

            if (width < 5.0D) {
                width = 5.0D;
            }

            if (height < 5.0D) {
                height = 5.0D;
            }

            GraphicsDisplay.this.selectionRect.setFrame(GraphicsDisplay.this.selectionRect.getX(),
                    GraphicsDisplay.this.selectionRect.getY(),
                    width,
                    height);

            GraphicsDisplay.this.repaint();
        }
    }
}