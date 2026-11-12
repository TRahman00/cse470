package com.bracu.fraudgraph.controller;

import com.bracu.fraudgraph.service.ForwardingAnalysisService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AnalysisController {

    private final ForwardingAnalysisService service;

    public AnalysisController(ForwardingAnalysisService service) {
        this.service = service;
    }

    @GetMapping("/analysis/forwarding")
    public String forwarding(@RequestParam(defaultValue = "5") int maxGapMinutes, Model model) {
        model.addAttribute("maxGapMinutes", maxGapMinutes);
        if (maxGapMinutes <= 0) {
            model.addAttribute("error", "Time limit must be a positive number of minutes.");
            return "forwarding";
        }
        var matches = service.run(service.demoSnapshot(), service.defaultRule(maxGapMinutes));
        model.addAttribute("matches", matches);
        return "forwarding";
    }
}
