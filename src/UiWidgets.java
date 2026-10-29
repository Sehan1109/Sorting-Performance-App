// MEMBER 3 - VISUALIZATIONS, COLUMN ROWS AND PROGRESS WIDGETS (3 of 3)
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;


/** Visual result, progress and column list widgets (same look and behavior). */
final class UiWidgets {
    private UiWidgets() {}

    // ------------------------------------------------------------------ bar chart
    public record ChartRow(String name, double millis, boolean best, boolean valid) {}

    /** Animated horizontal bar chart comparing execution times. */
    public static class BarChartPanel extends JPanel {
        private static final int ROW_H = 38;
        private List<ChartRow> rows = List.of();
        private double progress = 1;
        private Timer timer;

        public BarChartPanel() { setOpaque(false); }

        public void setData(List<ChartRow> data) {
            this.rows = data;
            this.progress = 0;
            if (timer != null) timer.stop();
            timer = new Timer(16, e -> {
                progress += 0.045;
                if (progress >= 1) {
                    progress = 1;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            });
            timer.start();
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(500, Math.max(1, rows.size()) * ROW_H + 4);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            UiKit.quality(g);
            if (rows.isEmpty()) { g.dispose(); return; }
            double max = rows.stream().mapToDouble(ChartRow::millis).max().orElse(1);
            if (max <= 0) max = 1;
            int labelW = 140, valueW = 120, rowH = ROW_H, barH = 20;
            int trackX = labelW, trackW = Math.max(50, getWidth() - labelW - valueW);
            double eased = 1 - Math.pow(1 - progress, 3);

            for (int i = 0; i < rows.size(); i++) {
                ChartRow r = rows.get(i);
                int y = i * rowH + (rowH - barH) / 2;
                g.setFont(Theme.font(r.best() ? Font.BOLD : Font.PLAIN, 14f));
                g.setColor(Theme.TEXT);
                FontMetrics fm = g.getFontMetrics();
                g.drawString(r.name(), 0, y + (barH + fm.getAscent() - fm.getDescent()) / 2);

                g.setColor(new Color(0xEEF2FF));
                g.fill(new RoundRectangle2D.Double(trackX, y, trackW, barH, barH, barH));
                double frac = Math.max(0.025, r.millis() / max) * eased;
                g.setColor(!r.valid() ? Theme.DANGER : r.best() ? Theme.SUCCESS : Theme.PRIMARY);
                g.fill(new RoundRectangle2D.Double(trackX, y, Math.max(barH, trackW * frac), barH, barH, barH));

                String val = r.valid() ? String.format(Locale.US, "%.4f ms", r.millis()) : "not sorted!";
                g.setFont(Theme.font(Font.BOLD, 14f));
                g.setColor(!r.valid() ? Theme.DANGER : r.best() ? Theme.SUCCESS : Theme.TEXT);
                fm = g.getFontMetrics();
                g.drawString(val, getWidth() - valueW + 14, y + (barH + fm.getAscent() - fm.getDescent()) / 2);
            }
            g.dispose();
        }
    }

    // ------------------------------------------------------------------ scrollbar
    public static class ModernScrollBarUI extends BasicScrollBarUI {
        private JButton zero() {
            JButton b = new JButton();
            Dimension d = new Dimension(0, 0);
            b.setPreferredSize(d);
            b.setMinimumSize(d);
            b.setMaximumSize(d);
            return b;
        }

