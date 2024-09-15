package com.unconv.spring.web.rest;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
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

    /**
     * Retrieves the application version.
     *
     * <p>This endpoint provides the application version in a RESTful manner, returning a JSON
     * response containing the version information.
     *
     * @return {@link ResponseEntity} containing a {@link Map} with the key "version" and the
     *     application version as the value, along with an HTTP 200 OK status.
     */
    @GetMapping("v1/version")
    public ResponseEntity<Map<String, String>> getVersion() {
        Map<String, String> response = new HashMap<>();
        response.put("version", buildProperties.getVersion());

        return ResponseEntity.ok(response);
    }
}
