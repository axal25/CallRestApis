package axal25.oles.jacek.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ThreadLocalListAppender extends ListAppender<ILoggingEvent> {
    private static final ThreadLocal<ListAppender<ILoggingEvent>> threadLocal = new ThreadLocal<>();
    private static final ThreadLocalListAppender instance = new ThreadLocalListAppender();
    // TODO: proxy
    public final List<ILoggingEvent> list = new ArrayList<>();

    private ThreadLocalListAppender() {
        waitUntilLogBackIsReady();
    }

    public static Logger getLogger(Class<?> classContaining) {
        waitUntilLogBackIsReady();
        return (Logger) LoggerFactory.getLogger(classContaining);
    }

    public static synchronized ThreadLocalListAppender getInstance() {
        waitUntilLogBackIsReady();
        return instance;
    }

    private static void waitUntilLogBackIsReady() {
        while (!isLogBackReady()) ;
    }

    private static boolean isLogBackReady() {
        return LoggerFactory.getILoggerFactory() instanceof LoggerContext;
    }

    private static <T, R> R callMethod(T instanceToBeCalledOn, String methodName, Object... arguments) {
        Class<?> classToBeCalledOn = instanceToBeCalledOn.getClass();
        Class<?>[] argumentClasses = Arrays.stream(arguments).map(Object::getClass).toArray(Class<?>[]::new);

        Method method = null;
        try {
            method = classToBeCalledOn.getDeclaredMethod(methodName, argumentClasses);
        } catch (NoSuchMethodException e) {
            argumentClasses = Arrays.stream(argumentClasses).map(aClass -> Object.class).toArray(Class<?>[]::new);
            try {
                method = classToBeCalledOn.getDeclaredMethod(methodName, argumentClasses);
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException(
                        String.format("%s during calling method via reflection." +
                                        " During getting declared method: \"%s\", with class of arguments: [%s].",
                                e.getClass().getSimpleName(),
                                methodName,
                                Arrays.stream(argumentClasses).map(Class::getName).collect(Collectors.joining())),
                        e);
            }
        }

        method.setAccessible(true);
        Object invokeReturn = null;
        try {
            invokeReturn = method.invoke(instanceToBeCalledOn, arguments);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(
                    String.format("%s during calling method via reflection." +
                                    " During declared method invocation: %s, with arguments: [%s].",
                            e.getClass().getSimpleName(),
                            method,
                            Arrays.stream(arguments).map(Object::toString).collect(Collectors.joining())),
                    e);
        }

        if (invokeReturn == null) {
            return null;
        }

        try {
            @SuppressWarnings("unchecked") R toBeReturned = (R) invokeReturn;
            return toBeReturned;
        } catch (ClassCastException e) {
            throw new RuntimeException(
                    String.format("%s during calling method via reflection." +
                                    " During casting %s to expected return type.",
                            e.getClass().getSimpleName(),
                            invokeReturn),
                    e);
        }
    }

    public ListAppender<ILoggingEvent> get() {
        ListAppender<ILoggingEvent> listAppender = threadLocal.get();

        if (listAppender == null) {
            listAppender = new ListAppender<>();
            threadLocal.set(listAppender);
        }

        return listAppender;
    }

    public void stopAndDetach(Logger logger) {
        ListAppender<ILoggingEvent> listAppender = get();
        listAppender.stop();
        logger.detachAppender(listAppender);

        threadLocal.set(null);
    }

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        callMethod(get(), "append", iLoggingEvent);
    }

    @Override
    public String getName() {
        return get().getName();
    }

    @Override
    public synchronized void doAppend(ILoggingEvent eventObject) {
        get().doAppend(eventObject);
    }

    @Override
    public void setName(String name) {
        get().setName(name);
    }

    @Deprecated
    @Override
    public void start() {
        get().start();
    }

    @Override
    public void stop() {
        get().stop();
    }

    @Override
    public boolean isStarted() {
        return get().isStarted();
    }

    @Override
    public String toString() {
        return get().toString();
    }

    @Override
    public void addFilter(Filter<ILoggingEvent> newFilter) {
        get().addFilter(newFilter);
    }

    @Override
    public void clearAllFilters() {
        get().clearAllFilters();
    }

    @Override
    public List<Filter<ILoggingEvent>> getCopyOfAttachedFiltersList() {
        return get().getCopyOfAttachedFiltersList();
    }

    @Override
    public FilterReply getFilterChainDecision(ILoggingEvent event) {
        return get().getFilterChainDecision(event);
    }

    public void setSafelyContextLevelAttachAndStart(Logger logger, Level level) {
        waitUntilLogBackIsReady();
        if (getContext() == null) {
            setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        }
        logger.setLevel(level);
        synchronized (logger) {
            List<Appender<ILoggingEvent>> appenders = getAppenders(logger);
            if (!appenders.contains(this)) {
                logger.addAppender(this);
            }
        }
        get().start();
    }

    private List<Appender<ILoggingEvent>> getAppenders(Logger logger) {
        Iterator<Appender<ILoggingEvent>> iterator = logger.iteratorForAppenders();
        Iterable<Appender<ILoggingEvent>> iterable = () -> iterator;
        Spliterator<Appender<ILoggingEvent>> spliterator = iterable.spliterator();
        return StreamSupport.stream(spliterator, false).toList();
    }

    @Override
    public void setContext(Context context) {
        get().setContext(context);
    }

    @Override
    public Context getContext() {
        return get().getContext();
    }

    @Override
    public StatusManager getStatusManager() {
        return get().getStatusManager();
    }

    @Override
    protected Object getDeclaredOrigin() {
        return callMethod(get(), "getDeclaredOrigin");
    }

    @Override
    public void addStatus(Status status) {
        get().addStatus(status);
    }

    @Override
    public void addInfo(String msg) {
        get().addInfo(msg);
    }

    @Override
    public void addInfo(String msg, Throwable ex) {
        get().addInfo(msg, ex);
    }

    @Override
    public void addWarn(String msg) {
        get().addWarn(msg);
    }

    @Override
    public void addWarn(String msg, Throwable ex) {
        get().addWarn(msg, ex);
    }

    @Override
    public void addError(String msg) {
        get().addError(msg);
    }

    @Override
    public void addError(String msg, Throwable ex) {
        get().addError(msg, ex);
    }
}
