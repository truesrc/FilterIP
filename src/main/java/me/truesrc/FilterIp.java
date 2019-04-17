package me.truesrc;

import me.truesrc.web.IpBlocksHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FilterIp extends IpBlocksHelper {
    public static void main(String[] args) {
        SpringApplication.run(FilterIp.class, args);
    }
}
