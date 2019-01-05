package pl.edu.wat.wcy.jfk.lab2.mvc;

import javassist.*;
import pl.edu.wat.wcy.jfk.lab2.UpdatedClass;
import pl.edu.wat.wcy.jfk.lab2.jar.JarHandler;
import pl.edu.wat.wcy.jfk.lab2.util.Texts;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static javax.swing.JFileChooser.APPROVE_OPTION;

public class Controller {

    private Model model;
    private Application view;

    private static final Logger LOGGER = Logger.getLogger(Controller.class.getName());


    public Controller(Model model, Application view) {
        this.model = model;
        this.view = view;

        this.view.getJarClassesList().addSelectionListener(new SelectClassListener());
        this.view.getMethodList().addSelectionListener(new SelectMethodListener());
        this.view.getConstructorList().addSelectionListener(new SelectConstructorListener());
        this.view.getFieldList().addSelectionListener(new SelectFieldListener());

        this.view.getLoadJarFileMenuItem().addActionListener(new LoadFileListener());
        this.view.getNewJarNameTextField().getDocument().addDocumentListener(new NewNameTypedListener());
        this.view.getExportJarButton().addActionListener(new ExportJarListener());

        this.view.getAddClassButton().addActionListener(new AddClassListener());
        this.view.getAddInterfaceButton().addActionListener(new AddInterfaceListener());
        this.view.getDeleteClassButton().addActionListener(new DeleteClassListener());

        this.view.getAddMethodButton().addActionListener(new AddMethodListener());
        this.view.getDeleteMethodButton().addActionListener(new DeleteMethodListener());
        this.view.getOverwriteMethodBodyButton().addActionListener(new OverwriteMethodBodyListener());
        this.view.getInsertBeforeMethodBodyButton().addActionListener(new InsertBeforeMethodListener());
        this.view.getInsertAfterMethodBodyButton().addActionListener(new InsertAfterMethodListener());

        this.view.getAddConstructorButton().addActionListener(new AddConstructorListener());
        this.view.getDeleteConstructorButton().addActionListener(new DeleteConstructorListener());
        this.view.getOverwriteConstructorBodyButton().addActionListener(new OverwriteConstructorBodyListener());

        this.view.getAddFieldButton().addActionListener(new AddFieldListener());
        this.view.getDeleteFieldButton().addActionListener(new DeleteFieldListener());
    }
    //////////////////////////////////////////

    private void updateUI() {
        updateClassesList();
        updateFieldList();
        updateConstructorList();
        updateMethodsList();

        view.getOldJarNameLabel().setText(Texts.OLD_JAR_LABEL + model.getOldJarName());
    }

    private void updateClassesList() {
        List<String> classNames = model.getListOfClasses().stream().map(CtClass::getName).collect(Collectors.toList());
        view.getJarClassesList().setjList(classNames);
    }

    private void updateFieldList() {
        List<CtClass> classes = model.getListOfClasses();
        Optional<CtClass> optionalCtClass = classes.stream().filter(ctClass -> ctClass.getName().equals(model.getCurrentClass())).findFirst();
        if (!optionalCtClass.isPresent()) {
            view.getFieldList().setjList(new ArrayList<>());
            return;
        }
        CtClass ctClass = optionalCtClass.get();
        List<CtField> ctFields = new ArrayList<>(Arrays.asList(ctClass.getDeclaredFields()));
        view.getFieldList().setjList(ctFields.stream().map(CtField::getName).collect(Collectors.toList()));
    }

    private void updateConstructorList() {
        List<CtClass> classes = model.getListOfClasses();
        Optional<CtClass> optionalCtClass = classes.stream().filter(ctClass -> ctClass.getName().equals(model.getCurrentClass())).findFirst();
        if (!optionalCtClass.isPresent()) {
            view.getConstructorList().setjList(new ArrayList<>());
            return;
        }
        CtClass ctClass = optionalCtClass.get();
        List<CtConstructor> ctConstructors = new ArrayList<>(Arrays.asList(ctClass.getDeclaredConstructors()));
        view.getConstructorList().setjList(ctConstructors.stream().map(CtConstructor::getLongName).collect(Collectors.toList()));
    }

