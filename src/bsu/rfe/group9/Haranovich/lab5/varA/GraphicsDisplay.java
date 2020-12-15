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

public class GraphicsDisplay extends JPanel
{
    private ArrayList<Double[]> graphicsData;
    private ArrayList<Double[]> originalData;
    private int selectedMarker;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double[][] viewport;
    private ArrayList<double[][]> undoHistory;
    private double scaleX;
    private double scaleY;
    private BasicStroke axisStroke;
    private BasicStroke gridStroke;
    private BasicStroke markerStroke;
    private BasicStroke selectionStroke;
    private Font axisFont;
    private Font labelsFont;
    private static DecimalFormat formatter;
    private boolean scaleMode;
    private boolean changeMode;
    private double[] originalPoint;
    private Rectangle2D.Double selectionRect;
    
    static {
        GraphicsDisplay.formatter = (DecimalFormat)NumberFormat.getInstance();
    }
    
    public GraphicsDisplay() {
        this.selectedMarker = -1;
        this.viewport = new double[2][2];
        this.undoHistory = new ArrayList<double[][]>(5);
        this.scaleMode = false;
        this.changeMode = false;
        this.originalPoint = new double[2];
        this.selectionRect = new Rectangle2D.Double();
        this.setBackground(Color.WHITE);
        this.axisStroke = new BasicStroke(2.0f, 0, 0, 10.0f, null, 0.0f);
        this.gridStroke = new BasicStroke(1.0f, 0, 0, 10.0f, new float[] { 4.0f, 4.0f }, 0.0f);
        this.markerStroke = new BasicStroke(1.0f, 0, 0, 10.0f, null, 0.0f);
        this.selectionStroke = new BasicStroke(1.0f, 0, 0, 10.0f, new float[] { 10.0f, 10.0f }, 0.0f);
        this.axisFont = new Font("Serif", 1, 36);
        this.labelsFont = new Font("Serif", 0, 10);
        GraphicsDisplay.formatter.setMaximumFractionDigits(5);
        this.addMouseListener((MouseListener)new GraphicsDisplay.MouseHandler(this));
        this.addMouseMotionListener((MouseMotionListener)new GraphicsDisplay.MouseMotionHandler(this));
    }
    
    public void displayGraphics(final ArrayList<Double[]> graphicsData) {
        this.graphicsData = graphicsData;
        this.originalData = new ArrayList<Double[]>(graphicsData.size());
        for (final Double[] point : graphicsData) {
            final Double[] newPoint = { new Double(point[0]), new Double(point[1]) };
            this.originalData.add(newPoint);
        }
        this.minX = graphicsData.get(0)[0];
        this.maxX = graphicsData.get(graphicsData.size() - 1)[0];
        this.minY = graphicsData.get(0)[1];
        this.maxY = this.minY;
        for (int i = 1; i < graphicsData.size(); ++i) {
            if (graphicsData.get(i)[1] < this.minY) {
                this.minY = graphicsData.get(i)[1];
            }
            if (graphicsData.get(i)[1] > this.maxY) {
                this.maxY = graphicsData.get(i)[1];
            }
        }
        this.zoomToRegion(this.minX, this.maxY, this.maxX, this.minY);
    }
    
