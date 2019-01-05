package pl.edu.wat.wcy.jfk.lab2.mvc;

import javassist.CtClass;
import lombok.Getter;
import lombok.Setter;
import pl.edu.wat.wcy.jfk.lab2.util.Texts;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class Model {

    private String jarPath;
    private String oldJarName;
    private String newJarName;

    private String currentClass;
    private String currentMethod;
    private String currentConstructor;
    private String currentField;

    private List<CtClass> oldClasses;
    private List<CtClass> modifiedClasses;
    private List<CtClass> addedClasses;
    private List<CtClass> removedClasses;

    public Model() {
        oldClasses = new ArrayList<>();
        modifiedClasses = new ArrayList<>();
        addedClasses = new ArrayList<>();
        removedClasses = new ArrayList<>();

        this.jarPath = Texts.DEFAULT_PATH;
    }

    public List<CtClass> getListOfClasses() {
        List<CtClass> ctClasses = new ArrayList<>(oldClasses);
        ctClasses.addAll(modifiedClasses);
        ctClasses.addAll(addedClasses);
        return ctClasses;
    }
}
