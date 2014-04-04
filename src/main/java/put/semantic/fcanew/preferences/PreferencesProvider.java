/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import org.apache.commons.lang.StringUtils;
import put.semantic.fcanew.ui.MainWindow;

/**
 *
 * @author smaug
 */
public class PreferencesProvider {

    private PreferencesProvider() {

    }

    private static final PreferencesProvider INSTANCE = new PreferencesProvider();

    public static PreferencesProvider getInstance() {
        return INSTANCE;
    }

    protected Preferences getNode() {
        return Preferences.userNodeForPackage(MainWindow.class);
    }

    public List<File> getFiles() {
        String filesString = getNode().get("files", "");
        if (filesString == null) {
            return Collections.EMPTY_LIST;
        }
        List<File> result = new ArrayList<>();
        String[] paths = StringUtils.split(filesString, File.pathSeparatorChar);
        for (String path : paths) {
            File f = new File(path);
            if (f.isFile() && f.canRead()) {
                result.add(f);
            }
        }
        return result;
    }

    public void setFiles(Iterable<File> data) {
        List<String> paths = new ArrayList<>();
        for (File f : data) {
            paths.add(f.getAbsolutePath());
        }
        String files = StringUtils.join(paths, File.pathSeparatorChar);
        getNode().put("files", files);
    }

    public boolean[] getCalculators() {
        String s = getNode().get("calculators", "");
        boolean[] result = new boolean[s.length()];
        for (int i = 0; i < result.length; ++i) {
            result[i] = (s.charAt(i) != '0');
        }
        assert result != null;
        return result;
    }

    public void setCalculators(boolean[] checked) {
        String s = "";
        for (int i = 0; i < checked.length; ++i) {
            s += checked[i] ? "1" : "0";
        }
        getNode().put("calculators", s);
    }

    public void setMappingsFile(File f) {
        getNode().put("mappings", f.getAbsolutePath());
    }

    public File getMappingsFile() {
        return new File(getNode().get("mappings", ""));
    }

    public int getClassifier() {
        return getNode().getInt("classifier", 0);
    }

    public void setClassifier(int cl) {
        getNode().putInt("classifier", cl);
    }
}
