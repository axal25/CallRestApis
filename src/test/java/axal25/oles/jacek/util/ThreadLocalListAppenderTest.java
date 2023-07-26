package axal25.oles.jacek.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

public class ThreadLocalListAppenderTest {

    @Test
    void getInstance() {
        assertThat(ThreadLocalListAppender.getInstance()).isNotNull();
    }

    @Test
    void getInstance_list_isInitializedCorrectly() {
        ThreadLocalListAppender threadLocalListAppender = ThreadLocalListAppender.getInstance();
        assertThat(threadLocalListAppender.list).isNotNull();
        assertThat(threadLocalListAppender.list).isEmpty();
    }

    @Test
    void singleLoggingTestSimulation_with_stopAndDetach_and_start() {
        // @BeforeAll beforeAll()
        ThreadLocalListAppender threadLocalListAppender = ThreadLocalListAppender.getInstance();
        Logger logger = ThreadLocalListAppender.getLogger(this.getClass());

        AtomicReference<ListAppender<ILoggingEvent>> executorListAppenderRef = new AtomicReference<>();
        // executor simulate test thread
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            executor.execute(() -> {
                // @BeforeEach setUp()
                threadLocalListAppender.setSafelyContextLevelAttachAndStart(logger, Level.ALL);
                // @Test method()
                logger.error("message 1");
                try {
                    Thread.sleep(250L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.error("message 2");
                executorListAppenderRef.set(ThreadLocalListAppender.get());
                // @AfterEach tearDown()
                threadLocalListAppender.stopAndDetach(logger);
            });
        }

        assertThat(executorListAppenderRef.get().list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList()))
                .isEqualTo(List.of(
                        "message 1",
                        "message 2"));
    }