    private void updateMethodsList() {
        List<CtClass> classes = model.getListOfClasses();
        Optional<CtClass> optionalCtClass = classes.stream().filter(ctClass -> ctClass.getName().equals(model.getCurrentClass())).findFirst();
        if (!optionalCtClass.isPresent()) {
            view.getMethodList().setjList(new ArrayList<>());
            return;
        }
        CtClass ctClass = optionalCtClass.get();
        List<CtMethod> ctMethods = new ArrayList<>(Arrays.asList(ctClass.getDeclaredMethods()));
        view.getMethodList().setjList(ctMethods.stream().map(CtMethod::getLongName).collect(Collectors.toList()));
    }

    /////////////////////////////

    private class SelectClassListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting())
                return;
            String selectedClassName = view.getJarClassesList().getjList().getSelectedValue();
            model.setCurrentClass(selectedClassName);
            if (selectedClassName == null)
                return;
            disableButtonIfCannotDeleteClass(selectedClassName);
            updateMethodsList();
            updateConstructorList();
            updateFieldList();
        }

        private void disableButtonIfCannotDeleteClass(String selectedClassName) {
            if (model.getAddedClasses().stream().map(CtClass::getName).anyMatch(selectedClassName::equals))
                view.getDeleteClassButton().setEnabled(true);
            else
                view.getDeleteClassButton().setEnabled(false);
        }
    }

    private class SelectMethodListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting())
                return;
            String selectedMethodName = view.getMethodList().getjList().getSelectedValue();
            model.setCurrentMethod(selectedMethodName);
        }
    }

    private class SelectConstructorListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting())
                return;
            String selectedConstructorName = view.getConstructorList().getjList().getSelectedValue();
            model.setCurrentConstructor(selectedConstructorName);
        }
    }

    private class SelectFieldListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting())
                return;
            String selectedFieldName = view.getFieldList().getjList().getSelectedValue();
            model.setCurrentField(selectedFieldName);
        }
    }

    private class LoadFileListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser jFileChooser = new JFileChooser(model.getJarPath());
            FileNameExtensionFilter jar = new FileNameExtensionFilter(Texts.JAR_FILE_DESC, Texts.JAR);
            jFileChooser.addChoosableFileFilter(jar);

            int ret = jFileChooser.showDialog(view.getPanel(), Texts.OPEN_FILE);

            if (ret == APPROVE_OPTION) {
                File selectedFile = jFileChooser.getSelectedFile();
                if (!selectedFile.getName().endsWith(".jar")) {
                    JOptionPane.showMessageDialog(jFileChooser, Texts.NOT_JAR_ERR);
                    return;
                }
                try {
                    exploreJarFile(selectedFile);
                } catch (NotFoundException e1) {
                    LOGGER.log(Level.WARNING, "Error while loading classes from jar", e1);
                }
            }
        }

        private void exploreJarFile(File selectedFile) throws NotFoundException {
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(selectedFile);
            } catch (IOException e1) {
                LOGGER.log(Level.WARNING, "IO exception in jar file", e1);
            }
            if (jarFile == null)
                return;

            model.setOldJarName(getNameFromJarFile(jarFile.getName()));
            model.setJarPath(getPathFromJarFile(jarFile.getName()));
            model.setNewJarName(getDefaultNewName(jarFile.getName()));

            LOGGER.info("Loaded jar: " + model.getOldJarName() + ", jar path: " + model.getJarPath() + ", new jar name: " + model.getNewJarName());

            List<String> classNames = new ArrayList<>();
            for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); ) {
                JarEntry jarEntry = entries.nextElement();
                if (jarEntry.getName().endsWith(".class"))
                    classNames.add(changePathToPackageName(jarEntry.getName()));
            }

            ClassPool classPool = ClassPool.getDefault();
            classPool.insertClassPath(jarFile.getName());
            List<CtClass> ctClasses = new ArrayList<>();
            for (String jarClass : classNames) {
                ctClasses.add(classPool.get(jarClass));
            }

            model.setOldClasses(ctClasses);
            updateUI();
        }

        private String getDefaultNewName(String fullName) {
            String fileName = getNameFromJarFile(fullName);
            String[] split = fileName.split("\\.");
            return split[0] +
                    "Updated." +
                    split[1];
        }

        private String getPathFromJarFile(String fullName) {
            String fileName = getNameFromJarFile(fullName);
            return fullName.substring(0, fullName.length() - fileName.length());
        }

        private String getNameFromJarFile(String fullName) {
            String[] split = fullName.split(Pattern.quote("\\"));
            return split[split.length - 1];
        }

        private String changePathToPackageName(String name) {
            return name.replace("/", ".").substring(0, name.length() - 6);
        }
    }

    private class NewNameTypedListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            saveNewJarName(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            saveNewJarName(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {

        }

        private void saveNewJarName(DocumentEvent e) {
            Document document = e.getDocument();
            int length = document.getLength();
            String text = null;
            try {
                text = document.getText(0, length);
            } catch (BadLocationException e1) {
                LOGGER.log(Level.WARNING, "Bad location ex", e1);
            }
            model.setNewJarName(text);
        }
    }

    private class ExportJarListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (model.getOldJarName() == null) {
                LOGGER.info(Texts.NO_JAR_FILE_LOADED_ERR);
                JOptionPane.showMessageDialog(view.getPanel(), Texts.NO_JAR_FILE_LOADED_ERR, Texts.NO_JAR_FILE_LOADED_ERR, JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<CtClass> ctClasses = model.getListOfClasses();
            List<UpdatedClass> updatedClasses = new ArrayList<>();

            ctClasses.forEach(ctClass -> {
                try {
                    updatedClasses.add(new UpdatedClass(ctClass));
                } catch (IOException | CannotCompileException e1) {
                    LOGGER.log(Level.WARNING, "Error in ExportJarMethod", e1);
                }
            });

            JarHandler jarHandler = new JarHandler();
            try {
                jarHandler.createUpdatedJar(model.getJarPath(), model.getOldJarName(), model.getNewJarName(), updatedClasses);
            } catch (IOException e1) {
                LOGGER.log(Level.WARNING, "Exception in saving new Jar", e1);
            }

            JOptionPane.showMessageDialog(view.getPanel(), Texts.NEW_JAR_INFO_1 + model.getJarPath() + Texts.NEW_JAR_INFO_2 + model.getNewJarName(), Texts.NEW_JAR_INFO_TITLE, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class AddClassListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<CtClass> ctClasses = model.getListOfClasses();
            String newClassName = view.getCodeTextArea().getText();
            LOGGER.info("Class to add: " + newClassName);
            if (model.getOldJarName() == null) {
                LOGGER.warning("Jar file is not loaded");
                JOptionPane.showMessageDialog(view.getPanel(), Texts.NO_JAR_LOADED, "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (ctClasses.stream().map(CtClass::getName).anyMatch(newClassName::equals)) {
                LOGGER.info("Class with this name already exist: " + newClassName);
                JOptionPane.showMessageDialog(view.getPanel(), Texts.CLASS_EXIST_ERR, "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (model.getRemovedClasses().stream().map(CtClass::getName).anyMatch(newClassName::equals)) {
                List<CtClass> matchedCtClasses = model.getRemovedClasses().stream().filter(ctClass -> ctClass.getName().equals(newClassName)).collect(Collectors.toList());
                model.getRemovedClasses().removeAll(matchedCtClasses);
                model.getAddedClasses().addAll(matchedCtClasses);
                updateUI();
                LOGGER.info("OK - class added: " + newClassName);
                JOptionPane.showMessageDialog(view.getPanel(), Texts.CLASS_ADDED_INFO, "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            ClassPool classPool = ClassPool.getDefault();
            CtClass ctClass = classPool.makeClass(newClassName);
            try {
                ctClass.toClass();
                ctClass.defrost();
            } catch (CannotCompileException e1) {
                LOGGER.log(Level.WARNING, "Error in addClassListener", e1);
                JOptionPane.showMessageDialog(view.getPanel(), "Error", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            model.getAddedClasses().add(ctClass);

            updateUI();
            LOGGER.info("OK - class added: " + newClassName);
            JOptionPane.showMessageDialog(view.getPanel(), Texts.CLASS_ADDED_INFO, "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class AddInterfaceListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<CtClass> ctClasses = model.getListOfClasses();
            String newInterfaceName = view.getCodeTextArea().getText();
            LOGGER.info("Interface to add: " + newInterfaceName);
            if (model.getOldJarName() == null) {
                LOGGER.warning("Jar file is not loaded");
                JOptionPane.showMessageDialog(view.getPanel(), Texts.NO_JAR_LOADED, "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (ctClasses.stream().map(CtClass::getName).anyMatch(newInterfaceName::equals)) {
                LOGGER.info("Class with this name already exist: " + newInterfaceName);
                JOptionPane.showMessageDialog(view.getPanel(), Texts.CLASS_EXIST_ERR, "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (model.getRemovedClasses().stream().map(CtClass::getName).anyMatch(newInterfaceName::equals)) {
                List<CtClass> matchedCtClasses = model.getRemovedClasses().stream().filter(ctClass -> ctClass.getName().equals(newInterfaceName)).collect(Collectors.toList());
                model.getRemovedClasses().removeAll(matchedCtClasses);
                model.getAddedClasses().addAll(matchedCtClasses);
                updateUI();
                LOGGER.info("OK - interface added: " + newInterfaceName);
                JOptionPane.showMessageDialog(view.getPanel(), Texts.INTERFACE_ADDED_INFO, "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            ClassPool classPool = ClassPool.getDefault();
            CtClass ctClass = classPool.makeInterface(newInterfaceName);
            try {
                ctClass.toClass();
                ctClass.defrost();
            } catch (CannotCompileException e1) {
                LOGGER.log(Level.WARNING, "Error in addInterfaceListener", e1);
                JOptionPane.showMessageDialog(view.getPanel(), "Error", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            model.getAddedClasses().add(ctClass);

            updateUI();
            LOGGER.info("OK - interface added: " + newInterfaceName);
            JOptionPane.showMessageDialog(view.getPanel(), Texts.INTERFACE_ADDED_INFO, "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class DeleteClassListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String classToDelete = model.getCurrentClass();
            LOGGER.info("Class to delete: " + classToDelete);
            if (classToDelete == null) {
                JOptionPane.showMessageDialog(view.getPanel(), Texts.NO_CLASS_SELECTED, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Optional<CtClass> first = model.getAddedClasses().stream().filter(ctClass -> ctClass.getName().equals(classToDelete)).findFirst();
            if (!first.isPresent()) {
                LOGGER.warning("Class to delete not found");
                return;
            }
            boolean flag = model.getAddedClasses().remove(first.get());
            if (flag)
                LOGGER.fine("Removed class from addedClassList");

            flag = model.getRemovedClasses().add(first.get());
            if (flag)
                LOGGER.fine("Class added to removedClassList");

            LOGGER.info("OK - class deleted: " + classToDelete);
            JOptionPane.showMessageDialog(view.getPanel(), Texts.CLASS_DELETED_INFO, "Info", JOptionPane.INFORMATION_MESSAGE);
            updateUI();
        }

    }

    private class AddMethodListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String methodToAdd = view.getCodeTextArea().getText();

            LOGGER.info("Method to add:" + methodToAdd);

            List<CtClass> ctClasses = model.getListOfClasses();

            Optional<CtClass> ctClassOptional = ctClasses.stream().filter(ctClass -> ctClass.getName().equals(model.getCurrentClass())).findFirst();
            if (!ctClassOptional.isPresent()) {
                LOGGER.warning("Add method - class not found");
                JOptionPane.showMessageDialog(view.getPanel(), Texts.NO_CLASS_SELECTED, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            CtClass ctClass = ctClassOptional.get();

            CtMethod ctMethod;
            try {
                if (ctClass.isFrozen())
                    ctClass.defrost();
                ctMethod = CtMethod.make(methodToAdd, ctClass);

                for (CtMethod method : ctClass.getDeclaredMethods()) {
                    if (method.getLongName().equals(ctMethod.getLongName()) && method.getSignature().equals(ctMethod.getSignature())) {
                        LOGGER.info("Method with given name already exist");
                        JOptionPane.showMessageDialog(view.getPanel(), Texts.METHOD_ALREADY_EXIST, "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                ctClass.addMethod(ctMethod);

            } catch (CannotCompileException e1) {
                LOGGER.log(Level.WARNING, "Exception in adding method", e1);
                JOptionPane.showMessageDialog(view.getPanel(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            markClassAsModified(ctClass);
            updateMethodsList();

            LOGGER.info("OK - method added");
            JOptionPane.showMessageDialog(view.getPanel(), Texts.METHOD_ADDED_INFO, "Info", JOptionPane.INFORMATION_MESSAGE);
        }


    }

    private void markClassAsModified(CtClass ctClass) {
        if (model.getOldClasses().contains(ctClass)) {
            model.getOldClasses().remove(ctClass);
            model.getModifiedClasses().add(ctClass);
        }
    }

    private class DeleteMethodListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            List<CtClass> ctClasses = model.getListOfClasses();
            Optional<CtClass> ctClassOptional = ctClasses.stream().filter(ctClass -> ctClass.getName().equals(model.getCurrentClass())).findFirst();
            if (!ctClassOptional.isPresent()) {
                LOGGER.warning("Method to remove - class not found");
                JOptionPane.showMessageDialog(view.getPanel(), Texts.NO_CLASS_SELECTED, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            CtClass ctClass = ctClassOptional.get();
            Optional<CtMethod> methodToRemove = Arrays.stream(ctClass.getDeclaredMethods()).filter(ctMethod -> ctMethod.getLongName().equals(model.getCurrentMethod())).findFirst();
            if (!methodToRemove.isPresent()) {
                LOGGER.warning("Method to remove not found. Class: " + ctClass.getName());
                JOptionPane.showMessageDialog(view.getPanel(), Texts.NO_METHOD_SELECTED, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            CtMethod ctMethod = methodToRemove.get();

            try {
                if (ctClass.isFrozen())
                    ctClass.defrost();
                ctClass.removeMethod(ctMethod);
            } catch (NotFoundException e1) {
                LOGGER.log(Level.WARNING, "Exception in deleting method", e1);
                JOptionPane.showMessageDialog(view.getPanel(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            markClassAsModified(ctClass);
            updateMethodsList();

            LOGGER.info("OK - method removed. Class: " + ctClass.getName() + ". Method: " + ctMethod.getLongName());
            JOptionPane.showMessageDialog(view.getPanel(), Texts.METHOD_REMOVED_INFO, "Info", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    private class OverwriteMethodBodyListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<CtClass> ctClasses = model.getListOfClasses();
            Optional<CtClass> ctClassOptional = ctClasses.stream().filter(ctClass -> ctClass.getName().equals(model.getCurrentClass())).findFirst();
            if (!ctClassOptional.isPresent()) {
                LOGGER.warning("Overwrite method body - Class not found");
                JOptionPane.showMessageDialog(view.getPanel(), Texts.NO_CLASS_SELECTED, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            CtClass ctClass = ctClassOptional.get();
            Optional<CtMethod> methodToOverwrite = Arrays.stream(ctClass.getDeclaredMethods()).filter(ctMethod -> ctMethod.getLongName().equals(model.getCurrentMethod())).findFirst();
            if (!methodToOverwrite.isPresent()) {
                LOGGER.warning("Method not found");
                JOptionPane.showMessageDialog(view.getPanel(), Texts.NO_METHOD_SELECTED, "Info", JOptionPane.ERROR_MESSAGE);
                return;
            }
            CtMethod ctMethod = methodToOverwrite.get();
            String text = view.getCodeTextArea().getText();

            try {
                if (ctClass.isFrozen())
                    ctClass.defrost();
                ctMethod.setBody(text);
                ctClass.defrost();
            } catch (CannotCompileException e1) {
                LOGGER.log(Level.WARNING, "Exception in overwriting method", e1);
                JOptionPane.showMessageDialog(view.getPanel(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            markClassAsModified(ctClass);

            LOGGER.info("OK - method overwritten. Class: " + ctClass.getName() + ". Method: " + ctMethod.getLongName());
            JOptionPane.showMessageDialog(view.getPanel(), Texts.METHOD_OVERWRITTEN_INFO, "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class InsertBeforeMethodListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<CtClass> ctClasses = model.getListOfClasses();
            Optional<CtClass> ctClassOptional = ctClasses.stream().filter(ctClass -> ctClass.getName().equals(model.getCurrentClass())).findFirst();
            if (!ctClassOptional.isPresent()) {
                LOGGER.warning("Insert before method - Class not found");
                JOptionPane.showMessageDialog(view.getPanel(), Texts.NO_CLASS_SELECTED, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            CtClass ctClass = ctClassOptional.get();
            Optional<CtMethod> methodToOverwrite = Arrays.stream(ctClass.getDeclaredMethods()).filter(ctMethod -> ctMethod.getLongName().equals(model.getCurrentMethod())).findFirst();
            if (!methodToOverwrite.isPresent()) {
                LOGGER.warning("Method not found");
                JOptionPane.showMessageDialog(view.getPanel(), Texts.NO_METHOD_SELECTED, "Info", JOptionPane.ERROR_MESSAGE);
                return;
            }
            CtMethod ctMethod = methodToOverwrite.get();
            String text = view.getCodeTextArea().getText();
            try {
                if (ctClass.isFrozen())
                    ctClass.defrost();
                ctMethod.insertBefore(text);
                ctClass.defrost();
            } catch (CannotCompileException e1) {
                LOGGER.log(Level.WARNING, "Exception while to trying insert before body", e1);
                JOptionPane.showMessageDialog(view.getPanel(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            markClassAsModified(ctClass);

            LOGGER.info("OK - insert before method body. Class: " + ctClass.getName() + ". Method: " + ctMethod.getLongName());
            JOptionPane.showMessageDialog(view.getPanel(), Texts.INSERT_BEFORE_METHOD_INFO, "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class InsertAfterMethodListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<CtClass> ctClasses = model.getListOfClasses();
            Optional<CtClass> ctClassOptional = ctClasses.stream().filter(ctClass -> ctClass.getName().equals(model.getCurrentClass())).findFirst();
            if (!ctClassOptional.isPresent()) {
                LOGGER.warning("Insert after method - Class not found");
                JOptionPane.showMessageDialog(view.getPanel(), Texts.NO_CLASS_SELECTED, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            CtClass ctClass = ctClassOptional.get();
            Optional<CtMethod> methodToOverwrite = Arrays.stream(ctClass.getDeclaredMethods()).filter(ctMethod -> ctMethod.getLongName().equals(model.getCurrentMethod())).findFirst();
            if (!methodToOverwrite.isPresent()) {
                LOGGER.warning("Method not found");
                JOptionPane.showMessageDialog(view.getPanel(), Texts.NO_METHOD_SELECTED, "Info", JOptionPane.ERROR_MESSAGE);
                return;
            }
            CtMethod ctMethod = methodToOverwrite.get();
            String text = view.getCodeTextArea().getText();
            try {
                if (ctClass.isFrozen())
                    ctClass.defrost();
                ctMethod.insertAfter(text);
                ctClass.defrost();
            } catch (CannotCompileException e1) {
                LOGGER.log(Level.WARNING, "Exception while to trying insert after body", e1);
                JOptionPane.showMessageDialog(view.getPanel(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            markClassAsModified(ctClass);

            LOGGER.info("OK - insert after method body. Class: " + ctClass.getName() + ". Method: " + ctMethod.getLongName());
            JOptionPane.showMessageDialog(view.getPanel(), Texts.INSERT_AFTER_METHOD_INFO, "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class AddConstructorListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<CtClass> ctClasses = model.getListOfClasses();

            Optional<CtClass> ctClassOptional = ctClasses.stream().filter(ctClass -> ctClass.getName().equals(model.getCurrentClass())).findFirst();
            if (!ctClassOptional.isPresent()) {
                LOGGER.warning("Add constructor - class not found");
                JOptionPane.showMessageDialog(view.getPanel(), Texts.NO_CLASS_SELECTED, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            CtClass ctClass = ctClassOptional.get();

            String text = view.getCodeTextArea().getText();

            LOGGER.info("Constructor to add:" + text);

            CtConstructor ctConstructor;

            try {
                if (ctClass.isFrozen()) {
                    ctClass.defrost();
                }
                ctConstructor = CtNewConstructor.make(text, ctClass);

                for (CtConstructor constructor : ctClass.getDeclaredConstructors()) {
                    if (constructor.getLongName().equals(ctConstructor.getLongName()) && constructor.getSignature().equals(ctConstructor.getSignature())) {
                        LOGGER.info("Constructor with given name already exist");
                        JOptionPane.showMessageDialog(view.getPanel(), Texts.CONSTRUCTOR_ALREADY_EXIST, "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                ctClass.addConstructor(ctConstructor);
            } catch (CannotCompileException e1) {
                LOGGER.log(Level.WARNING, "Exception in adding constructor", e1);
                JOptionPane.showMessageDialog(view.getPanel(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            markClassAsModified(ctClass);
            updateConstructorList();

            LOGGER.info("OK - constructor added");
            JOptionPane.showMessageDialog(view.getPanel(), Texts.CONSTRUCTOR_ADDED_INFO, "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class DeleteConstructorListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<CtClass> ctClasses = model.getListOfClasses();
            Optional<CtClass> ctClassOptional = ctClasses.stream().filter(ctClass -> ctClass.getName().equals(model.getCurrentClass())).findFirst();
            if (!ctClassOptional.isPresent()) {
                LOGGER.warning("Delete constructor - Class not found");
                JOptionPane.showMessageDialog(view.getPanel(), Texts.NO_CLASS_SELECTED, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            CtClass ctClass = ctClassOptional.get();
            Optional<CtConstructor> constructorToOverwrite = Arrays.stream(ctClass.getDeclaredConstructors()).filter(ctConstructor -> ctConstructor.getLongName().equals(model.getCurrentConstructor())).findFirst();
            if (!constructorToOverwrite.isPresent()) {
                LOGGER.warning("Constructor not found");
                JOptionPane.showMessageDialog(view.getPanel(), Texts.NO_CONSTRUCTOR_SELECTED, "Info", JOptionPane.ERROR_MESSAGE);
                return;
            }
            CtConstructor ctConstructor = constructorToOverwrite.get();

            try {
                if (ctClass.isFrozen()) {
                    ctClass.defrost();
                }
                ctClass.removeConstructor(ctConstructor);
            } catch (NotFoundException e1) {
                LOGGER.log(Level.WARNING, "Exception while to trying to delete constructor", e1);
                JOptionPane.showMessageDialog(view.getPanel(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

            markClassAsModified(ctClass);
            updateConstructorList();

            LOGGER.info("OK - delete constructor. Class: " + ctClass.getName() + ". Constructor: " + ctConstructor.getLongName());
            JOptionPane.showMessageDialog(view.getPanel(), Texts.CONSTRUCTOR_DELETED, "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class OverwriteConstructorBodyListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<CtClass> ctClasses = model.getListOfClasses();
            Optional<CtClass> ctClassOptional = ctClasses.stream().filter(ctClass -> ctClass.getName().equals(model.getCurrentClass())).findFirst();
            if (!ctClassOptional.isPresent()) {
                LOGGER.warning("Overwrite constructor - Class not found");
                JOptionPane.showMessageDialog(view.getPanel(), Texts.NO_CLASS_SELECTED, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            CtClass ctClass = ctClassOptional.get();

            Optional<CtConstructor> constructorToOverwrite = Arrays.stream(ctClass.getDeclaredConstructors()).filter(ctConstructor -> ctConstructor.getLongName().equals(model.getCurrentConstructor())).findFirst();
            if (!constructorToOverwrite.isPresent()) {
                LOGGER.warning("Constructor not found");
                JOptionPane.showMessageDialog(view.getPanel(), Texts.NO_CONSTRUCTOR_SELECTED, "Info", JOptionPane.ERROR_MESSAGE);
                return;
            }
            CtConstructor ctConstructor = constructorToOverwrite.get();
            String text = view.getCodeTextArea().getText();

            try {
                if (ctClass.isFrozen())
                    ctClass.defrost();
                ctConstructor.setBody(text);
                ctClass.defrost();
            } catch (CannotCompileException e1) {
                LOGGER.log(Level.WARNING, "Exception while to trying to overwrite constructor", e1);
                JOptionPane.showMessageDialog(view.getPanel(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            markClassAsModified(ctClass);

            LOGGER.info("OK - overwrite constructor. Class: " + ctClass.getName() + ". Constructor: " + ctConstructor.getLongName());
            JOptionPane.showMessageDialog(view.getPanel(), Texts.CONSTRUCTOR_OVERWRITTEN_INFO, "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class AddFieldListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<CtClass> ctClasses = model.getListOfClasses();

            Optional<CtClass> ctClassOptional = ctClasses.stream().filter(ctClass -> ctClass.getName().equals(model.getCurrentClass())).findFirst();
            if (!ctClassOptional.isPresent()) {
                LOGGER.warning("Add field - class not found");
                JOptionPane.showMessageDialog(view.getPanel(), Texts.NO_CLASS_SELECTED, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            CtClass ctClass = ctClassOptional.get();

            String text = view.getCodeTextArea().getText();

            LOGGER.info("Field to add:" + text);


            try {
                if (ctClass.isFrozen())
                    ctClass.defrost();
                CtField ctField = CtField.make(text, ctClass);

                for (CtField field : ctClass.getDeclaredFields()) {
                    if (field.getName().equals(ctField.getName())) {
                        LOGGER.info("Field with given name already exist");
                        JOptionPane.showMessageDialog(view.getPanel(), Texts.FIELD_ALREADY_EXIST, "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                ctClass.addField(ctField);
            } catch (CannotCompileException e1) {
                LOGGER.log(Level.WARNING, "Exception in adding field", e1);
                JOptionPane.showMessageDialog(view.getPanel(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            markClassAsModified(ctClass);
            updateFieldList();

            LOGGER.info("OK - field added");
            JOptionPane.showMessageDialog(view.getPanel(), Texts.FIELD_ADDED_INFO, "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class DeleteFieldListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<CtClass> ctClasses = model.getListOfClasses();
            Optional<CtClass> ctClassOptional = ctClasses.stream().filter(ctClass -> ctClass.getName().equals(model.getCurrentClass())).findFirst();
            if (!ctClassOptional.isPresent()) {
                LOGGER.warning("Delete field - Class not found");
                JOptionPane.showMessageDialog(view.getPanel(), Texts.NO_CLASS_SELECTED, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            CtClass ctClass = ctClassOptional.get();
            Optional<CtField> fieldToDelete = Arrays.stream(ctClass.getDeclaredFields()).filter(ctConstructor -> ctConstructor.getName().equals(model.getCurrentField())).findFirst();
            if (!fieldToDelete.isPresent()) {
                LOGGER.warning("Delete field - no field selected");
                JOptionPane.showMessageDialog(view.getPanel(), Texts.NO_FIELD_SELECTED, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            CtField ctField = fieldToDelete.get();

            try {
                if (ctClass.isFrozen())
                    ctClass.defrost();
                ctClass.removeField(ctField);
            } catch (NotFoundException e1) {
                LOGGER.log(Level.WARNING, "Exception while trying to delete field", e1);
                JOptionPane.showMessageDialog(view.getPanel(), e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            markClassAsModified(ctClass);
            updateFieldList();

            LOGGER.info("OK - delete field. Class: " + ctClass.getName() + ". Field: " + ctField.getName());
            JOptionPane.showMessageDialog(view.getPanel(), Texts.FIELD_DELETED, "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
