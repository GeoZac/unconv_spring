package com.unconv.spring.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller class for handling application status requests. */
@RestController
@RequestMapping("/public/status")
public class ApplicationStatusController {

    @Autowired private BuildProperties buildProperties;

    /**
     * Retrieves the application version.
     *
     * @return the application version
     */
    @GetMapping("version")
    public String getAppVersion() {
        return buildProperties.getVersion();
    }
}
