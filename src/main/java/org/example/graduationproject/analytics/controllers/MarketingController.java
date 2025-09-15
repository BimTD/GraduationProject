package org.example.graduationproject.analytics.controllers;

import org.example.graduationproject.analytics.services.MarketingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/admin/marketing")
public class MarketingController {
    
    @Autowired
    private MarketingService marketingService;
    
    // Trang chính marketing strategies
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String marketingStrategies(Model model) {
        Map<String, Object> strategies = marketingService.getMarketingStrategies();
        model.addAttribute("strategies", strategies);
        model.addAttribute("currentPage", "marketing");
        return "admin/marketing";
    }
    
    // Trang chi tiết cho một cluster
    @GetMapping("/{clusterId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String marketingStrategyDetail(@PathVariable Integer clusterId, Model model) {
        Map<String, Object> strategy = marketingService.getMarketingStrategyForCluster(clusterId);
        if (strategy == null) {
            return "redirect:/admin/marketing";
        }
        model.addAttribute("strategy", strategy);
        model.addAttribute("currentPage", "marketing");
        return "admin/marketing-detail";
    }
    
    // API để lấy overview
    @GetMapping("/api/overview")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMarketingOverview() {
        Map<String, Object> overview = marketingService.getMarketingOverview();
        return ResponseEntity.ok(overview);
    }
    
    // API để lấy strategies
    @GetMapping("/api/strategies")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMarketingStrategies() {
        Map<String, Object> strategies = marketingService.getMarketingStrategies();
        return ResponseEntity.ok(strategies);
    }
    
    // API để lấy strategy cho cluster cụ thể
    @GetMapping("/api/strategies/{clusterId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMarketingStrategyForCluster(@PathVariable Integer clusterId) {
        Map<String, Object> strategy = marketingService.getMarketingStrategyForCluster(clusterId);
        if (strategy == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(strategy);
    }
}



