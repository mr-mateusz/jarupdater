package pl.edu.wat.wcy.jfk.lab2.jar;

import pl.edu.wat.wcy.jfk.lab2.UpdatedClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;

public class JarHandler {

    public static final Logger LOGGER = Logger.getLogger(JarHandler.class.getName());

    public void createUpdatedJar(String jarPath, String oldJarName, String newJarName, List<UpdatedClass> updatedClasses) throws IOException {
        newJarName = validateNewJarName(newJarName);

        LOGGER.info("Saving new jar. Path: " + jarPath + ", old jar: " + oldJarName + ", new jar: " + newJarName);

        File oldFile = new File(jarPath + oldJarName);
        File newFile = new File(jarPath + newJarName);

        try (JarFile oldJar = new JarFile(oldFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            try (JarOutputStream newJarOutputStream = new JarOutputStream(new FileOutputStream(newFile))) {

                LOGGER.info("Saving new classes: ");
                for (UpdatedClass updatedClass : updatedClasses) {
                    JarEntry entry = new JarEntry(updatedClass.getName());
                    newJarOutputStream.putNextEntry(entry);
                    newJarOutputStream.write(updatedClass.getBytes());

                    LOGGER.info(updatedClass.getName() + " saved! ");
                }

                LOGGER.info("Saving old entries ");
                InputStream entryStream = null;
                for (Enumeration entries = oldJar.entries(); entries.hasMoreElements(); ) {
                    JarEntry entry = (JarEntry) entries.nextElement();

                    if (updatedClasses.stream().map(UpdatedClass::getName).noneMatch(entry.getName()::equals)) {
                        LOGGER.info(entry.getName() + " -  saved! ");

                        entryStream = oldJar.getInputStream(entry);
                        newJarOutputStream.putNextEntry(entry);
                        while ((bytesRead = entryStream.read(buffer)) != -1) {
                            newJarOutputStream.write(buffer, 0, bytesRead);
                        }
                    } else {
                        LOGGER.info("This entry is written: " + entry.getName());
                    }
                }
                if (entryStream != null)
                    entryStream.close();
            }
        }
    }

    private String validateNewJarName(String newJarName) {
        return newJarName.endsWith(".jar") ? newJarName : newJarName.concat(".jar");
    }
}