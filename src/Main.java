// MEMBER 3 - WINDOW AND 4-STEP GUI WORKFLOW (1 of 3)
// Original application logic and UI implementation preserved.

import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

/* ========= Main.java ========= */
/**
 * Entry point of the Sorting Algorithm Performance Evaluator.
 * Launches the Swing GUI on the Event Dispatch Thread.
 */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to the default Swing look and feel.
        }
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}

/* ========= Window class: MainFrame ========= */
/** The application window. All UI lives in {@link AppPanel}. */
class MainFrame extends JFrame {

    public MainFrame() {
        super("Sorting Algorithm Performance Evaluator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(new AppPanel());
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(Math.min(1000, screen.width), Math.min(760, screen.height));
        setMinimumSize(new Dimension(860, Math.min(660, screen.height)));
        setLocationRelativeTo(null);
        setIconImage(createIcon());
    }

    private static Image createIcon() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        UiKit.quality(g);
        g.setColor(Theme.PRIMARY);
        g.fill(new RoundRectangle2D.Double(2, 2, 60, 60, 18, 18));
        g.setColor(Color.WHITE);
        g.fillRoundRect(14, 34, 9, 16, 4, 4);
        g.fillRoundRect(27, 22, 9, 28, 4, 4);
        g.fillRoundRect(40, 12, 9, 38, 4, 4);
        g.dispose();
        return img;
    }
}

/* ========= Wizard view class: AppPanel ========= */
/**
 * The whole application UI: a 4-step wizard
 * (1 Select CSV File, 2 Select Column, 3 Run Algorithms, 4 View Results).
 */
class AppPanel extends UiKit.GradientPanel {

    private static final int LARGE_DATA_WARNING = 200_000;

    private final PerformanceEvaluator evaluator = new PerformanceEvaluator();

    private final UiKit.StepperPanel stepper = new UiKit.StepperPanel(
            new String[]{"Select CSV File", "Select Column", "Run Algorithms", "View Results"});
    private final CardLayout cards = new CardLayout();
    private final JPanel host = new JPanel(cards);

    // state
    private CsvDataset dataset;
    private File file;

    // step 1
    private UiKit.RoundedPanel errorBox;
    private final JLabel errorLabel = UiKit.label(" ", 13, Font.PLAIN, Theme.DANGER);

    // step 2
    private final DefaultListModel<UiWidgets.ColumnItem> columnModel = new DefaultListModel<>();
    private final JList<UiWidgets.ColumnItem> columnList = new JList<>(columnModel);
    private final JLabel fileChip = UiKit.label(" ", 13, Font.BOLD, Theme.PRIMARY_DARK);
    private final JLabel hint = UiKit.label(" ", 13, Font.PLAIN, Theme.MUTED);
    private final UiKit.RoundedButton runButton = new UiKit.RoundedButton("Run Algorithms", UiKit.ButtonKind.PRIMARY);

    // step 3
    private final UiWidgets.RunPanel runPanel = new UiWidgets.RunPanel();

    // step 4
    private final JLabel bestTitle = UiKit.label(" ", 20, Font.BOLD, Theme.SUCCESS);
    private final JLabel bestSub = UiKit.label(" ", 13, Font.PLAIN, Theme.MUTED);
    private final UiWidgets.BarChartPanel chart = new UiWidgets.BarChartPanel();
    private final JTextArea preview = new JTextArea(3, 40);

