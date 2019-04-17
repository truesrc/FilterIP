package me.truesrc.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author truesrc
 * @since 28.03.2019
 */

@RestController
public class Controller {

    @GetMapping("/")
    public String get() {
        return "Welcome";
    }
}
