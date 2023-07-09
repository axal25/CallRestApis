package axal25.oles.jacek;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class)
public class CallRestApisApp {
    public static void main(String[] args) {
        SpringApplication.run(CallRestApisApp.class, args);
    }
}
