package chartgram;

import chartgram.config.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TimeZone;

@SpringBootApplication
@Slf4j
public class App implements ApplicationRunner {
    private ApplicationContext context;

    public static void main(String[] args) {
        setupConfigurationFiles();
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(ApplicationArguments arg0) throws Exception {
        Configuration configuration = context.getBean(Configuration.class);
        TimeZone.setDefault(TimeZone.getTimeZone(configuration.getTimezone()));
        if (configuration.isTest()) {
            log.warn("Test mode enabled! API won't ask for authorization");
        }
    }

    public static void setupConfigurationFiles() {
        File[] configurationFiles = { new File("./config/application.json"), new File("./config/configuration.json") };
        for (File configurationFile : configurationFiles) {
            if (!configurationFile.exists()) {
                try {
                    Path createdFile = Files.copy(Path.of(configurationFile.getPath() + ".example"), Path.of(configurationFile.getPath()));
                    log.info("Created {} from example file", createdFile);
                } catch (IOException e) {
                    log.error("Error while creating {} from example file", configurationFile.getPath(), e);
                }
            } else {
                log.info("Loaded {}", configurationFile.getPath());
            }
        }
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