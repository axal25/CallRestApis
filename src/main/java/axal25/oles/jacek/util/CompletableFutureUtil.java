package axal25.oles.jacek.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class CompletableFutureUtil {
    public static Executor getDefaultExecutor() {
        return CompletableFuture.completedFuture("").defaultExecutor();
    }
}
