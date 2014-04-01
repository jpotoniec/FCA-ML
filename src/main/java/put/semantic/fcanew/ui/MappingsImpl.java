/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */






package put.semantic.fcanew.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import put.semantic.fcanew.Attribute;
import put.semantic.fcanew.mappings.Mappings;

class EntryImpl implements Mappings.Entry {

    private Attribute attribute;
    private String pattern = "";

    public EntryImpl(Attribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public Attribute getAttribute() {
        return attribute;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        pattern = pattern.trim();
        if (pattern.endsWith(".")) {
            pattern = pattern.substring(0, pattern.length() - 1);
        }
        this.pattern = pattern;
    }

}

public class MappingsImpl implements Mappings {

    private final AbstractTableModel tableModel = new AbstractTableModel() {

        @Override
        public int getRowCount() {
            return entries.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Entry e = entries.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return e.getAttribute();
                case 1:
                    return e.getPattern();
            }
            throw new IllegalArgumentException();
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Attribute";
                case 1:
                    return "Pattern";
            }
            return super.getColumnName(column); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex != 0;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            assert isCellEditable(rowIndex, columnIndex);
            EntryImpl e = entries.get(rowIndex);
            e.setPattern(aValue.toString());
        }
    };

    public TableModel getTableModel() {
        return tableModel;
    }

    public void updateAttributes(List<? extends Attribute> attributes) {
        this.entries.clear();
        for (Attribute a : attributes) {
            this.entries.add(new EntryImpl(a));
        }
        tableModel.fireTableDataChanged();
    }

    private final List<EntryImpl> entries = new ArrayList<>();
    private String prefixes = "";
    private String endpoint = "";

    public void setPrefixes(String newValue) {
        this.prefixes = newValue;
    }

    @Override
    public String getPrefixes() {
        return prefixes;
    }

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void load(File f) {
        try {
            XMLConfiguration c = new XMLConfiguration();
            c.setDelimiterParsingDisabled(true);
            c.load(f);
            endpoint = c.getString("mappings.endpoint");
            prefixes = c.getString("mappings.prefixes");
            for (HierarchicalConfiguration m : c.configurationsAt("mappings.mapping")) {
                String attr = m.getString("attribute");
                for (EntryImpl e : entries) {
                    if (attr.equals(e.getAttribute().toString())) {
                        e.setPattern(m.getString("pattern"));
                    }
                }
            }
            tableModel.fireTableDataChanged();
        } catch (ConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void save(File f) {
        try {
            XMLConfiguration c = new XMLConfiguration();
            c.setDelimiterParsingDisabled(true);
            c.addProperty("mappings.endpoint", endpoint);
            c.addProperty("mappings.prefixes", prefixes);
            for (Entry e : entries) {
                if (!e.getPattern().isEmpty()) {
                    c.addProperty("mappings.mapping(-1).attribute", e.getAttribute().toString());
                    c.addProperty("mappings.mapping.pattern", e.getPattern());
                }
            }
            c.save(f);
        } catch (ConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<? extends Entry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    @Override
    public String getPattern(Attribute a) {
        for (Entry e : entries) {
            if (e.getAttribute().equals(a)) {
                return e.getPattern();
            }
        }
        return null;
    }

}
