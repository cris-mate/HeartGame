package com.perisic.banana.peripherals;

import com.perisic.banana.engine.DBManager;
import com.perisic.banana.engine.SessionManager;
import com.perisic.banana.engine.HallOfFameEntry;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.io.Serial;
import java.util.List;


public class HallOfFame extends JFrame {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String currentPlayer;
    private final SessionManager session;
    private final DBManager dbManager;
    private final GameNavigator navigator;

    public HallOfFame(String currentPlayer, DBManager dbManager, SessionManager session) {
        super("Six Equations Solver");

        this.currentPlayer = currentPlayer;
        this.session = session;
        this.dbManager = dbManager;
        this.navigator = new GameNavigator(currentPlayer, dbManager, session);

        UIFactory.defaultFrame(this);
        buildComponents();
    }

    private void buildComponents() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UIFactory.Colors.BACKGROUND);
        contentPanel.add(buildUserPanel());
        contentPanel.add(buildControlPanel());
        contentPanel.add(buildGreetingPanel());
        contentPanel.add(buildTitlePanel());
        contentPanel.add(buildHallOfFamePanel());

        add(contentPanel);
    }

    private JPanel buildUserPanel() {
        return UIFactory.buildUserPanel(currentPlayer);
    }

    private JPanel buildControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setBackground(UIFactory.Colors.BACKGROUND);
        controlPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // creates control buttons
        controlPanel.add(UIFactory.createButton(UIFactory.Buttons.CTRL_Buttons.PLAY_GAME,
                "PLAY_GAME",
                e -> {
                    this.dispose();
                    session.playGame(navigator);
                }));
        controlPanel.add(UIFactory.createButton(UIFactory.Buttons.CTRL_Buttons.HOME,
                "HOME",
                e -> {
                    this.dispose();
                    navigator.openHome();
                }));
        controlPanel.add(UIFactory.createButton(UIFactory.Buttons.CTRL_Buttons.MILESTONES,
                "MILESTONES",
                e -> {
                    this.dispose();
                    navigator.openMilestones();
                }));
        controlPanel.add(UIFactory.createButton(UIFactory.Buttons.CTRL_Buttons.SWITCH_ACCOUNT,
                "SWITCH_ACCOUNT",
                e -> {
                    this.dispose();
                    session.switchAccount(dbManager);
                }));
        return controlPanel;
    }

    private JPanel buildGreetingPanel() {
        JPanel greetingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        greetingPanel.setBackground(UIFactory.Colors.BACKGROUND);
        greetingPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel greetingLabel = UIFactory.Labels.label("Banana Equation Game",
                UIFactory.Fonts.GREETING);
        greetingLabel.setForeground(UIFactory.Colors.TEXT_SECONDARY);
        greetingPanel.add(greetingLabel);
        return greetingPanel;
    }

    private JPanel buildTitlePanel() {
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        titlePanel.setBackground(UIFactory.Colors.BACKGROUND);
        titlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel titleLabel = UIFactory.Labels.label("Hall of Fame",
                UIFactory.Fonts.TITLE);
        titleLabel.setForeground(UIFactory.Colors.TEXT_PRIMARY);
        titlePanel.add(titleLabel);
        return titlePanel;
    }

    private JPanel buildHallOfFamePanel() {
        JPanel hallOfFamePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        hallOfFamePanel.setBackground(UIFactory.Colors.BACKGROUND);

        String[] columnNames = { "Player", "Highest Score", "Win Streak" };
        List<HallOfFameEntry> entries = dbManager.getHallOfFameEntries();

        String[][] data = new String[entries.size()][3];
        for (int i = 0; i < entries.size(); i++) {
            HallOfFameEntry entry = entries.get(i);
            data[i][0] = entry.getPlayer();
            data[i][1] = String.valueOf(entry.getHighestScore());
            data[i][2] = String.valueOf(entry.getWinStreak());
        }

        JTable table = new JTable(data, columnNames);
        table.setEnabled(false);
        table.setFillsViewportHeight(true);
        table.setPreferredSize(new Dimension(600, 400));
        table.setBackground(UIFactory.Colors.BACKGROUND);
        table.setForeground(UIFactory.Colors.TEXT_SECONDARY);
        table.setFont(UIFactory.Fonts.TEXT);
        table.setRowHeight(35);
        JTableHeader header = table.getTableHeader();
        header.setFont(UIFactory.Fonts.MESSAGE);
        header.setPreferredSize(new Dimension(200, 40));
        JScrollPane scrollPane = new JScrollPane(table);
        hallOfFamePanel.add(scrollPane);

        return hallOfFamePanel;
    }
}