    public AppPanel() {
        super(new BorderLayout());
        setBorder(new EmptyBorder(8, 34, 8, 34));

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.setBorder(new EmptyBorder(0, 30, 6, 30));
        north.add(stepper, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        UiKit.RoundedPanel card = new UiKit.RoundedPanel(28, Theme.CARD, 10).padding(14, 30, 14, 30);
        card.setLayout(new BorderLayout());
        host.setOpaque(false);
        host.add(buildStep1(), "1");
        host.add(buildStep2(), "2");
        host.add(buildStep3(), "3");
        host.add(buildStep4(), "4");
        card.add(host, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);

        goTo(0);
    }

    private void goTo(int step) {
        stepper.setCurrent(step);
        cards.show(host, String.valueOf(step + 1));
    }

    // ================================================================== STEP 1
    private JPanel buildStep1() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.add(header(UiKit.IconType.FOLDER, "Select CSV File", "Choose a csv file containing your dataset to begin"),
                BorderLayout.NORTH);
        p.add(new UiKit.DropZone(this::chooseFile, this::loadFile), BorderLayout.CENTER);

        errorBox = new UiKit.RoundedPanel(14, Theme.DANGER_LIGHT, 0).padding(10, 16, 10, 16);
        errorBox.setLayout(new BorderLayout());
        errorBox.add(errorLabel, BorderLayout.CENTER);
        errorBox.setVisible(false);
        p.add(errorBox, BorderLayout.SOUTH);
        return p;
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a CSV dataset");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            loadFile(chooser.getSelectedFile());
        }
    }

    /** Loads a CSV and moves to step 2, or shows an inline error. */
    public void loadFile(File f) {
        CsvDataset loaded;
        try {
            loaded = CsvLoader.load(f);
        } catch (IOException ex) {
            showStep1Error(ex.getMessage());
            return;
        } catch (RuntimeException ex) {
            showStep1Error("Unexpected error while reading the file: " + ex.getMessage());
            return;
        }
        dataset = loaded;
        file = f;
        errorBox.setVisible(false);

        columnModel.clear();
        List<String> headers = dataset.getHeaders();
        int firstNumeric = -1;
        for (int i = 0; i < headers.size(); i++) {
            boolean numeric = dataset.isNumericColumn(i);
            String name = headers.get(i).isEmpty() ? "Column " + (i + 1) : headers.get(i);
            columnModel.addElement(new UiWidgets.ColumnItem(i, name, numeric));
            if (numeric && firstNumeric < 0) firstNumeric = i;
        }
        fileChip.setText(f.getName() + "   \u2022   " + dataset.getRowCount() + " rows   \u2022   " + headers.size() + " columns");

        if (firstNumeric >= 0) {
            columnList.setSelectedIndex(firstNumeric);
            setHint("Choose the numeric column you want to sort.", Theme.MUTED);
        } else {
            columnList.clearSelection();
            setHint("This file has no numeric columns, so nothing can be sorted. Go back and pick another file.", Theme.DANGER);
        }
        runButton.setEnabled(columnList.getSelectedIndex() >= 0);
        goTo(1);
    }

    private void showStep1Error(String msg) {
        errorLabel.setText(msg);
        errorBox.setVisible(true);
        errorBox.revalidate();
        errorBox.repaint();
    }

    // ================================================================== STEP 2
    private JPanel buildStep2() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JPanel head = header(UiKit.IconType.COLUMNS, "Select Column", "Pick the numeric column that should be sorted");
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(head);

        UiKit.RoundedPanel chip = new UiKit.RoundedPanel(16, Theme.PRIMARY_LIGHT, 0).padding(6, 14, 6, 14);
        chip.setLayout(new BorderLayout());
        chip.add(fileChip, BorderLayout.CENTER);
        JPanel chipRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        chipRow.setOpaque(false);
        chipRow.add(chip);
        chipRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(chipRow);
        p.add(top, BorderLayout.NORTH);

        columnList.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int a, int b) {
                if (isNumeric(a)) super.setSelectionInterval(a, b);
            }
        });
        columnList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        columnList.setOpaque(false);
        columnList.setCellRenderer(new UiWidgets.ColumnRow());
        columnList.setFixedCellHeight(56);
        columnList.addListSelectionListener(e -> {
            UiWidgets.ColumnItem it = columnList.getSelectedValue();
            runButton.setEnabled(it != null);
            if (it != null) setHint("Selected: " + it.name, Theme.PRIMARY_DARK);
        });
        columnList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int i = columnList.locationToIndex(e.getPoint());
                Rectangle r = i >= 0 ? columnList.getCellBounds(i, i) : null;
                if (r != null && r.contains(e.getPoint()) && !isNumeric(i)) {
                    setHint("\u201C" + columnModel.get(i).name + "\u201D is not numeric, so it can't be sorted. Choose a column marked Numeric.",
                            Theme.DANGER);
                }
            }
        });
        JScrollPane sp = new JScrollPane(columnList);
        UiKit.styleScroll(sp);
        sp.setBorder(new EmptyBorder(4, 0, 4, 0));
        p.add(sp, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(12, 0));
        south.setOpaque(false);
        south.add(hint, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        UiKit.RoundedButton back = new UiKit.RoundedButton("Back", UiKit.ButtonKind.SECONDARY);
        back.addActionListener(e -> goTo(0));
        runButton.addActionListener(e -> runSort());
        runButton.setEnabled(false);
        buttons.add(back);
        buttons.add(runButton);
        south.add(buttons, BorderLayout.EAST);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private boolean isNumeric(int i) {
        return i >= 0 && i < columnModel.size() && columnModel.get(i).numeric;
    }

    private void setHint(String text, Color c) {
        hint.setText("<html>" + text + "</html>");
        hint.setForeground(c);
    }

    // ================================================================== STEP 3
    private JPanel buildStep3() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.add(header(UiKit.IconType.RUN, "Run Algorithms", "Every algorithm sorts its own copy of the selected column"),
                BorderLayout.NORTH);
        p.add(runPanel, BorderLayout.CENTER);
        return p;
    }

    /** Validates the selection, then runs all algorithms in the background. */
    public void runSort() {
        UiWidgets.ColumnItem item = columnList.getSelectedValue();
        if (dataset == null || item == null) {
            JOptionPane.showMessageDialog(this, "Please select a numeric column first.", "No column selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        final CsvDataset.ColumnData data;
        try {
            data = dataset.extractNumericColumn(item.index);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid column", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (data.values.length > LARGE_DATA_WARNING) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "This column has " + data.values.length + " values.\nInsertion Sort is O(n\u00B2) and may take a long time.\nContinue?",
                    "Large dataset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) return;
        }

        runPanel.reset(evaluator.getAlgorithmNames());
        goTo(2);

        new SwingWorker<List<SortResult>, Object>() {
            @Override
            protected List<SortResult> doInBackground() {
                return evaluator.evaluate(data.values, new PerformanceEvaluator.ProgressListener() {
                    @Override public void onStart(String algorithm, int index, int total) { publish(algorithm); }
                    @Override public void onFinish(SortResult result) { publish(result); }
                });
            }

            @Override
            protected void process(List<Object> chunks) {
                for (Object o : chunks) {
                    if (o instanceof String s) runPanel.markRunning(s);
                    else if (o instanceof SortResult r) runPanel.markDone(r);
                }
            }

            @Override
            protected void done() {
                try {
                    List<SortResult> results = get();
                    Timer t = new Timer(700, e -> showResults(results, item.name, data));
                    t.setRepeats(false);
                    t.start();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(AppPanel.this, cause.toString(), "Sorting failed", JOptionPane.ERROR_MESSAGE);
                    goTo(1);
                }
            }
        }.execute();
    }

    // ================================================================== STEP 4
    private JPanel buildStep4() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.add(header(UiKit.IconType.CHART, "View Results", "Execution time of each algorithm (shorter is better)"),
                BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        UiKit.RoundedPanel banner = new UiKit.RoundedPanel(18, Theme.SUCCESS_LIGHT, 0).padding(8, 20, 8, 20);
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        bestTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        bestSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        banner.add(bestTitle);
        banner.add(Box.createVerticalStrut(2));
        banner.add(bestSub);
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        banner.setMinimumSize(new Dimension(100, 66));
        banner.setPreferredSize(new Dimension(100, 66));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        center.add(banner);
        center.add(Box.createVerticalStrut(10));

        chart.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(chart);
        center.add(Box.createVerticalStrut(10));

        JLabel previewTitle = UiKit.label("Sorted output preview", 13, Font.BOLD, Theme.TEXT);
        previewTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(previewTitle);
        center.add(Box.createVerticalStrut(6));

        preview.setEditable(false);
        preview.setLineWrap(true);
        preview.setWrapStyleWord(true);
        preview.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        preview.setBackground(new Color(0xF8FAFC));
        preview.setForeground(Theme.TEXT);
        preview.setMargin(new Insets(8, 10, 8, 10));
        JScrollPane psp = new JScrollPane(preview);
        psp.setBorder(BorderFactory.createLineBorder(new Color(0xE5E7EB)));
        psp.setAlignmentX(Component.LEFT_ALIGNMENT);
        psp.setPreferredSize(new Dimension(100, 62));
        psp.setMinimumSize(new Dimension(100, 62));
        psp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        center.add(psp);
        p.add(center, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        UiKit.RoundedButton another = new UiKit.RoundedButton("Sort Another Column", UiKit.ButtonKind.SECONDARY);
        another.addActionListener(e -> goTo(1));
        UiKit.RoundedButton again = new UiKit.RoundedButton("Start Over", UiKit.ButtonKind.PRIMARY);
        again.addActionListener(e -> {
            dataset = null;
            errorBox.setVisible(false);
            goTo(0);
        });
        buttons.add(another);
        buttons.add(again);
        p.add(buttons, BorderLayout.SOUTH);
        return p;
    }

    private void showResults(List<SortResult> results, String columnName, CsvDataset.ColumnData data) {
        SortResult best = PerformanceEvaluator.findBest(results);
        List<SortResult> ranked = new ArrayList<>(results);
        ranked.sort((a, b) -> Double.compare(a.getTimeMillis(), b.getTimeMillis()));

        List<UiWidgets.ChartRow> rows = new ArrayList<>();
        for (SortResult r : ranked) {
            rows.add(new UiWidgets.ChartRow(r.getAlgorithm(), r.getTimeMillis(), r == best, r.isSortedCorrectly()));
        }
        chart.setData(rows);

        if (best != null) {
            bestTitle.setText("Best performer: " + best.getAlgorithm());
            bestSub.setText(String.format(Locale.US, "%.4f ms  \u2022  sorted %d values from column \u201C%s\u201D%s",
                    best.getTimeMillis(), data.values.length, columnName,
                    data.skipped > 0 ? "  \u2022  " + data.skipped + " empty cells skipped" : ""));
            preview.setText(buildPreview(best.getSortedData()));
        } else {
            bestTitle.setText("No algorithm produced a valid result");
            bestSub.setText(" ");
            preview.setText("");
        }
        preview.setCaretPosition(0);
        goTo(3);
    }

    private static String buildPreview(double[] sorted) {
        int n = sorted.length, show = Math.min(n, 12);
        StringBuilder sb = new StringBuilder("Smallest " + show + ":  ");
        for (int i = 0; i < show; i++) sb.append(fmt(sorted[i])).append(i < show - 1 ? ", " : "");
        if (n > show) {
            sb.append("\nLargest  ").append(show).append(":  ");
            for (int i = n - show; i < n; i++) sb.append(fmt(sorted[i])).append(i < n - 1 ? ", " : "");
        }
        return sb.toString();
    }

    private static String fmt(double d) {
        return (d == Math.rint(d) && Math.abs(d) < 1e15) ? String.valueOf((long) d) : String.valueOf(d);
    }

    // ================================================================== shared UI
    private JPanel header(UiKit.IconType type, String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout(14, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 6, 0));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(UiKit.label(title, 25, Font.BOLD, Theme.TEXT));
        text.add(UiKit.label(subtitle, 14, Font.PLAIN, Theme.MUTED));
        p.add(new UiKit.IconComponent(type, 46, false), BorderLayout.WEST);
        p.add(text, BorderLayout.CENTER);
        return p;
    }
}
