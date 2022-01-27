package chartgram;

import chartgram.config.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.TimeZone;

// TODO: disattivare server Tomcat se non necessario

@SpringBootApplication
@Slf4j
public class App implements ApplicationRunner {
    private ApplicationContext context;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(ApplicationArguments arg0) throws Exception {
        Configuration configuration = context.getBean(Configuration.class);
        TimeZone.setDefault(TimeZone.getTimeZone(configuration.getTimezone()));
    }

    @Autowired
    public void setupContext(ApplicationContext context) {
        this.context = context;
    }

    // works only if running from jar
    public static String getArtifactVersion() {
        try {
            String s = App.class.getProtectionDomain().getCodeSource().getLocation().toURI().toString();
            int extensionStartIndex = s.indexOf(".jar");
            if (extensionStartIndex < 0) {
                return "";
            }
            int dashIndex = 0;
            for (int i = extensionStartIndex; i > 0; i--) {
                if (s.charAt(i)=='-') {
                    dashIndex = i;
                    break;
                }
            }
            if (dashIndex != 0) {
                s = s.substring(dashIndex + 1, extensionStartIndex);
            }
            return s;
        } catch (Exception e) {
            log.error("Cannot obtain artifact version", e);
            return null;
        }
    }
}