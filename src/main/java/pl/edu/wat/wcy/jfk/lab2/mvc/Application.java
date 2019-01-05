package pl.edu.wat.wcy.jfk.lab2.mvc;

import lombok.Getter;
import pl.edu.wat.wcy.jfk.lab2.mvc.components.JarList;
import pl.edu.wat.wcy.jfk.lab2.util.Texts;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyEvent;

import static javax.swing.GroupLayout.DEFAULT_SIZE;

@Getter
public class Application extends JFrame {

    private JPanel panel;

    private JTextArea codeTextArea;

    private JarList jarClassesList;
    private JarList methodList;
    private JarList constructorList;
    private JarList fieldList;

    private JLabel oldJarNameLabel;
    private JLabel newJarInfoLabel;
    private JTextField newJarNameTextField;

    private JButton exportJarButton;
    private JButton addClassButton;
    private JButton addInterfaceButton;
    private JButton deleteClassButton;
    private JButton addMethodButton;
    private JButton deleteMethodButton;
    private JButton overwriteMethodBodyButton;
    private JButton insertBeforeMethodBodyButton;
    private JButton insertAfterMethodBodyButton;
    private JButton addFieldButton;
    private JButton deleteFieldButton;
    private JButton addConstructorButton;
    private JButton deleteConstructorButton;
    private JButton overwriteConstructorBodyButton;

    private JMenuItem loadJarFileMenuItem;


    public Application() throws HeadlessException {
        initUI();
    }

    private void initUI() {
        panel = (JPanel) getContentPane();

        codeTextArea = new JTextArea("Type your code here");
        codeTextArea.setBorder(new TitledBorder("Your code"));
        codeTextArea.setFont(new Font("Courier New", Font.PLAIN, 14));
        codeTextArea.setTabSize(2);

        jarClassesList = new JarList("Classes in jar file:");
        methodList = new JarList("Class Methods:");
        constructorList = new JarList("Class Constructors:");
        fieldList = new JarList("Class Fields:");

        oldJarNameLabel = new JLabel("Jar name: ");
        newJarInfoLabel = new JLabel("New jar name:");
        newJarNameTextField = new JTextField();
        exportJarButton = new JButton("Save updated Jar");
        addClassButton = new JButton("New Class");
        addInterfaceButton = new JButton("New interface");
        deleteClassButton = new JButton("Delete Class");
        addMethodButton = new JButton("Add Method");
        deleteMethodButton = new JButton("Delete Method");
        overwriteMethodBodyButton = new JButton("Overwrite method");
        insertBeforeMethodBodyButton = new JButton("Insert before");
        insertAfterMethodBodyButton = new JButton("Insert after");
        addConstructorButton = new JButton("Add constructor");
        deleteConstructorButton = new JButton("Delete constructor");
        overwriteConstructorBodyButton = new JButton("Overwrite constructor");
        addFieldButton = new JButton("Add field");
        deleteFieldButton = new JButton("Delete field");
        JScrollPane jScrollPane = new JScrollPane();
        jScrollPane.getViewport().add(codeTextArea);

        loadJarFileMenuItem = createMenuBar();

        createLayout(addInterfaceButton,
                oldJarNameLabel, newJarInfoLabel, newJarNameTextField,
                jarClassesList, addClassButton, deleteClassButton,
                methodList, addMethodButton, deleteMethodButton, overwriteMethodBodyButton, insertBeforeMethodBodyButton, insertAfterMethodBodyButton,
                constructorList, addConstructorButton, deleteConstructorButton, overwriteConstructorBodyButton,
                fieldList, addFieldButton, deleteFieldButton,
                jScrollPane, exportJarButton);

        setTitle(Texts.TITLE);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private JMenuItem createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu jMenu = new JMenu("File");
        jMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem loadJarFileMenuItem = new JMenuItem("Load Jar File");
        loadJarFileMenuItem.setMnemonic(KeyEvent.VK_L);

        jMenu.add(loadJarFileMenuItem);

        menuBar.add(jMenu);
        setJMenuBar(menuBar);

        return loadJarFileMenuItem;
    }

    private void createLayout(JComponent... arg) {
        Container contentPane = getContentPane();
        GroupLayout gl = new GroupLayout(contentPane);
        contentPane.setLayout(gl);

        gl.setAutoCreateContainerGaps(true);

        gl.setHorizontalGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER, true)
                .addGroup(gl.createSequentialGroup()
                        .addComponent(arg[1])
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(arg[2])
                        .addComponent(arg[3], 150, 150, 150)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(arg[21]))
                .addGroup(gl.createSequentialGroup()
                        .addGroup(gl.createParallelGroup()
                                .addComponent(arg[4])
                                .addGroup(gl.createSequentialGroup()
                                        .addComponent(arg[5])
                                        .addComponent(arg[0])
                                        .addComponent(arg[6])))
                        .addGroup(gl.createSequentialGroup()
                                .addGroup(gl.createParallelGroup()
                                        .addComponent(arg[13])
                                        .addGroup(gl.createSequentialGroup()
                                                .addComponent(arg[14])
                                                .addComponent(arg[15])
                                                .addComponent(arg[16])))
                                .addGroup(gl.createParallelGroup()
                                        .addComponent(arg[17])
                                        .addGroup(gl.createSequentialGroup()
                                                .addComponent(arg[18])
                                                .addComponent(arg[19])))))
                .addGroup(gl.createSequentialGroup()
                        .addGroup(gl.createParallelGroup()
                                .addComponent(arg[7])
                                .addGroup(gl.createSequentialGroup()
                                        .addComponent(arg[8])
                                        .addComponent(arg[9])
                                        .addComponent(arg[10])
                                        .addComponent(arg[11])
                                        .addComponent(arg[12])))
                        .addComponent(arg[20]))
        );
        gl.setVerticalGroup(gl.createSequentialGroup()
                .addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(arg[1])
                        .addComponent(arg[2])
                        .addComponent(arg[3])
                        .addComponent(arg[21]))
                .addGroup(gl.createParallelGroup()
                        .addGroup(gl.createSequentialGroup()
                                .addComponent(arg[4])
                                .addGroup(gl.createParallelGroup()
                                        .addComponent(arg[5])
                                        .addComponent(arg[0])
                                        .addComponent(arg[6])))
                        .addGroup(gl.createParallelGroup()
                                .addGroup(gl.createSequentialGroup()
                                        .addComponent(arg[13])
                                        .addGroup(gl.createParallelGroup()
                                                .addComponent(arg[14])
                                                .addComponent(arg[15])
                                                .addComponent(arg[16])))
                                .addGroup(gl.createSequentialGroup()
                                        .addComponent(arg[17])
                                        .addGroup(gl.createParallelGroup()
                                                .addComponent(arg[18])
                                                .addComponent(arg[19])))))
                .addGroup(gl.createParallelGroup()
                        .addGroup(gl.createSequentialGroup()
                                .addComponent(arg[7])
                                .addGroup(gl.createParallelGroup()
                                        .addComponent(arg[8])
                                        .addComponent(arg[9])
                                        .addComponent(arg[10])
                                        .addComponent(arg[11])
                                        .addComponent(arg[12])))
                        .addGroup(gl.createSequentialGroup()
                                .addComponent(arg[20])
                                .addGap(27)))
        );
        pack();
    }
}
