package pl.edu.wat.wcy.jfk.lab2;

import javassist.CannotCompileException;
import javassist.CtClass;
import lombok.Getter;

import java.io.IOException;

@Getter
public class UpdatedClass {
    private String name;
    private byte[] bytes;

    public UpdatedClass(CtClass ctClass) throws IOException, CannotCompileException {
        this.name = ctClass.getName().replace(".", "/") + ".class";
        this.bytes = ctClass.toBytecode();
    }

    public UpdatedClass(String name, byte[] bytes) {
        this.name = name;
        this.bytes = bytes;
    }
}
