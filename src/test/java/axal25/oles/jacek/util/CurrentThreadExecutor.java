package axal25.oles.jacek.util;

import java.util.concurrent.Executor;

public class CurrentThreadExecutor implements Executor {

    public CurrentThreadExecutor() {
    }


    @Override
    public void execute(Runnable runnable) {
        runnable.run();
    }
}
