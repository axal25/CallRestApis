package axal25.oles.jacek.executor;

import axal25.oles.jacek.util.CompletableFutureUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

@Configuration
public class DefaultExecutorProvider {
    @Bean("executor")
    public Executor provideDefaultExecutor() {
        return CompletableFutureUtil.getDefaultExecutor();
    }
}
