package de.uni.freiburg.iig.telematik.secsy.gui.dialog;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import de.invation.code.toval.time.TimeScale;
import de.invation.code.toval.time.TimeValue;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.ConfigurationException;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.Simulation;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.SimulationRun;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.AbstractTransformer;

public class TimeFrameDialog extends JDialog {

    private static final long serialVersionUID = 4501959307493776929L;
    private static final int buttonPanelHeight = 40;
    private static final int preferredWidth = 600;

    private TimeFramePanel timeFramePanel = null;

    public TimeFrameDialog(Window parent, Simulation simulation) throws ConfigurationException {
        super(parent);
        setModal(true);

        setTitle("Time Frame");

        getContentPane().setLayout(new BorderLayout());

        getContentPane().add(getTimeFramePanel(simulation), BorderLayout.CENTER);

        JPanel buttonPane = new JPanel();
        buttonPane.setPreferredSize(new Dimension(preferredWidth, buttonPanelHeight));
        getContentPane().add(buttonPane, BorderLayout.SOUTH);
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));

        buttonPane.add(Box.createHorizontalGlue());

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        okButton.setActionCommand("OK");
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);

        buttonPane.add(Box.createHorizontalGlue());
        pack();

        setLocationRelativeTo(parent);
        setVisible(true);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(preferredWidth, timeFramePanel.getPreferredSize().height + buttonPanelHeight + 50);
    }

    private Component getTimeFramePanel(Simulation simulation) throws ConfigurationException {
        JScrollPane scrollPane = new JScrollPane();
        timeFramePanel = new TimeFramePanel(simulation);
        scrollPane.setViewportView(timeFramePanel);
        scrollPane.setPreferredSize(new Dimension(preferredWidth, timeFramePanel.getPreferredSize().height));
        return scrollPane;
    }

    private class TimeFramePanel extends JPanel {

        private static final long serialVersionUID = -3708244174165780349L;

        private static final int minBoxWidth = 200;
        private static final int minBoxHeight = 200;
        private int preferredBoxHeight = 200;

        private static final int marginLeft = 30;
        private static final int marginRight = 30;
        private static final int marginTop = 20;
        private static final int marginBottom = 40;
        private static final int margin = 20;

        private List<SimulationRun> simulationRuns = null;
        private Simulation simulation = null;
        private Long sumPasses = 0L;
        private Integer minPasses = Integer.MAX_VALUE;
        private Double factor = null;

        public TimeFramePanel(Simulation simulation) throws ConfigurationException {
            setBorder(new EmptyBorder(5, 5, 5, 5));
            setBackground(Color.white);
            prepare(simulation);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension((int) Math.round(sumPasses * factor) + (simulationRuns.size() - 1) * margin + marginLeft + marginRight, preferredBoxHeight + marginTop + marginBottom);
        }

        private void prepare(Simulation simulation) throws ConfigurationException {
            this.simulation = simulation;
            simulationRuns = simulation.getSimulationRuns();

            int maxTransformerCount = 0;
            for (SimulationRun run : simulationRuns) {
                sumPasses += run.getPasses();
                if (run.getPasses() < minPasses) {
                    minPasses = run.getPasses();
                }
                if (run.getTraceTransformerManager().getTraceTransformers().size() > maxTransformerCount) {
                    maxTransformerCount = run.getTraceTransformerManager().getTraceTransformers().size();
                }
                if (maxTransformerCount < 7) {
                    preferredBoxHeight = minBoxHeight;
                } else {
                    preferredBoxHeight = minBoxHeight + (maxTransformerCount - 6) * 20;
                }
            }
            factor = minBoxWidth / minPasses.doubleValue();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;

            g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{2f}, 0f));

            g2d.drawLine(marginLeft - (int) Math.round(margin / 2.0), marginTop, marginLeft - (int) Math.round(margin / 2.0), marginTop + preferredBoxHeight + 20);
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            Date startDate = new Date(simulation.getCaseTimeGenerator().getStartTime());
            g2d.drawString(sdf.format(startDate), marginLeft, marginTop + preferredBoxHeight + 20);

            Date approxDate = startDate;
            int count = 1;
            int lastPartEnd = marginLeft;
            for (SimulationRun run : simulationRuns) {
                int width = (int) Math.round(run.getPasses() * factor);
                g2d.setColor(Color.lightGray);
                g2d.fillRect(lastPartEnd, marginTop, width, preferredBoxHeight);
                g2d.setColor(Color.black);
                g2d.drawRect(lastPartEnd, marginTop, width, preferredBoxHeight);
                g2d.drawString(run.getName(), lastPartEnd + 10, marginTop + 20);
                g2d.drawString("Net: " + run.getPetriNet().getName(), lastPartEnd + 10, marginTop + 40);
                g2d.drawString("Passes: " + run.getPasses().toString(), lastPartEnd + 10, marginTop + 60);

                if (!run.getTraceTransformerManager().getTraceTransformers().isEmpty()) {
                    g2d.drawString("Transformers:", lastPartEnd + 10, marginTop + 90);
                    int transformerCount = 0;
                    for (AbstractTransformer transformer : run.getTraceTransformerManager().getTraceTransformers()) {
                        g2d.drawString(transformer.getName(), lastPartEnd + 10, marginTop + 90 + (++transformerCount) * 20);
                    }
                } else {
                    g2d.drawString("No transformers", lastPartEnd + 10, marginTop + 90);
                }

                if (count++ <= simulationRuns.size()) {
                    g2d.drawLine(lastPartEnd + width + (int) Math.round(margin / 2.0), marginTop, lastPartEnd + width + (int) Math.round(margin / 2.0), marginTop + preferredBoxHeight + 20);
                    Double approxDays = run.getPasses().doubleValue() / simulation.getCaseTimeGenerator().getMaxCasesPerDay();
                    TimeValue timeValue = null;
                    try {
                        timeValue = new TimeValue(approxDays.intValue(), TimeScale.DAYS);
                    } catch (ParameterException e) {
                        e.printStackTrace();
                    }
                    approxDate = new Date(approxDate.getTime() + timeValue.getValueInMilliseconds());
                    if (count < simulationRuns.size()) {
                        g2d.drawString("?" + sdf.format(approxDate), lastPartEnd + width + margin, marginTop + preferredBoxHeight + 20);
                    } else {
                        g2d.drawString("?" + sdf.format(approxDate), lastPartEnd + width + margin - 100, marginTop + preferredBoxHeight + 20);
                    }
                }

                lastPartEnd += width + margin;
            }

        }

    }

}