        @Override protected JButton createDecreaseButton(int orientation) { return zero(); }
        @Override protected JButton createIncreaseButton(int orientation) { return zero(); }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) { }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            UiKit.quality(g2);
            g2.setColor(new Color(0xC3CCF0));
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }
    }

    // Display widgets used by AppPanel (kept identical to the original UI).
    /** Item in the column list. */
    static class ColumnItem {
        final int index;
        final String name;
        final boolean numeric;

        ColumnItem(int index, String name, boolean numeric) {
            this.index = index;
            this.name = name;
            this.numeric = numeric;
        }
    }

    /** Renders each column as a rounded card with a Numeric / Text badge. */
    static class ColumnRow extends JComponent implements ListCellRenderer<ColumnItem> {
        private ColumnItem item;
        private boolean selected;

        @Override
        public Component getListCellRendererComponent(JList<? extends ColumnItem> list, ColumnItem value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            this.item = value;
            this.selected = isSelected;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            if (item == null) return;
            Graphics2D g = (Graphics2D) g0.create();
            UiKit.quality(g);
            int w = getWidth(), h = getHeight();
            RoundRectangle2D box = new RoundRectangle2D.Double(1, 4, w - 3, h - 8, 16, 16);
            g.setColor(selected ? new Color(0xEFF5FF) : Color.WHITE);
            g.fill(box);
            g.setColor(selected ? Theme.PRIMARY : new Color(0xE5E7EB));
            g.setStroke(new BasicStroke(selected ? 2f : 1.2f));
            g.draw(box);

            // radio dot
            double cy = h / 2.0;
            g.setColor(selected ? Theme.PRIMARY : Theme.LINE);
            g.setStroke(new BasicStroke(2f));
            g.draw(new Ellipse2D.Double(18, cy - 9, 18, 18));
            if (selected) g.fill(new Ellipse2D.Double(23, cy - 4, 8, 8));

            g.setFont(Theme.font(Font.BOLD, 15f));
            g.setColor(item.numeric ? Theme.TEXT : Theme.MUTED);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(item.name, 50, (int) (cy + (fm.getAscent() - fm.getDescent()) / 2.0));

            String badge = item.numeric ? "Numeric" : "Text \u2022 not sortable";
            Font bf = Theme.font(Font.BOLD, 12f);
            g.setFont(bf);
            fm = g.getFontMetrics();
            int bw = fm.stringWidth(badge) + 22, bh = 26;
            double bx = w - bw - 18, by = cy - bh / 2.0;
            g.setColor(item.numeric ? Theme.SUCCESS_LIGHT : new Color(0xF3F4F6));
            g.fill(new RoundRectangle2D.Double(bx, by, bw, bh, bh, bh));
            g.setColor(item.numeric ? Theme.SUCCESS : Theme.MUTED);
            g.drawString(badge, (float) (bx + 11), (float) (by + (bh + fm.getAscent() - fm.getDescent()) / 2.0));
            g.dispose();
        }
    }

    // ================================================================== step 3 widgets
    static class RunPanel extends JPanel {
        private final JPanel rows = new JPanel(new GridLayout(0, 1, 0, 10));
        private final Map<String, AlgoRow> map = new LinkedHashMap<>();
        private final UiKit.ProgressBar bar = new UiKit.ProgressBar();
        private final JLabel summary = UiKit.label(" ", 14, Font.BOLD, Theme.TEXT);
        private int done, total;

        RunPanel() {
            super(new BorderLayout());
            setOpaque(false);
            rows.setOpaque(false);

            JPanel box = new JPanel();
            box.setOpaque(false);
            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
            summary.setAlignmentX(Component.LEFT_ALIGNMENT);
            bar.setAlignmentX(Component.LEFT_ALIGNMENT);
            rows.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(summary);
            box.add(Box.createVerticalStrut(8));
            box.add(bar);
            box.add(Box.createVerticalStrut(18));
            box.add(rows);
            add(box, BorderLayout.NORTH);
        }

        void reset(List<String> names) {
            rows.removeAll();
            map.clear();
            done = 0;
            total = names.size();
            for (String n : names) {
                AlgoRow r = new AlgoRow(n);
                map.put(n, r);
                rows.add(r);
            }
            bar.setValue(0);
            summary.setText("0 of " + total + " algorithms complete");
            rows.revalidate();
            rows.repaint();
        }

        void markRunning(String name) {
            AlgoRow r = map.get(name);
            if (r != null) r.setRunning();
        }

        void markDone(SortResult result) {
            AlgoRow r = map.get(result.getAlgorithm());
            if (r != null) r.setDone(result);
            done++;
            bar.setValue(done / (double) total);
            summary.setText(done + " of " + total + " algorithms complete");
        }
    }

    private static class AlgoRow extends JComponent {
        private enum State { WAITING, RUNNING, DONE }

        private final String name;
        private State state = State.WAITING;
        private SortResult result;
        private int angle;
        private final Timer spinner = new Timer(30, e -> {
            angle = (angle + 18) % 360;
            repaint();
        });

        AlgoRow(String name) {
            this.name = name;
            setOpaque(false);
        }

        void setRunning() {
            state = State.RUNNING;
            spinner.start();
            repaint();
        }

        void setDone(SortResult r) {
            spinner.stop();
            state = State.DONE;
            result = r;
            repaint();
        }

        @Override public void removeNotify() { spinner.stop(); super.removeNotify(); }
        @Override public Dimension getPreferredSize() { return new Dimension(300, 52); }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            UiKit.quality(g);
            int w = getWidth(), h = getHeight();
            RoundRectangle2D box = new RoundRectangle2D.Double(1, 1, w - 3, h - 3, 16, 16);
            Color fill = state == State.DONE ? new Color(0xF0FDF4) : state == State.RUNNING ? new Color(0xEFF6FF) : new Color(0xF9FAFB);
            Color line = state == State.DONE ? new Color(0xBBF7D0) : state == State.RUNNING ? new Color(0xBFDBFE) : new Color(0xE5E7EB);
            g.setColor(fill);
            g.fill(box);
            g.setColor(line);
            g.setStroke(new BasicStroke(1.2f));
            g.draw(box);

            double cx = 30, cy = h / 2.0;
            switch (state) {
                case WAITING -> {
                    g.setColor(Theme.LINE);
                    g.setStroke(new BasicStroke(2f));
                    g.draw(new Ellipse2D.Double(cx - 10, cy - 10, 20, 20));
                }
                case RUNNING -> {
                    g.setColor(new Color(0xC7D8FB));
                    g.setStroke(new BasicStroke(3f));
                    g.draw(new Ellipse2D.Double(cx - 10, cy - 10, 20, 20));
                    g.setColor(Theme.PRIMARY);
                    g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.draw(new Arc2D.Double(cx - 10, cy - 10, 20, 20, -angle, 100, Arc2D.OPEN));
                }
                case DONE -> {
                    g.setColor(result != null && result.isSortedCorrectly() ? Theme.SUCCESS : Theme.DANGER);
                    g.fill(new Ellipse2D.Double(cx - 11, cy - 11, 22, 22));
                    UiKit.drawCheck(g, cx, cy, 14, Color.WHITE, 2.4f);
                }
            }

            g.setFont(Theme.font(Font.BOLD, 15f));
            g.setColor(state == State.WAITING ? Theme.MUTED : Theme.TEXT);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(name, 58, (int) (cy + (fm.getAscent() - fm.getDescent()) / 2.0));

            String right = switch (state) {
                case WAITING -> "Waiting";
                case RUNNING -> "Running\u2026";
                case DONE -> String.format(Locale.US, "%.4f ms", result.getTimeMillis());
            };
            g.setFont(Theme.font(state == State.DONE ? Font.BOLD : Font.PLAIN, 14f));
            g.setColor(state == State.RUNNING ? Theme.PRIMARY : state == State.DONE ? Theme.SUCCESS : Theme.MUTED);
            fm = g.getFontMetrics();
            g.drawString(right, w - 22 - fm.stringWidth(right), (int) (cy + (fm.getAscent() - fm.getDescent()) / 2.0));
            g.dispose();
        }
    }
}


