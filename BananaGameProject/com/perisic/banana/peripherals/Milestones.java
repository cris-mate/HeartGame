package com.perisic.banana.peripherals;

import com.perisic.banana.engine.DBManager;
import com.perisic.banana.engine.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

public class Milestones extends JFrame {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String currentPlayer;
    private final SessionManager session;
    private final DBManager dbManager;
    private final GameNavigator navigator;

    public Milestones(String currentPlayer, DBManager dbManager, SessionManager session) {
        super("Six Equations Solver");

        this.currentPlayer = currentPlayer;
        this.dbManager = dbManager;
        this.session = session;
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
        contentPanel.add(buildMilestonesPanel());
        add(contentPanel);
    }

    private JPanel buildUserPanel() {
        return UIFactory.buildUserPanel(currentPlayer);
    }

    private JPanel buildControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        controlPanel.setBackground(UIFactory.Colors.BACKGROUND);

        // creating control buttons
        controlPanel.add(UIFactory.createButton(UIFactory.Buttons.CTRL_Buttons.PLAY_GAME,
                "PLAY_GAME", e -> {
                    this.dispose();
                    session.playGame(navigator);
                }));
        controlPanel.add(UIFactory.createButton(UIFactory.Buttons.CTRL_Buttons.HOME,
                "HOME", e -> {
                    this.dispose();
                    navigator.openHome();
                }));
        controlPanel.add(UIFactory.createButton(UIFactory.Buttons.CTRL_Buttons.HALL_OF_FAME,
                "HALL_OF_FAME", e -> {
                    this.dispose();
                    navigator.openHallOfFame();
                }));
        controlPanel.add(UIFactory.createButton(UIFactory.Buttons.CTRL_Buttons.SWITCH_ACCOUNT,
                "SWITCH_ACCOUNT", e -> {
                    this.dispose();
                    session.switchAccount(dbManager);
                }));
        return controlPanel;
    }

    private JPanel buildMilestonesPanel() {
        JPanel milestonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 200));
        milestonesPanel.setBackground(UIFactory.Colors.BACKGROUND);

        JLabel milestoneLabel = UIFactory.Labels.label("Milestones coming soon — building in progress!",
                UIFactory.Fonts.GREETING);
        milestoneLabel.setForeground(UIFactory.Colors.TEXT_SECONDARY);
        milestonesPanel.add(milestoneLabel);

        return milestonesPanel;
    }

}