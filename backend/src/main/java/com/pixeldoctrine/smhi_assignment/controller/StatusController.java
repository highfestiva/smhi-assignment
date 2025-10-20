package com.pixeldoctrine.smhi_assignment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pixeldoctrine.smhi_assignment.dto.StatusDTO;

@RestController
@RequestMapping("/v1")
public class StatusController {

    @GetMapping("/status")
    public StatusDTO getStatus() {
        return new StatusDTO("ok", 0);
    }
}
