package com.research.assistant;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins="*")
@RequestMapping("/api/research")
@AllArgsConstructor
public class ResearchController {
    private final ResearchService researchservice;
    @PostMapping("/process")
    public ResponseEntity<String> processContent(@RequestBody ResearchRequest request){
String result = researchservice.processContent(request);
        return ResponseEntity.ok(result);
    }
}
