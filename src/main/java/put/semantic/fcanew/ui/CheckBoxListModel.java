/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class CheckBoxListModel<T> implements ListModel<T> {

    private List<? extends T> items;
    private boolean[] checked;
    private List<ListDataListener> listeners = new ArrayList<>();

    public CheckBoxListModel(List<? extends T> items) {
        this.items = items;
        this.checked = new boolean[this.items.size()];
    }

    public CheckBoxListModel(List<? extends T> items, boolean[] checked) {
        this.items = items;
        this.checked = Arrays.copyOf(checked, items.size());
    }

    @Override
    public int getSize() {
        return items.size();
    }

    @Override
    public T getElementAt(int index) {
        return items.get(index);
    }

    public List<? extends T> getChecked() {
        List<T> result = new ArrayList<>();
        for (int i = 0; i < checked.length; ++i) {
            if (checked[i]) {
                result.add(items.get(i));
            }
        }
        return result;
    }

    public boolean[] getCheckedAsArray() {
        return Arrays.copyOf(checked, checked.length);
    }

    public boolean isChecked(int index) {
        return checked[index];
    }

    public void setChecked(int index, boolean value) {
        if (checked[index] != value) {
            checked[index] = value;
            fireContentsChanged(index, index);
        }
    }

    protected void fireContentsChanged(int begin, int end) {
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, begin, end);
        for (ListDataListener l : listeners) {
            l.contentsChanged(e);
        }
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        if (l != null) {
            listeners.add(l);
        }
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }

}
