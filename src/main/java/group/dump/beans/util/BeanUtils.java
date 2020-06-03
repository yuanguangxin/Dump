package group.dump.beans.util;

import group.dump.exception.DumpException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author yuanguangxin
 */
public class BeanUtils {

    public static <T> T instantiateClass(Constructor<T> ctor, Object... args){
        try {
            ctor.setAccessible(true);
            return ctor.newInstance(args);
        }
        catch (InstantiationException ex) {
            throw new DumpException("'"+ctor.getName()+"',Is it an abstract class?", ex);
        }
        catch (IllegalAccessException ex) {
            throw new DumpException("'"+ctor.getName()+",Is the constructor accessible?", ex);
        }
        catch (IllegalArgumentException ex) {
            throw new DumpException("'"+ctor.getName()+",Illegal arguments for constructor", ex);
        }
        catch (InvocationTargetException ex) {
            throw new DumpException("'"+ctor.getName()+",Constructor threw exception", ex.getTargetException());
        }
    }
}
