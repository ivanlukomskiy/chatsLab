package com.ivanlukomskiy.chatsLab.gui;

import com.google.gson.JsonSyntaxException;
import com.ivanlukomskiy.chatsLab.model.ChatGuiDto;
import com.ivanlukomskiy.chatsLab.model.ChatTableElement;
import com.ivanlukomskiy.chatsLab.model.JsonNodeTableElement;
import com.ivanlukomskiy.chatsLab.service.TelegramJsonParser;
import com.ivanlukomskiy.chatsLab.service.TelegramParser;
import com.ivanlukomskiy.chatsLab.service.VkService;
import com.ivanlukomskiy.chatsLab.util.Localization;
import com.ivanlukomskiy.chatsLab.util.LocalizationHolder;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonNode;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.ivanlukomskiy.chatsLab.service.IOService.INSTANCE;
import static com.ivanlukomskiy.chatsLab.util.LocalizationHolder.LOCALIZATION_RESOURCE;
import static com.ivanlukomskiy.chatsLab.util.LocalizationHolder.localization;
import static java.awt.EventQueue.invokeLater;
import static java.util.stream.Collectors.toList;
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
 *
 * @author ivan_l
 */
public class MainFrame extends javax.swing.JFrame implements DownloadingStatusListener {
    private static final Logger logger = LogManager.getLogger(MainFrame.class);
    private static final TelegramParser TELEGRAM_PARSER = new TelegramJsonParser();

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
                switchToCard("telegramSelect");
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

    private JPanel buildTelegramSelectPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel label = new JLabel(localization.getText("telegram.fileChoose"));
        label.setBorder(new EmptyBorder(10, 10, 10, 10));


        chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Telegram JSON export", "json"));
        chooser.setControlButtonsAreShown(false);
        chooser.setMultiSelectionEnabled(false);
        chooser.addActionListener(evt -> telegramUploadClicked());

        panel.add(label, BorderLayout.NORTH);
        panel.add(chooser, BorderLayout.CENTER);

        JButton approve = new JButton(localization.getText("telegram.choose"));
        approve.addActionListener(evt -> telegramUploadClicked());

        JButton cancel = new JButton(localization.getText("telegram.skip"));
        cancel.addActionListener(evt -> switchToVk());

        JPanel subPanel = new JPanel();
        subPanel.add(approve);
        subPanel.add(cancel);

        panel.add(subPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void switchToVk() {
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

    private void telegramUploadClicked() {
        File file = chooser.getSelectedFile();
        if (file == null || !file.exists()) {
            return;
        }
        System.out.println("Valid file is " + file.getAbsolutePath());

        SwingWorker worker = new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground() {
                try {
                    setStatusGui(localization.getText("telegram.downloading"));
                    List<JsonNodeTableElement> chats = TELEGRAM_PARSER.getChats(file.getAbsolutePath()).stream()
                            .map(JsonNodeTableElement::new)
                            .collect(toList());
                    telegramChatsTable.setModel(new ChatsListTableModel(chats));
                    telegramChatsTable.getColumnModel().getColumn(0).setMaxWidth(50);
                    switchToCard("telegramChatsSelect");
                } catch (IOException e) {
                    e.printStackTrace();
                    switchToCard("telegramChatsSelect");
                }
                return null;
            }
        };
        worker.execute();
    }

    private JPanel initChatsTable(JTable table, ActionListener l, String buttonText, String labelText) {
        JScrollPane jScrollPane1 = new JScrollPane();
        JLabel jLabel1 = new JLabel();
        JButton jButton1 = new JButton();
        JPanel listCard = new JPanel();

        table.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(table);

        jLabel1.setText(labelText);

        jButton1.setText(buttonText);
        jButton1.addActionListener(l);

        GroupLayout listCardLayout = new GroupLayout(listCard);
        listCard.setLayout(listCardLayout);
        listCardLayout.setHorizontalGroup(
                listCardLayout.createParallelGroup(LEADING)
                        .addComponent(jScrollPane1, DEFAULT_SIZE, 635, Short.MAX_VALUE)
                        .addComponent(jLabel1, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(TRAILING, listCardLayout.createSequentialGroup()
                                .addContainerGap()
                                .addPreferredGap(RELATED, DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton1, PREFERRED_SIZE, 160, PREFERRED_SIZE)
                        )
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

        return listCard;
    }

    private void telegramChatsSelected() {
        switchToVk();
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

        containerPanel.add(buildTelegramSelectPanel(), "telegramSelect");

        telegramChatsTable = new JTable();
        containerPanel.add(initChatsTable(telegramChatsTable, evt -> telegramChatsSelected(),
                localization.getText("telegram.choose"),
                localization.getText("telegram.titleChoose")),
                "telegramChatsSelect");

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

        chatsTable = new JTable();
        containerPanel.add(initChatsTable(chatsTable, evt -> jButton1ActionPerformed(),
                localization.getText("download_window.download"),
                localization.getText("download_window.text")), "listCard");

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
                } catch (Exception e) {
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
                    vkService.downloadMessages(chats, getTelegramChats(), listener);
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

    private List<JsonNode> getTelegramChats() {
        try {
            ChatsListTableModel model = (ChatsListTableModel) telegramChatsTable.getModel();
            List<? extends ChatTableElement> data = model.getData();
            return data.stream().map(elem -> (JsonNodeTableElement) elem)
                    .filter(JsonNodeTableElement::isDownload)
                    .map(JsonNodeTableElement::getNode)
                    .collect(toList());
        } catch (Exception e) {
            logger.error("Failed to get telegram chats", e);
            return Collections.emptyList();
        }
    }


    private void jButton4ActionPerformed() {
        switchToCard("listCard");
    }

    private void jButton3ActionPerformed() {
        INSTANCE.openDownloadsFolder();
    }

    private JTable chatsTable;
    private JTable telegramChatsTable;
    private JPanel containerPanel;
    private JButton failedButton;
    private JLabel failedLabel;
    private JCheckBox saveToDiskBox;
    private JLabel statusLabel;
    private JTextField userCodeField;
    private JFileChooser chooser;
}
