package com.pixeldoctrine.smhi_assignment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pixeldoctrine.smhi_assignment.dto.StatusDTO;

@RestController
public class StatusController {

    @GetMapping("/v1/status")
    public StatusDTO getStatus() {
        return new StatusDTO("ok", 0);
    }
}
