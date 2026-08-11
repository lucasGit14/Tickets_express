package br.com.ticketsexpress.tickets_express_api;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;
import java.io.FileWriter;

public class GenerateBcryptTest {
    @Test
    public void writeHashToFile() throws Exception {
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        String hash = enc.encode("gatekeeper");
        File out = new File("target/generated-bcrypt.txt");
        out.getParentFile().mkdirs();
        try (FileWriter fw = new FileWriter(out)) {
            fw.write(hash);
        }
    }
}