/* ========= Shared Theme palette (included in this UI source file) ========= */
/** Central place for colours and fonts so the whole UI stays consistent. */
final class Theme {
    private Theme() {}

    public static final Color PRIMARY       = new Color(0x2563EB);
    public static final Color PRIMARY_DARK  = new Color(0x1D4ED8);
    public static final Color PRIMARY_LIGHT = new Color(0xDBEAFE);
    public static final Color TEXT          = new Color(0x111827);
    public static final Color MUTED         = new Color(0x6B7280);
    public static final Color LINE          = new Color(0xD1D5DB);
    public static final Color SUCCESS       = new Color(0x16A34A);
    public static final Color SUCCESS_LIGHT = new Color(0xDCFCE7);
    public static final Color DANGER        = new Color(0xDC2626);
    public static final Color DANGER_LIGHT  = new Color(0xFEE2E2);
    public static final Color CARD          = Color.WHITE;
    public static final Color BG_TOP        = new Color(0xF8FAFF);
    public static final Color BG_BOTTOM     = new Color(0xDCE3FB);

    /** Segoe UI on Windows; Java falls back to a default sans font elsewhere. */
    public static Font font(int style, float size) {
        return new Font("Segoe UI", Font.PLAIN, 12).deriveFont(style, size);
    }
}

