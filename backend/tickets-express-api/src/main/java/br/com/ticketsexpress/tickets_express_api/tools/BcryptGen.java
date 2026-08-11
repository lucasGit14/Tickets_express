package br.com.ticketsexpress.tickets_express_api.tools;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;
import java.io.FileWriter;

public class BcryptGen {
    public static void main(String[] args) throws Exception {
        String passwd = args.length > 0 ? args[0] : "gatekeeper";
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        String hash = enc.encode(passwd);
        File out = new File("target/bcrypt-seed.txt");
        out.getParentFile().mkdirs();
        try (FileWriter fw = new FileWriter(out)) {
            fw.write(hash);
        }
    }
}
