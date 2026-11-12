package com.bracu.fraudgraph.controller;

import com.bracu.fraudgraph.domain.ImportBatch;
import com.bracu.fraudgraph.service.ImportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Optional;

/**
 * GET  /imports              -> ImportPage (upload form + saved snapshots)
 * POST /imports              -> import the uploaded file
 * GET  /imports/{batchId}    -> ValidationPage (result and issues of one upload)
 */
@Controller
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/imports";
    }

    @GetMapping("/imports")
    public String importPage(Model model) {
        model.addAttribute("snapshots", importService.allSnapshots());
        model.addAttribute("batches", importService.allBatches());
        return "import";
    }

    @PostMapping("/imports")
    public String upload(@RequestParam("file") MultipartFile file, RedirectAttributes redirect) throws IOException {
        if (file.isEmpty()) {
            redirect.addFlashAttribute("error", "Please choose a CSV file.");
            return "redirect:/imports";
        }
        byte[] content = file.getBytes();

        // Same file uploaded again: show the old result instead of saving it twice
        Optional<ImportBatch> earlier = importService.findSameFile(content);
        if (earlier.isPresent()) {
            redirect.addFlashAttribute("message", "This file was already imported, so nothing new was saved.");
            return "redirect:/imports/" + earlier.get().getId();
        }

        ImportBatch batch = importService.importCsv(file.getOriginalFilename(), content);
        return "redirect:/imports/" + batch.getId();
    }

    @GetMapping("/imports/{batchId}")
    public String validationPage(@PathVariable String batchId, Model model) {
        Optional<ImportBatch> batch = importService.findBatch(batchId);
        if (batch.isEmpty()) {
            return "redirect:/imports";
        }
        model.addAttribute("batch", batch.get());
        return "validation";
    }
}
