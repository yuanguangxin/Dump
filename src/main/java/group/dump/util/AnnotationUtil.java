package group.dump.util;

import org.reflections.Reflections;

import java.util.*;

public class AnnotationUtil {

    public static Set<Class<?>> getClass(String pack, String AnnotationName) throws Exception {
        Class cl = Class.forName(AnnotationName);
        Reflections reflections = new Reflections(pack);
        Set<Class<?>> classesList = reflections.getTypesAnnotatedWith(cl);
        return classesList;
    }
}