    @Test
    void twoLoggingTestsSimulation_singleThread_without_stopAndDetach_and_start_inBetweenTests() {
        // @BeforeAll beforeAll()
        ThreadLocalListAppender threadLocalListAppender = ThreadLocalListAppender.getInstance();
        Logger logger = ThreadLocalListAppender.getLogger(this.getClass());

        AtomicReference<ListAppender<ILoggingEvent>> executorListAppenderRef1 = new AtomicReference<>();
        AtomicReference<ListAppender<ILoggingEvent>> executorListAppenderRef2 = new AtomicReference<>();
        // executor simulate test thread
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            executor.execute(() -> {
                // @BeforeEach setUp()
                threadLocalListAppender.setSafelyContextLevelAttachAndStart(logger, Level.ALL);
                // @Test method()
                logger.error("message 1");
                try {
                    Thread.sleep(250L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.error("message 2");
                executorListAppenderRef1.set(ThreadLocalListAppender.get());
            });
            executor.execute(() -> {
                // @Test method()
                logger.error("message 3");
                try {
                    Thread.sleep(250L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.error("message 4");
                executorListAppenderRef2.set(ThreadLocalListAppender.get());
                // @AfterEach tearDown()
                threadLocalListAppender.stopAndDetach(logger);
            });
        }
        assertThat(executorListAppenderRef1.get()).isEqualTo(executorListAppenderRef2.get());
        assertThat(executorListAppenderRef2.get().list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList()))
                .isEqualTo(List.of(
                        "message 1",
                        "message 2",
                        "message 3",
                        "message 4"));
    }

    @Test
    void twoLoggingTestsSimulation_separateThreads_without_stopAndDetach_and_start_inBetweenTests() {
        // @BeforeAll beforeAll()
        ThreadLocalListAppender threadLocalListAppender = ThreadLocalListAppender.getInstance();
        Logger logger = ThreadLocalListAppender.getLogger(this.getClass());

        AtomicReference<ListAppender<ILoggingEvent>> executorListAppenderRef1 = new AtomicReference<>();
        AtomicReference<ListAppender<ILoggingEvent>> executorListAppenderRef2 = new AtomicReference<>();
        // executor simulate test thread
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            executor.execute(() -> {
                // @BeforeEach setUp()
                threadLocalListAppender.setSafelyContextLevelAttachAndStart(logger, Level.ALL);
                // @Test method()
                logger.error("message 1");
                try {
                    Thread.sleep(250L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.error("message 2");
                executorListAppenderRef1.set(ThreadLocalListAppender.get());
            });
            executor.execute(() -> {
                // @Test method()
                logger.error("message 3");
                try {
                    Thread.sleep(250L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.error("message 4");
                executorListAppenderRef2.set(ThreadLocalListAppender.get());
                // @AfterEach tearDown()
                threadLocalListAppender.stopAndDetach(logger);
            });
        }
        assertThat(executorListAppenderRef1.get()).isNotEqualTo(executorListAppenderRef2.get());
        assertThat(executorListAppenderRef1.get().list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList()))
                .isEqualTo(List.of(
                        "message 1",
                        "message 2"));
        assertThat(executorListAppenderRef2.get().list).isEmpty();
    }

    @Test
    void twoLoggingTestsSimulation_separateThreads_without_stopAndDetach_with_start_inBetweenTests() {
        // @BeforeAll beforeAll()
        ThreadLocalListAppender threadLocalListAppender = ThreadLocalListAppender.getInstance();
        Logger logger = ThreadLocalListAppender.getLogger(this.getClass());

        AtomicReference<ListAppender<ILoggingEvent>> executorListAppenderRef1 = new AtomicReference<>();
        AtomicReference<ListAppender<ILoggingEvent>> executorListAppenderRef2 = new AtomicReference<>();
        // executor simulate test thread
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            executor.execute(() -> {
                // @BeforeEach setUp()
                threadLocalListAppender.setSafelyContextLevelAttachAndStart(logger, Level.ALL);
                // @Test method()
                logger.error("message 1");
                try {
                    Thread.sleep(250L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.error("message 2");
                executorListAppenderRef1.set(ThreadLocalListAppender.get());
            });
            executor.execute(() -> {
                // @BeforeEach setUp()
                threadLocalListAppender.setSafelyContextLevelAttachAndStart(logger, Level.ALL);
                // @Test method()
                logger.error("message 3");
                try {
                    Thread.sleep(250L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.error("message 4");
                executorListAppenderRef2.set(ThreadLocalListAppender.get());
                // @AfterEach tearDown()
                threadLocalListAppender.stopAndDetach(logger);
            });
        }
        assertThat(executorListAppenderRef1.get()).isNotEqualTo(executorListAppenderRef2.get());
        assertThat(executorListAppenderRef1.get().list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList()))
                .isEqualTo(List.of(
                        "message 1",
                        "message 2"));
        assertThat(executorListAppenderRef2.get().list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList()))
                .isEqualTo(List.of(
                        "message 3",
                        "message 4"));
    }

    @Test
    void twoLoggingTestsSimulation_singleThread_with_stopAndDetach_and_start_inBetweenTests() {
        // @BeforeAll beforeAll()
        ThreadLocalListAppender threadLocalListAppender = ThreadLocalListAppender.getInstance();
        Logger logger = ThreadLocalListAppender.getLogger(this.getClass());

        AtomicReference<ListAppender<ILoggingEvent>> executorListAppenderRef1 = new AtomicReference<>();
        AtomicReference<ListAppender<ILoggingEvent>> executorListAppenderRef2 = new AtomicReference<>();
        // executor simulate test thread
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            executor.execute(() -> {
                // @BeforeEach setUp()
                threadLocalListAppender.setSafelyContextLevelAttachAndStart(logger, Level.ALL);
                // @Test method()
                logger.error("message 1");
                try {
                    Thread.sleep(250L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.error("message 2");
                executorListAppenderRef1.set(ThreadLocalListAppender.get());
                // @AfterEach tearDown()
                threadLocalListAppender.stopAndDetach(logger);
            });
            executor.execute(() -> {
                // @BeforeEach setUp()
                threadLocalListAppender.setSafelyContextLevelAttachAndStart(logger, Level.ALL);
                // @Test method()
                logger.error("message 3");
                try {
                    Thread.sleep(250L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.error("message 4");
                executorListAppenderRef2.set(ThreadLocalListAppender.get());
                // @AfterEach tearDown()
                threadLocalListAppender.stopAndDetach(logger);
            });
        }
        assertThat(executorListAppenderRef1.get()).isNotEqualTo(executorListAppenderRef2.get());
        assertThat(executorListAppenderRef1.get().list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList()))
                .isEqualTo(List.of(
                        "message 1",
                        "message 2"));
        assertThat(executorListAppenderRef2.get().list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList()))
                .isEqualTo(List.of(
                        "message 3",
                        "message 4"));
    }

    @Test
    void twoLoggingTestsSimulation_separateThreads_with_stopAndDetach_and_start_inBetweenTests() {
        // @BeforeAll beforeAll()
        ThreadLocalListAppender threadLocalListAppender = ThreadLocalListAppender.getInstance();
        Logger logger = ThreadLocalListAppender.getLogger(this.getClass());

        AtomicReference<ListAppender<ILoggingEvent>> executorListAppenderRef1 = new AtomicReference<>();
        AtomicReference<ListAppender<ILoggingEvent>> executorListAppenderRef2 = new AtomicReference<>();
        // executor simulate test thread
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            executor.execute(() -> {
                // @BeforeEach setUp()
                threadLocalListAppender.setSafelyContextLevelAttachAndStart(logger, Level.ALL);
                // @Test method()
                logger.error("message 1");
                try {
                    Thread.sleep(250L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.error("message 2");
                executorListAppenderRef1.set(ThreadLocalListAppender.get());
                // @AfterEach tearDown()
                threadLocalListAppender.stopAndDetach(logger);
            });
            executor.execute(() -> {
                // @BeforeEach setUp()
                threadLocalListAppender.setSafelyContextLevelAttachAndStart(logger, Level.ALL);
                // @Test method()
                logger.error("message 3");
                try {
                    Thread.sleep(250L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.error("message 4");
                executorListAppenderRef2.set(ThreadLocalListAppender.get());
                // @AfterEach tearDown()
                threadLocalListAppender.stopAndDetach(logger);
            });
        }
        assertThat(executorListAppenderRef1.get()).isNotEqualTo(executorListAppenderRef2.get());
        assertThat(executorListAppenderRef1.get().list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList()))
                .isEqualTo(List.of(
                        "message 1",
                        "message 2"));
        assertThat(executorListAppenderRef2.get().list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList()))
                .isEqualTo(List.of(
                        "message 3",
                        "message 4"));
    }
}
