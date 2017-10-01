package com.ivanlukomskiy.chatsLab.gui;

import com.ivanlukomskiy.chatsLab.model.ChatGuiDto;
import lombok.AllArgsConstructor;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author ivan_l
 */
@AllArgsConstructor
public class ChatsListTableModel extends AbstractTableModel {

    private final static String[] columnNames = {"", "Name"};

    private final List<ChatGuiDto> data;

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        ChatGuiDto chat = data.get(row);

        Object value = null;
        switch (col) {
            case 0:
                value = chat.isDownload();
                break;
            case 1:
                value = chat.getName();
                break;
        }

        return value;
    }

    /*
     * JTable uses this method to determine the default renderer/ editor for
     * each cell. If we didn't implement this method, then the last column
     * would contain text ("true"/"false"), rather than a check box.
     */
    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's editable.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        return col == 0;
    }

    /*
     * Don't need to implement this method unless your table's data can
     * change.
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        ChatGuiDto chat = data.get(row);
        chat.setDownload((Boolean) value);
    }
}
