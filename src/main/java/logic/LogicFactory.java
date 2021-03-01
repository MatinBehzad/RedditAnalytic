package logic;

//TODO this class is just a skeleton it must be completed
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class LogicFactory {

    private static final String PACKAGE = "logic.";
    private static final String SUFFIX = "Logic";

    private LogicFactory() {

    }

    // beporsam
    public static < R> R getFor(Class<R> type) {
        R newInstance = null;
        try {
            Constructor<R> declaredConstructor = type.getDeclaredConstructor();
            newInstance = declaredConstructor.newInstance();

        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException(e);
        }

        return newInstance;

    }

    public static < T> T getFor(String entityName) {
        try {
            return LogicFactory.getFor((Class< T>) Class.forName(PACKAGE + entityName + SUFFIX));
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
