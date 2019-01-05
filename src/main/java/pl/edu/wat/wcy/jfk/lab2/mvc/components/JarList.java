package pl.edu.wat.wcy.jfk.lab2.mvc.components;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.List;

import static java.awt.BorderLayout.CENTER;

public class JarList extends JPanel {

    private JList<String> jList = new JList<>();

    public JarList(String title) {
        setLayout(new BorderLayout());
        add(new JScrollPane(jList), CENTER);
        setBorder(new TitledBorder(title));
    }

    public void setjList(List<String> methods) {
        DefaultListModel<String> model = new DefaultListModel<>();
        methods.forEach(model::addElement);
        jList.setModel(model);
    }

    public String getSelectedMethod() {
        return jList.getSelectedValue();
    }

    public void addSelectionListener(ListSelectionListener listener) {
        jList.getSelectionModel().addListSelectionListener(listener);
    }

    public void clear() {
        jList.setModel(new DefaultListModel<>());
    }

    public JList<String> getjList() {
        return jList;
    }

    public void setClassesList(JList<String> classesList) {
        this.jList = classesList;
    }
}
