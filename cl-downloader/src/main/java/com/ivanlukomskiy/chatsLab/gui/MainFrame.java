package com.ivanlukomskiy.chatsLab.gui;

import com.google.gson.JsonSyntaxException;
import com.ivanlukomskiy.chatsLab.model.ChatGuiDto;
import com.ivanlukomskiy.chatsLab.service.VkService;
import com.ivanlukomskiy.chatsLab.util.Localization;
import com.ivanlukomskiy.chatsLab.util.LocalizationHolder;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.io.IOException;
import java.util.List;

import static com.ivanlukomskiy.chatsLab.service.IOService.INSTANCE;
import static com.ivanlukomskiy.chatsLab.util.LocalizationHolder.LOCALIZATION_RESOURCE;
import static com.ivanlukomskiy.chatsLab.util.LocalizationHolder.localization;
import static java.awt.EventQueue.invokeLater;
import static javax.swing.GroupLayout.Alignment.*;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;
import static javax.swing.SwingConstants.CENTER;
import static javax.swing.SwingConstants.TOP;
import static javax.swing.UIManager.getInstalledLookAndFeels;
import static javax.swing.UIManager.setLookAndFeel;

/**
 * Downloader GUI
 * @author ivan_l
 */
public class MainFrame extends javax.swing.JFrame implements DownloadingStatusListener {
    private static final Logger logger = LogManager.getLogger(MainFrame.class);

    private VkService vkService = new VkService();
    private List<ChatGuiDto> chats;

    @SneakyThrows
    public static void main(String args[]) {
        for (String arg : args) {
            if ("-locale=en".equals(arg)) {
                LocalizationHolder.localization = new Localization(LOCALIZATION_RESOURCE, "en");
                break;
            }
        }
        // Set look and feel
        try {
            for (LookAndFeelInfo info : getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Failed to set look and feel", e);
        }

        // Create and display the form
        invokeLater(() -> new MainFrame().setVisible(true));
    }

    @Override
    public void changeText(String text) {
        setStatusGui(text);
    }

    private MainFrame() {
        initComponents();

        SwingWorker worker = new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground() {
                try {
                    INSTANCE.deserialize();
                    switchToCard("loadOrCreateDialogue");
                } catch (IOException | JsonSyntaxException e) {
                    switchToCard("startAuth");
                }
                return null;
            }
        };
        worker.execute();
    }

    private void switchToCard(String cardName) {
        ((CardLayout) containerPanel.getLayout()).show(containerPanel, cardName);
    }

