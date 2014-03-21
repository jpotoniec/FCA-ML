/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author smaug
 */
public class FilesListModel implements ListModel<File> {

    private final List<File> data = new ArrayList<>();
    private final List<ListDataListener> listeners = new ArrayList<>();

    private void loadPreferences() {
        String filesString = Preferences.userNodeForPackage(MainWindow.class).get("files", "");
        if (filesString == null) {
            return;
        }
        String[] paths = StringUtils.split(filesString, File.pathSeparatorChar);
        for (String path : paths) {
            File f = new File(path);
            if (f.isFile() && f.canRead()) {
                add(f);
            }
        }
    }

    private void savePreferences() {
        List<String> paths = new ArrayList<>();
        for (File f : data) {
            paths.add(f.getAbsolutePath());
        }
        String files = StringUtils.join(paths, File.pathSeparatorChar);
        Preferences.userNodeForPackage(MainWindow.class).put("files", files);
    }

    public FilesListModel() {
        loadPreferences();
        addListDataListener(new ListDataListener() {

            @Override
            public void intervalAdded(ListDataEvent e) {
                savePreferences();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                savePreferences();
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                savePreferences();
            }
        });
    }

    public void add(File f) {
        if (f == null) {
            return;
        }
        data.add(f);
        int i = data.size() - 1;
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, i, i);
        for (ListDataListener l : listeners) {
            l.intervalAdded(e);
        }
    }

    public void add(File[] files) {
        if (files == null || files.length == 0) {
            return;
        }
        int begin = data.size();
        data.addAll(Arrays.asList(files));
        int end = data.size() - 1;
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, begin, end);
        for (ListDataListener l : listeners) {
            l.intervalAdded(e);
        }
    }

    public void remove(int index) {
        data.remove(index);
        ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index);
        for (ListDataListener l : listeners) {
            l.intervalAdded(e);
        }
    }

    @Override
    public int getSize() {
        return data.size();
    }

    @Override
    public File getElementAt(int index) {
        return data.get(index);
    }

    public List<File> getFiles() {
        return Collections.unmodifiableList(data);
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
