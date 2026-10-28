// MEMBER 3 - REUSABLE UI CONTROLS (2 of 3)
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;

/* ========= UiKit.java ========= */
/** Small custom Swing components that give the app its modern look. */
final class UiKit {
    private UiKit() {}

    // ------------------------------------------------------------------ helpers
    public static void quality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    public static JLabel label(String text, float size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.font(style, size));
        l.setForeground(color);
        return l;
    }

    public static void drawCheck(Graphics2D g, double cx, double cy, double s, Color color, float stroke) {
        g.setColor(color);
        g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine((int) Math.round(cx - 0.28 * s), (int) Math.round(cy + 0.02 * s),
                   (int) Math.round(cx - 0.06 * s), (int) Math.round(cy + 0.24 * s));
        g.drawLine((int) Math.round(cx - 0.06 * s), (int) Math.round(cy + 0.24 * s),
                   (int) Math.round(cx + 0.30 * s), (int) Math.round(cy - 0.20 * s));
    }

    public static void drawCentered(Graphics2D g, String text, Font font, Color color, double cx, double baseline) {
        g.setFont(font);
        g.setColor(color);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (float) (cx - fm.stringWidth(text) / 2.0), (float) baseline);
    }

    public static void styleScroll(JScrollPane sp) {
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUI(new UiWidgets.ModernScrollBarUI());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        sp.getVerticalScrollBar().setUnitIncrement(16);
    }

    // ------------------------------------------------------------------ panels
    /** Soft diagonal gradient used as the window background. */
    public static class GradientPanel extends JPanel {
        public GradientPanel(LayoutManager lm) {
            super(lm);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setPaint(new GradientPaint(0, 0, Theme.BG_TOP, getWidth() * 0.7f, getHeight(), Theme.BG_BOTTOM));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose();
        }
    }

    /** Rounded panel with optional soft drop shadow. */
    public static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color fill;
        private final int shadow;
        private Color border;

        public RoundedPanel(int radius, Color fill, int shadow) {
            this.radius = radius;
            this.fill = fill;
            this.shadow = shadow;
            setOpaque(false);
        }

        public RoundedPanel withBorder(Color c) {
            this.border = c;
            return this;
        }

        public RoundedPanel padding(int top, int left, int bottom, int right) {
            setBorder(new EmptyBorder(top + shadow, left + shadow, bottom + shadow, right + shadow));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            quality(g);
            int w = getWidth(), h = getHeight();
            for (int i = shadow; i > 0; i--) {
                g.setColor(new Color(40, 55, 120, 5));
                g.fill(new RoundRectangle2D.Double(shadow - i, shadow - i + 4, w - 2.0 * (shadow - i), h - 2.0 * (shadow - i) - 4, radius + i, radius + i));
            }
            RoundRectangle2D card = new RoundRectangle2D.Double(shadow, shadow, w - 2.0 * shadow - 1, h - 2.0 * shadow - 1, radius, radius);
            g.setColor(fill);
            g.fill(card);
            if (border != null) {
                g.setColor(border);
                g.setStroke(new BasicStroke(1.2f));
                g.draw(card);
            }
            g.dispose();
        }
    }

    // ------------------------------------------------------------------ button
    public enum ButtonKind { PRIMARY, SECONDARY }

    public static class RoundedButton extends JButton {
        private final ButtonKind kind;

        public RoundedButton(String text, ButtonKind kind) {
            super(text);
            this.kind = kind;
            setFont(Theme.font(Font.BOLD, 14f));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(11, 28, 11, 28));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            quality(g);
            Color bg;
            if (kind == ButtonKind.PRIMARY) {
                if (!isEnabled()) bg = new Color(0xB9CBF5);
                else if (getModel().isPressed()) bg = Theme.PRIMARY_DARK.darker();
                else if (getModel().isRollover()) bg = Theme.PRIMARY_DARK;
                else bg = Theme.PRIMARY;
                setForeground(Color.WHITE);
            } else {
                if (!isEnabled()) bg = new Color(0xF3F4F6);
                else if (getModel().isPressed()) bg = new Color(0xE5E7EB);
                else if (getModel().isRollover()) bg = new Color(0xF3F4F6);
                else bg = Color.WHITE;
                setForeground(isEnabled() ? Theme.TEXT : Theme.MUTED);
            }
            RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            g.setColor(bg);
            g.fill(shape);
            if (kind == ButtonKind.SECONDARY) {
                g.setColor(Theme.LINE);
                g.setStroke(new BasicStroke(1.2f));
                g.draw(shape);
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }

    // ------------------------------------------------------------------ icons
    public enum IconType { UPLOAD, FOLDER, COLUMNS, RUN, CHART }

    /** Vector icons drawn with Java2D (no image files needed). */
    public static class IconComponent extends JComponent {
        private final IconType type;
        private final int size;
        private final boolean circle;

        public IconComponent(IconType type, int size, boolean circle) {
            this.type = type;
            this.size = size;
            this.circle = circle;
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(size, size);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            quality(g);
            double x0 = (getWidth() - size) / 2.0, y0 = (getHeight() - size) / 2.0;
            g.translate(x0, y0);
            g.setColor(Theme.PRIMARY_LIGHT);
            if (circle) g.fill(new Ellipse2D.Double(0, 0, size, size));
            else g.fill(new RoundRectangle2D.Double(0, 0, size, size, size * 0.3, size * 0.3));

            double s = size;
            g.setColor(Theme.PRIMARY);
            g.setStroke(new BasicStroke((float) (s * 0.06), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (type) {
                case FOLDER -> {
                    g.fill(new RoundRectangle2D.Double(s * 0.24, s * 0.28, s * 0.22, s * 0.14, 4, 4));
                    g.fill(new RoundRectangle2D.Double(s * 0.24, s * 0.36, s * 0.52, s * 0.36, 6, 6));
                }
                case COLUMNS -> {
                    g.draw(new RoundRectangle2D.Double(s * 0.26, s * 0.28, s * 0.48, s * 0.44, 6, 6));
                    g.drawLine((int) (s * 0.42), (int) (s * 0.28), (int) (s * 0.42), (int) (s * 0.72));
                    g.drawLine((int) (s * 0.58), (int) (s * 0.28), (int) (s * 0.58), (int) (s * 0.72));
                    g.drawLine((int) (s * 0.26), (int) (s * 0.42), (int) (s * 0.74), (int) (s * 0.42));
                }
                case RUN -> {
                    Polygon p = new Polygon();
                    p.addPoint((int) (s * 0.38), (int) (s * 0.26));
                    p.addPoint((int) (s * 0.38), (int) (s * 0.74));
                    p.addPoint((int) (s * 0.74), (int) (s * 0.50));
                    g.fill(p);
                }
                case CHART -> {
                    g.fill(new RoundRectangle2D.Double(s * 0.27, s * 0.50, s * 0.12, s * 0.22, 3, 3));
                    g.fill(new RoundRectangle2D.Double(s * 0.44, s * 0.34, s * 0.12, s * 0.38, 3, 3));
                    g.fill(new RoundRectangle2D.Double(s * 0.61, s * 0.24, s * 0.12, s * 0.48, 3, 3));
                }
                case UPLOAD -> {
                    g.drawLine((int) (s * 0.5), (int) (s * 0.62), (int) (s * 0.5), (int) (s * 0.28));
                    g.drawLine((int) (s * 0.5), (int) (s * 0.28), (int) (s * 0.38), (int) (s * 0.40));
                    g.drawLine((int) (s * 0.5), (int) (s * 0.28), (int) (s * 0.62), (int) (s * 0.40));
                    g.drawLine((int) (s * 0.30), (int) (s * 0.70), (int) (s * 0.70), (int) (s * 0.70));
                    g.drawLine((int) (s * 0.30), (int) (s * 0.62), (int) (s * 0.30), (int) (s * 0.70));
                    g.drawLine((int) (s * 0.70), (int) (s * 0.62), (int) (s * 0.70), (int) (s * 0.70));
                }
            }
            g.dispose();
        }
    }

    // ------------------------------------------------------------------ stepper
    /** The 1-2-3-4 progress indicator at the top of the window. */
    public static class StepperPanel extends JPanel {
        private final String[] labels;
        private int current;

        public StepperPanel(String[] labels) {
            this.labels = labels;
            setOpaque(false);
        }

        public void setCurrent(int step) {
            this.current = step;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(600, 88);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            quality(g);
            int n = labels.length, w = getWidth();
            double d = 46, top = 8, cy = top + d / 2;

            for (int i = 0; i < n - 1; i++) {
                double x1 = w * (2 * i + 1) / (2.0 * n) + d / 2 + 6;
                double x2 = w * (2 * i + 3) / (2.0 * n) - d / 2 - 6;
                g.setColor(i < current ? Theme.PRIMARY : Theme.LINE);
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new java.awt.geom.Line2D.Double(x1, cy, x2, cy));
            }
            for (int i = 0; i < n; i++) {
                double cx = w * (2 * i + 1) / (2.0 * n);
                Ellipse2D circle = new Ellipse2D.Double(cx - d / 2, top, d, d);
                if (i == current) {
                    g.setColor(new Color(37, 99, 235, 45));
                    g.fill(new Ellipse2D.Double(cx - d / 2 - 5, top - 5, d + 10, d + 10));
                }
                if (i <= current) {
                    g.setColor(Theme.PRIMARY);
                    g.fill(circle);
                    if (i < current) drawCheck(g, cx, cy, 22, Color.WHITE, 3f);
                    else drawCentered(g, String.valueOf(i + 1), Theme.font(Font.BOLD, 17f), Color.WHITE, cx, cy + 6);
                } else {
                    g.setColor(Color.WHITE);
                    g.fill(circle);
                    g.setColor(Theme.LINE);
                    g.setStroke(new BasicStroke(2f));
                    g.draw(circle);
                    drawCentered(g, String.valueOf(i + 1), Theme.font(Font.BOLD, 17f), Theme.MUTED, cx, cy + 6);
                }
                drawCentered(g, labels[i], Theme.font(Font.BOLD, 15f), i <= current ? Theme.PRIMARY : Theme.MUTED,
                        cx, top + d + 26);
            }
            g.dispose();
        }
    }

    // ------------------------------------------------------------------ drop zone
    /** Dashed upload area. Click to browse, or drag a file onto it. */
    public static class DropZone extends JPanel {
        private boolean hover, dragging;

        public DropZone(Runnable onBrowse, Consumer<File> onFile) {
            setLayout(new GridBagLayout());
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel content = new JPanel();
            content.setOpaque(false);
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

            IconComponent icon = new IconComponent(IconType.UPLOAD, 84, true);
            JLabel title = label("Drag & drop your CSV file here", 21, Font.BOLD, Theme.TEXT);
            JLabel sub = label("or click anywhere in this area to browse your computer", 14, Font.PLAIN, Theme.MUTED);
            RoundedButton btn = new RoundedButton("Browse Files", ButtonKind.PRIMARY);
            btn.addActionListener(e -> onBrowse.run());
            JLabel note = label("Supported format: .csv   \u2022   You will choose a numeric column next", 12, Font.PLAIN, Theme.MUTED);

            for (JComponent c : new JComponent[]{icon, title, sub, btn, note}) c.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(icon);
            content.add(Box.createVerticalStrut(16));
            content.add(title);
            content.add(Box.createVerticalStrut(6));
            content.add(sub);
            content.add(Box.createVerticalStrut(22));
            content.add(btn);
            content.add(Box.createVerticalStrut(18));
            content.add(note);
            add(content, new GridBagConstraints());

            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { onBrowse.run(); }
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });

            if (!GraphicsEnvironment.isHeadless()) {
                new DropTarget(this, new DropTargetAdapter() {
                    @Override public void dragEnter(DropTargetDragEvent e) { dragging = true; repaint(); }
                    @Override public void dragExit(DropTargetEvent e) { dragging = false; repaint(); }

                    @Override
                    @SuppressWarnings("unchecked")
                    public void drop(DropTargetDropEvent e) {
                        dragging = false;
                        repaint();
                        try {
                            e.acceptDrop(DnDConstants.ACTION_COPY);
                            Transferable t = e.getTransferable();
                            if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                                List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                                e.dropComplete(true);
                                if (!files.isEmpty()) onFile.accept(files.get(0));
                            } else {
                                e.dropComplete(false);
                            }
                        } catch (Exception ex) {
                            e.dropComplete(false);
                        }
                    }
                });
            }
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            quality(g);
            RoundRectangle2D r = new RoundRectangle2D.Double(2, 2, getWidth() - 5, getHeight() - 5, 24, 24);
            g.setColor(dragging || hover ? new Color(0xEAF1FF) : new Color(0xF6F8FF));
            g.fill(r);
            g.setColor(dragging ? Theme.PRIMARY : new Color(0xB3C4F0));
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{9f, 7f}, 0f));
            g.draw(r);
            g.dispose();
        }
    }

    // ------------------------------------------------------------------ progress bar
    public static class ProgressBar extends JComponent {
        private double value;

        public ProgressBar() { setOpaque(false); }

        public void setValue(double v) {
            value = Math.max(0, Math.min(1, v));
            repaint();
        }

        @Override public Dimension getPreferredSize() { return new Dimension(200, 12); }
        @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 12); }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            quality(g);
            int h = getHeight(), w = getWidth();
            g.setColor(new Color(0xE5E7EB));
            g.fill(new RoundRectangle2D.Double(0, 0, w, h, h, h));
            if (value > 0) {
                g.setColor(Theme.PRIMARY);
                g.fill(new RoundRectangle2D.Double(0, 0, Math.max(h, w * value), h, h, h));
            }
            g.dispose();
        }
    }

}