    private void setStatusGui(String status) {
        new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground() {
                statusLabel.setText("<html>" + status);
                switchToCard("status");
                return null;
            }
        }.run();
    }

    private String cardToGo;

    private void setFailedGui(String status, String buttonName, String cardToGo) {
        System.out.println("setting failed gui");
        this.cardToGo = cardToGo;
        new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground() {
                System.out.println("setting failed gui2");

                failedLabel.setText(status);
                failedButton.setText(buttonName);
                switchToCard("failed");
                System.out.println("setting failed gui3");

                return null;
            }
        }.run();
    }

    private void showChatsSelectCard() {
        chatsTable.setModel(new ChatsListTableModel(chats));
        chatsTable.getColumnModel().getColumn(0).setMaxWidth(50);
        //chatsTable.getColumnModel().getColumn(1).setMaxWidth(70);
        logger.info("Switching to the list card...");
        switchToCard("listCard");
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        containerPanel = new JPanel();
        JPanel statusCard = new JPanel();
        statusLabel = new JLabel();
        JPanel startAuthCard = new JPanel();
        JLabel startAuthLabel = new JLabel();
        userCodeField = new JTextField();
        JButton openLinkButton = new JButton();
        JLabel pasteUsersCodeLabel = new JLabel();
        JButton startAuthNextButton = new JButton();
        saveToDiskBox = new JCheckBox();
        JPanel failedCard = new JPanel();
        failedLabel = new JLabel();
        failedButton = new JButton();
        JPanel loadOrCreateDialogCard = new JPanel();
        JLabel loadOrCreateLabel = new JLabel();
        JButton createNewOneButton = new JButton();
        JButton useStoredButton = new JButton();
        JPanel listCard = new JPanel();
        JScrollPane jScrollPane1 = new JScrollPane();
        chatsTable = new JTable();
        JLabel jLabel1 = new JLabel();
        JButton jButton1 = new JButton();
        JPanel downloadingFinishedCard = new JPanel();
        JLabel jLabel3 = new JLabel();
        JButton jButton3 = new JButton();
        JButton jButton4 = new JButton();
        JPanel jPanel1 = new JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("ChatsLab");

        containerPanel.setLayout(new CardLayout());

        statusLabel.setHorizontalAlignment(CENTER);
        statusLabel.setText("Trying to read the config...");

        GroupLayout statusCardLayout = new GroupLayout(statusCard);
        statusCard.setLayout(statusCardLayout);
        statusCardLayout.setHorizontalGroup(
                statusCardLayout.createParallelGroup(LEADING)
                        .addComponent(statusLabel, DEFAULT_SIZE, 635, Short.MAX_VALUE)
        );
        statusCardLayout.setVerticalGroup(
                statusCardLayout.createParallelGroup(LEADING)
                        .addComponent(statusLabel, DEFAULT_SIZE, 509, Short.MAX_VALUE)
        );

        containerPanel.add(statusCard, "status");

        startAuthLabel.setText(localization.getText("welcome.first_boot.text"));
        startAuthLabel.setVerticalAlignment(TOP);

        openLinkButton.setText(localization.getText("welcome.first_boot.open_link"));
        openLinkButton.addActionListener(evt -> openLinkButtonActionPerformed());

        pasteUsersCodeLabel.setText(localization.getText("welcome.first_boot.user_code"));

        startAuthNextButton.setText(localization.getText("welcome.first_boot.next"));
        startAuthNextButton.addActionListener(evt -> startAuthNextButtonActionPerformed());

        saveToDiskBox.setText(localization.getText("welcome.first_boot.save_to_disk"));

        GroupLayout startAuthCardLayout = new GroupLayout(startAuthCard);
        startAuthCard.setLayout(startAuthCardLayout);
        startAuthCardLayout.setHorizontalGroup(
                startAuthCardLayout.createParallelGroup(LEADING)
                        .addGroup(startAuthCardLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(startAuthCardLayout.createParallelGroup(LEADING)
                                        .addComponent(startAuthLabel, TRAILING, PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                        .addComponent(openLinkButton, DEFAULT_SIZE, 611, Short.MAX_VALUE)
                                        .addGroup(startAuthCardLayout.createSequentialGroup()
                                                .addComponent(userCodeField)
                                                .addPreferredGap(RELATED)
                                                .addComponent(startAuthNextButton))
                                        .addGroup(startAuthCardLayout.createSequentialGroup()
                                                .addComponent(saveToDiskBox)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addComponent(pasteUsersCodeLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );
        startAuthCardLayout.setVerticalGroup(
                startAuthCardLayout.createParallelGroup(LEADING)
                        .addGroup(startAuthCardLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(startAuthLabel, DEFAULT_SIZE, 372, Short.MAX_VALUE)
                                .addPreferredGap(RELATED)
                                .addComponent(openLinkButton)
                                .addPreferredGap(RELATED)
                                .addComponent(pasteUsersCodeLabel)
                                .addPreferredGap(RELATED)
                                .addGroup(startAuthCardLayout.createParallelGroup(BASELINE)
                                        .addComponent(userCodeField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                        .addComponent(startAuthNextButton))
                                .addPreferredGap(RELATED)
                                .addComponent(saveToDiskBox)
                                .addContainerGap())
        );

        containerPanel.add(startAuthCard, "startAuth");

        failedLabel.setHorizontalAlignment(CENTER);

        failedButton.setText(localization.getText("authentication.try_again"));
        failedButton.addActionListener(evt -> failedButtonActionPerformed());

        GroupLayout failedCardLayout = new GroupLayout(failedCard);
        failedCard.setLayout(failedCardLayout);
        failedCardLayout.setHorizontalGroup(
                failedCardLayout.createParallelGroup(LEADING)
                        .addGroup(failedCardLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(failedCardLayout.createParallelGroup(LEADING)
                                        .addComponent(failedLabel, TRAILING, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(failedButton, DEFAULT_SIZE, 611, Short.MAX_VALUE))
                                .addContainerGap())
        );
        failedCardLayout.setVerticalGroup(
                failedCardLayout.createParallelGroup(LEADING)
                        .addGroup(failedCardLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(failedLabel, DEFAULT_SIZE, 451, Short.MAX_VALUE)
                                .addPreferredGap(RELATED)
                                .addComponent(failedButton)
                                .addContainerGap())
        );

        containerPanel.add(failedCard, "failed");

        loadOrCreateLabel.setText(localization.getText("welcome.token_saved.text"));
        loadOrCreateLabel.setVerticalAlignment(TOP);

        createNewOneButton.setText(localization.getText("welcome.token_saved.create_new"));
        createNewOneButton.addActionListener(evt -> createNewOneButtonActionPerformed());

        useStoredButton.setText(localization.getText("welcome.token_saved.use_saved"));
        useStoredButton.addActionListener(evt -> useStoredButtonActionPerformed());

        GroupLayout loadOrCreateDialogCardLayout = new GroupLayout(loadOrCreateDialogCard);
        loadOrCreateDialogCard.setLayout(loadOrCreateDialogCardLayout);
        loadOrCreateDialogCardLayout.setHorizontalGroup(
                loadOrCreateDialogCardLayout.createParallelGroup(LEADING)
                        .addGroup(TRAILING, loadOrCreateDialogCardLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(loadOrCreateDialogCardLayout.createParallelGroup(TRAILING)
                                        .addComponent(loadOrCreateLabel, PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                        .addComponent(createNewOneButton, LEADING, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(useStoredButton, LEADING, DEFAULT_SIZE, 611, Short.MAX_VALUE))
                                .addContainerGap())
        );
        loadOrCreateDialogCardLayout.setVerticalGroup(
                loadOrCreateDialogCardLayout.createParallelGroup(LEADING)
                        .addGroup(loadOrCreateDialogCardLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(loadOrCreateLabel, DEFAULT_SIZE, 419, Short.MAX_VALUE)
                                .addPreferredGap(RELATED)
                                .addComponent(useStoredButton)
                                .addPreferredGap(RELATED)
                                .addComponent(createNewOneButton)
                                .addContainerGap())
        );

        containerPanel.add(loadOrCreateDialogCard, "loadOrCreateDialogue");

        chatsTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{
                        {null, null},
                        {null, null}
                },
                new String[]{
                        "", ""
                }
        ) {
            Class[] types = new Class[]{
                    java.lang.Boolean.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        });
        jScrollPane1.setViewportView(chatsTable);

        jLabel1.setText(localization.getText("download_window.text"));

        jButton1.setText(localization.getText("download_window.download"));
        jButton1.addActionListener(evt -> jButton1ActionPerformed());

        GroupLayout listCardLayout = new GroupLayout(listCard);
        listCard.setLayout(listCardLayout);
        listCardLayout.setHorizontalGroup(
                listCardLayout.createParallelGroup(LEADING)
                        .addComponent(jScrollPane1, DEFAULT_SIZE, 635, Short.MAX_VALUE)
                        .addComponent(jLabel1, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(TRAILING, listCardLayout.createSequentialGroup()
                                .addContainerGap()
                                .addPreferredGap(RELATED, DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton1, PREFERRED_SIZE, 160, PREFERRED_SIZE))
        );
        listCardLayout.setVerticalGroup(
                listCardLayout.createParallelGroup(LEADING)
                        .addGroup(listCardLayout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(RELATED)
                                .addComponent(jScrollPane1, DEFAULT_SIZE, 454, Short.MAX_VALUE)
                                .addPreferredGap(RELATED)
                                .addGroup(listCardLayout.createParallelGroup(BASELINE)
                                        .addComponent(jButton1)
                                )));

        containerPanel.add(listCard, "listCard");

        jLabel3.setHorizontalAlignment(CENTER);
        jLabel3.setText(localization.getText("finished.text"));

        jButton3.setText(localization.getText("finished.open_folder"));
        jButton3.addActionListener(evt -> jButton3ActionPerformed());

        jButton4.setText(localization.getText("finished.back"));
        jButton4.addActionListener(evt -> jButton4ActionPerformed());

        GroupLayout downloadingFinishedCardLayout = new GroupLayout(downloadingFinishedCard);
        downloadingFinishedCard.setLayout(downloadingFinishedCardLayout);
        downloadingFinishedCardLayout.setHorizontalGroup(
                downloadingFinishedCardLayout.createParallelGroup(LEADING)
                        .addGroup(downloadingFinishedCardLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(downloadingFinishedCardLayout.createParallelGroup(LEADING)
                                        .addComponent(jLabel3, TRAILING, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(TRAILING, downloadingFinishedCardLayout.createSequentialGroup()
                                                .addComponent(jButton4)
                                                .addPreferredGap(RELATED, 325, Short.MAX_VALUE)
                                                .addComponent(jButton3)))
                                .addContainerGap())
        );
        downloadingFinishedCardLayout.setVerticalGroup(
                downloadingFinishedCardLayout.createParallelGroup(LEADING)
                        .addGroup(downloadingFinishedCardLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel3, PREFERRED_SIZE, 237, PREFERRED_SIZE)
                                .addPreferredGap(RELATED, 221, Short.MAX_VALUE)
                                .addGroup(downloadingFinishedCardLayout.createParallelGroup(BASELINE)
                                        .addComponent(jButton3)
                                        .addComponent(jButton4))
                                .addContainerGap())
        );

        containerPanel.add(downloadingFinishedCard, "downloadingFinished");

        jPanel1.setLayout(new java.awt.BorderLayout());

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(0, 318, Short.MAX_VALUE)
                                .addComponent(jPanel1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                .addGap(0, 317, Short.MAX_VALUE))
                        .addGroup(layout.createParallelGroup(LEADING)
                                .addComponent(containerPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(0, 254, Short.MAX_VALUE)
                                .addComponent(jPanel1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                .addGap(0, 255, Short.MAX_VALUE))
                        .addGroup(layout.createParallelGroup(LEADING)
                                .addComponent(containerPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }

    private void openLinkButtonActionPerformed() {
        logger.info("Opening key link in browser");
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(vkService.getAuthUri());
            } else {
                throw new RuntimeException("Desktop is not supported");
            }
        } catch (IOException | RuntimeException e) {
            logger.info("Failed to open link in browser", e);
            setStatusGui(localization.getText("unknown_error", e.getMessage()));
        }
    }

    private void startAuthNextButtonActionPerformed() {
        SwingWorker worker = new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground() {
                logger.info("Authentication started");
                try {
                    setStatusGui(localization.getText("authentication.started"));
                    vkService.requestAuthToken(userCodeField.getText().trim(), saveToDiskBox.isSelected());
                    logger.info("Authentication successful. Loading dialogues list...");
                    setStatusGui(localization.getText("authentication.success"));
                    chats = vkService.loadDialogues();
                    logger.info("Dialogues loaded");
                    showChatsSelectCard();
                } catch (Exception e) {
                    logger.error("Unexpected error on loading dialogues", e);
                    try {
                        setFailedGui(localization.getText("unknown_error", e.getMessage()),
                                localization.getText("authentication.try_again"), "startAuth");
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                return null;
            }
        };
        worker.execute();
    }

    private void failedButtonActionPerformed() {
        switchToCard(cardToGo);
    }

    private void createNewOneButtonActionPerformed() {
        switchToCard("startAuth");
    }

    private void useStoredButtonActionPerformed() {
        SwingWorker worker = new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground() {
                try {
                    chats = vkService.loadDialogues();
                    showChatsSelectCard();
                } catch (ApiException | ClientException | InterruptedException e) {
                    setFailedGui(localization.getText("authentication.error", e.getMessage()),
                            localization.getText("authentication.try_again"), "startAuth");
                }
                return null;
            }
        };
        setStatusGui(localization.getText("authentication.success"));
        worker.execute();
    }

    private void jButton1ActionPerformed() {
        DownloadingStatusListener listener = this;
        SwingWorker worker = new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground() {
                if (chats.stream().noneMatch(ChatGuiDto::isDownload)) {
                    return null;
                }
                try {
                    setStatusGui(localization.getText("downloading_started"));
                    vkService.downloadMessages(chats, listener);
                    switchToCard("downloadingFinished");
                } catch (Exception e) {
                    logger.error("Failed to download messages", e);
                    setFailedGui(localization.getText("downloading_failed", e.getMessage()),
                            localization.getText("finished.back"), "listCard");
                }
                return null;
            }
        };
        worker.execute();
    }

    private void jButton4ActionPerformed() {
        switchToCard("listCard");
    }

    private void jButton3ActionPerformed() {
        INSTANCE.openDownloadsFolder();
    }

    private JTable chatsTable;
    private JPanel containerPanel;
    private JButton failedButton;
    private JLabel failedLabel;
    private JCheckBox saveToDiskBox;
    private JLabel statusLabel;
    private JTextField userCodeField;
}
