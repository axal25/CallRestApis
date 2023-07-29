package axal25.oles.jacek.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

public class ThreadLocalListAppender {
    private final ThreadLocal<ListAppender<ILoggingEvent>> threadLocal = new ThreadLocal<>();
    // simulates ListAppender's public List<E> list field using proxy.
    public final List<ILoggingEvent> list = (List<ILoggingEvent>) Proxy.newProxyInstance(
            ThreadLocalListAppender.class.getClassLoader(),
            new Class[]{List.class},
            (proxy, method, args) ->
                    method.invoke(
                            getListAppenderForCurrentThread().list,
                            args));
    private final Appender<ILoggingEvent> listAppenderProxy = (Appender<ILoggingEvent>) Proxy.newProxyInstance(
            ThreadLocalListAppender.class.getClassLoader(),
            new Class[]{Appender.class},
            (proxy, method, args) ->
                    method.invoke(
                            getListAppenderForCurrentThread(),
                            args));

    public ThreadLocalListAppender() {
        waitUntilLogBackIsReady();
    }

    public static Logger getLogger(Class<?> classContaining) {
        waitUntilLogBackIsReady();
        return (Logger) LoggerFactory.getLogger(classContaining);
    }

    private static void waitUntilLogBackIsReady() {
        while (!isLogBackReady()) ;
    }

    private static boolean isLogBackReady() {
        return LoggerFactory.getILoggerFactory() instanceof LoggerContext;
    }

    @VisibleForTesting
    ListAppender<ILoggingEvent> getListAppenderForCurrentThread() {
        ListAppender<ILoggingEvent> listAppender = threadLocal.get();

        if (listAppender == null) {
            listAppender = new ListAppender<>();
            threadLocal.set(listAppender);
        }

        return listAppender;
    }

    public void stopAndDetach(Logger logger) {
        ListAppender<ILoggingEvent> listAppender = getListAppenderForCurrentThread();
        listAppender.stop();
        logger.detachAppender(listAppender);

        threadLocal.set(null);
    }

    public void setSafelyContextLevelAttachAndStart(Logger logger, Level level) {
        waitUntilLogBackIsReady();
        ListAppender<ILoggingEvent> listAppender = getListAppenderForCurrentThread();
        if (listAppender.getContext() == null) {
            listAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        }
        logger.setLevel(level);
        synchronized (logger) {
            List<Appender<ILoggingEvent>> logAppenders = getAppenders(logger);
            if (!logAppenders.contains(listAppenderProxy)) {
                logger.addAppender(listAppenderProxy);
            }
        }
        getListAppenderForCurrentThread().start();
    }

    private List<Appender<ILoggingEvent>> getAppenders(Logger logger) {
        Iterator<Appender<ILoggingEvent>> iterator = logger.iteratorForAppenders();
        Iterable<Appender<ILoggingEvent>> iterable = () -> iterator;
        Spliterator<Appender<ILoggingEvent>> spliterator = iterable.spliterator();
        return StreamSupport.stream(spliterator, false).toList();
    }
}
