/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JList;

/**
 *
 * @author smaug
 */
public class CheckBoxList<T> extends JList<T> {

    public CheckBoxList() {
        setCellRenderer(new CheckBoxListCellRenderer());

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int index = locationToIndex(e.getPoint());

                if (index != -1) {
                    CheckBoxListModel model = (CheckBoxListModel) getModel();
                    if (model == null) {
                        return;
                    }
                    model.setChecked(index, !model.isChecked(index));
//                    repaint();
                }
            }
        }
        );
    }

    private static class CheckBoxListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, final int index, boolean isSelected, boolean cellHasFocus) {
            if (list.getModel() == null || !(list.getModel() instanceof CheckBoxListModel)) {
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
            final CheckBoxListModel model = (CheckBoxListModel) list.getModel();
            Action a = new AbstractAction(value.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    System.err.println(e);
                    JCheckBox src = (JCheckBox) e.getSource();
                    model.setChecked(index, src.isSelected());
                }
            };
            JCheckBox result = new JCheckBox(a);
            result.setSelected(model.isChecked(index));            
            result.setOpaque(isSelected);
            result.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            result.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            return result;
        }

    }
}