    public void zoomToRegion(final double x1, final double y1, final double x2, final double y2) {
        this.viewport[0][0] = x1;
        this.viewport[0][1] = y1;
        this.viewport[1][0] = x2;
        this.viewport[1][1] = y2;
        this.repaint();
    }
    
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        this.scaleX = this.getSize().getWidth() / (this.viewport[1][0] - this.viewport[0][0]);
        this.scaleY = this.getSize().getHeight() / (this.viewport[0][1] - this.viewport[1][1]);
        if (this.graphicsData == null || this.graphicsData.size() == 0) {
            return;
        }
        final Graphics2D canvas = (Graphics2D)g;
        this.paintGrid(canvas);
        this.paintAxis(canvas);
        this.paintGraphics(canvas);
        this.paintMarkers(canvas);
        this.paintLabels(canvas);
        this.paintSelection(canvas);
    }
    
    private void paintSelection(final Graphics2D canvas) {
        if (!this.scaleMode) {
            return;
        }
        canvas.setStroke(this.selectionStroke);
        canvas.setColor(Color.BLACK);
        canvas.draw(this.selectionRect);
    }
    
    private void paintGraphics(final Graphics2D canvas) {
        canvas.setStroke(this.markerStroke);
        canvas.setColor(Color.RED);
        Double currentX = null;
        Double currentY = null;
        for (final Double[] point : this.graphicsData) {
            if (point[0] >= this.viewport[0][0] && point[1] <= this.viewport[0][1] && point[0] <= this.viewport[1][0]) {
                if (point[1] < this.viewport[1][1]) {
                    continue;
                }
                if (currentX != null && currentY != null) {
                    canvas.draw(new Line2D.Double(this.translateXYtoPoint(currentX, currentY), this.translateXYtoPoint(point[0], point[1])));
                }
                currentX = point[0];
                currentY = point[1];
            }
        }
    }
    
    private void paintMarkers(final Graphics2D canvas) {
        canvas.setStroke(this.markerStroke);
        canvas.setColor(Color.RED);
        canvas.setPaint(Color.RED);
        Ellipse2D.Double lastMarker = null;
        int i = -1;
        for (final Double[] point : this.graphicsData) {
            ++i;
            if (point[0] >= this.viewport[0][0] && point[1] <= this.viewport[0][1] && point[0] <= this.viewport[1][0]) {
                if (point[1] < this.viewport[1][1]) {
                    continue;
                }
                int radius;
                if (i == this.selectedMarker) {
                    radius = 6;
                }
                else {
                    radius = 3;
                }
                final Ellipse2D.Double marker = new Ellipse2D.Double();
                final Point2D center = this.translateXYtoPoint(point[0], point[1]);
                final Point2D corner = new Point2D.Double(center.getX() + radius, center.getY() + radius);
                marker.setFrameFromCenter(center, corner);
                if (i == this.selectedMarker) {
                    lastMarker = marker;
                }
                else {
                    canvas.draw(marker);
                    canvas.fill(marker);
                }
            }
        }
        if (lastMarker != null) {
            canvas.setColor(Color.BLUE);
            canvas.setPaint(Color.BLUE);
            canvas.draw(lastMarker);
            canvas.fill(lastMarker);
        }
    }
    
    private void paintLabels(final Graphics2D canvas) {
        canvas.setColor(Color.BLACK);
        canvas.setFont(this.labelsFont);
        final FontRenderContext context = canvas.getFontRenderContext();
        double labelYPos;
        if (this.viewport[1][1] < 0.0 && this.viewport[0][1] > 0.0) {
            labelYPos = 0.0;
        }
        else {
            labelYPos = this.viewport[1][1];
        }
        double labelXPos;
        if (this.viewport[0][0] < 0.0 && this.viewport[1][0] > 0.0) {
            labelXPos = 0.0;
        }
        else {
            labelXPos = this.viewport[0][0];
        }
        for (double pos = this.viewport[0][0], step = (this.viewport[1][0] - this.viewport[0][0]) / 10.0; pos < this.viewport[1][0]; pos += step) {
            final Point2D.Double point = this.translateXYtoPoint(pos, labelYPos);
            final String label = GraphicsDisplay.formatter.format(pos);
            final Rectangle2D bounds = this.labelsFont.getStringBounds(label, context);
            canvas.drawString(label, (float)(point.getX() + 5.0), (float)(point.getY() - bounds.getHeight()));
        }
        for (double pos = this.viewport[1][1], step = (this.viewport[0][1] - this.viewport[1][1]) / 10.0; pos < this.viewport[0][1]; pos += step) {
            final Point2D.Double point = this.translateXYtoPoint(labelXPos, pos);
            final String label = GraphicsDisplay.formatter.format(pos);
            final Rectangle2D bounds = this.labelsFont.getStringBounds(label, context);
            canvas.drawString(label, (float)(point.getX() + 5.0), (float)(point.getY() - bounds.getHeight()));
        }
        if (this.selectedMarker >= 0) {
            final Point2D.Double point = this.translateXYtoPoint(this.graphicsData.get(this.selectedMarker)[0], this.graphicsData.get(this.selectedMarker)[1]);
            final String label = "X=" + GraphicsDisplay.formatter.format(this.graphicsData.get(this.selectedMarker)[0]) + ", Y=" + GraphicsDisplay.formatter.format(this.graphicsData.get(this.selectedMarker)[1]);
            final Rectangle2D bounds = this.labelsFont.getStringBounds(label, context);
            canvas.setColor(Color.BLUE);
            canvas.drawString(label, (float)(point.getX() + 5.0), (float)(point.getY() - bounds.getHeight()));
        }
    }
    
    private void paintGrid(final Graphics2D canvas) {
        canvas.setStroke(this.gridStroke);
        canvas.setColor(Color.GRAY);
        for (double pos = this.viewport[0][0], step = (this.viewport[1][0] - this.viewport[0][0]) / 10.0; pos < this.viewport[1][0]; pos += step) {
            canvas.draw(new Line2D.Double(this.translateXYtoPoint(pos, this.viewport[0][1]), this.translateXYtoPoint(pos, this.viewport[1][1])));
        }
        canvas.draw(new Line2D.Double(this.translateXYtoPoint(this.viewport[1][0], this.viewport[0][1]), this.translateXYtoPoint(this.viewport[1][0], this.viewport[1][1])));
        for (double pos = this.viewport[1][1], step = (this.viewport[0][1] - this.viewport[1][1]) / 10.0; pos < this.viewport[0][1]; pos += step) {
            canvas.draw(new Line2D.Double(this.translateXYtoPoint(this.viewport[0][0], pos), this.translateXYtoPoint(this.viewport[1][0], pos)));
        }
        canvas.draw(new Line2D.Double(this.translateXYtoPoint(this.viewport[0][0], this.viewport[0][1]), this.translateXYtoPoint(this.viewport[1][0], this.viewport[0][1])));
    }
    
    private void paintAxis(final Graphics2D canvas) {
        canvas.setStroke(this.axisStroke);
        canvas.setColor(Color.BLACK);
        canvas.setFont(this.axisFont);
        final FontRenderContext context = canvas.getFontRenderContext();
        if (this.viewport[0][0] <= 0.0 && this.viewport[1][0] >= 0.0) {
            canvas.draw(new Line2D.Double(this.translateXYtoPoint(0.0, this.viewport[0][1]), this.translateXYtoPoint(0.0, this.viewport[1][1])));
            canvas.draw(new Line2D.Double(this.translateXYtoPoint(-(this.viewport[1][0] - this.viewport[0][0]) * 0.0025, this.viewport[0][1] - (this.viewport[0][1] - this.viewport[1][1]) * 0.015), this.translateXYtoPoint(0.0, this.viewport[0][1])));
            canvas.draw(new Line2D.Double(this.translateXYtoPoint((this.viewport[1][0] - this.viewport[0][0]) * 0.0025, this.viewport[0][1] - (this.viewport[0][1] - this.viewport[1][1]) * 0.015), this.translateXYtoPoint(0.0, this.viewport[0][1])));
            final Rectangle2D bounds = this.axisFont.getStringBounds("y", context);
            final Point2D.Double labelPos = this.translateXYtoPoint(0.0, this.viewport[0][1]);
            canvas.drawString("y", (float)labelPos.x + 10.0f, (float)(labelPos.y + bounds.getHeight() / 2.0));
        }
        if (this.viewport[1][1] <= 0.0 && this.viewport[0][1] >= 0.0) {
            canvas.draw(new Line2D.Double(this.translateXYtoPoint(this.viewport[0][0], 0.0), this.translateXYtoPoint(this.viewport[1][0], 0.0)));
            canvas.draw(new Line2D.Double(this.translateXYtoPoint(this.viewport[1][0] - (this.viewport[1][0] - this.viewport[0][0]) * 0.01, (this.viewport[0][1] - this.viewport[1][1]) * 0.005), this.translateXYtoPoint(this.viewport[1][0], 0.0)));
            canvas.draw(new Line2D.Double(this.translateXYtoPoint(this.viewport[1][0] - (this.viewport[1][0] - this.viewport[0][0]) * 0.01, -(this.viewport[0][1] - this.viewport[1][1]) * 0.005), this.translateXYtoPoint(this.viewport[1][0], 0.0)));
            final Rectangle2D bounds = this.axisFont.getStringBounds("x", context);
            final Point2D.Double labelPos = this.translateXYtoPoint(this.viewport[1][0], 0.0);
            canvas.drawString("x", (float)(labelPos.x - bounds.getWidth() - 10.0), (float)(labelPos.y - bounds.getHeight() / 2.0));
        }
    }
    
    protected Point2D.Double translateXYtoPoint(final double x, final double y) {
        final double deltaX = x - this.viewport[0][0];
        final double deltaY = this.viewport[0][1] - y;
        return new Point2D.Double(deltaX * this.scaleX, deltaY * this.scaleY);
    }
    
    protected double[] translatePointToXY(final int x, final int y) {
        return new double[] { this.viewport[0][0] + x / this.scaleX, this.viewport[0][1] - y / this.scaleY };
    }
    
    protected int findSelectedPoint(final int x, final int y) {
        if (this.graphicsData == null) {
            return -1;
        }
        int pos = 0;
        for (final Double[] point : this.graphicsData) {
            final Point2D.Double screenPoint = this.translateXYtoPoint(point[0], point[1]);
            final double distance = (screenPoint.getX() - x) * (screenPoint.getX() - x) + (screenPoint.getY() - y) * (screenPoint.getY() - y);
            if (distance < 100.0) {
                return pos;
            }
            ++pos;
        }
        return -1;
    }
    
    public void reset() {
        this.displayGraphics(this.originalData);
    }
}