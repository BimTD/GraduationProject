package org.example.graduationproject.analytics.controllers;

import org.example.graduationproject.analytics.models.MarketingTactic;
import org.example.graduationproject.analytics.services.MarketingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<Map<String, Object>> getMarketingStrategiesApi() {
        Map<String, Object> strategies = marketingService.getMarketingStrategies();
        return ResponseEntity.ok(strategies);
    }
    
    // ========== CRUD APIs FOR MARKETING TACTICS ==========
    
    // Lấy tất cả chiến thuật của một cluster
    @GetMapping("/{clusterId}/tactics")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<List<MarketingTactic>> getTacticsByCluster(@PathVariable Integer clusterId) {
        List<MarketingTactic> tactics = marketingService.getTacticsByClusterId(clusterId);
        return ResponseEntity.ok(tactics);
    }
    
    // Lấy chi tiết một chiến thuật
    @GetMapping("/tactics/{tacticId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<MarketingTactic> getTacticById(@PathVariable Integer tacticId) {
        MarketingTactic tactic = marketingService.getTacticById(tacticId);
        if (tactic == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tactic);
    }
    
    // Tạo chiến thuật mới
    @PostMapping("/tactics")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> createTactic(@RequestBody MarketingTactic tactic) {
        try {
            MarketingTactic createdTactic = marketingService.createTactic(tactic);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTactic);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Cập nhật chiến thuật
    @PutMapping("/tactics/{tacticId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> updateTactic(@PathVariable Integer tacticId, @RequestBody MarketingTactic tactic) {
        try {
            tactic.setId(tacticId);
            MarketingTactic updatedTactic = marketingService.updateTactic(tactic);
            return ResponseEntity.ok(updatedTactic);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Xóa chiến thuật (soft delete)
    @DeleteMapping("/tactics/{tacticId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> deleteTactic(@PathVariable Integer tacticId) {
        try {
            marketingService.deleteTactic(tacticId);
            return ResponseEntity.ok(Map.of("message", "Chiến thuật đã được xóa."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    
    // Kích hoạt/vô hiệu hóa chiến thuật
    @PatchMapping("/tactics/{tacticId}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> toggleTacticStatus(@PathVariable Integer tacticId) {
        try {
            MarketingTactic tactic = marketingService.toggleTacticStatus(tacticId);
            return ResponseEntity.ok(tactic);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Cập nhật trạng thái chiến thuật
    @PatchMapping("/tactics/{tacticId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> updateTacticStatus(@PathVariable Integer tacticId, @RequestParam String status) {
        try {
            MarketingTactic tactic = marketingService.updateTacticStatus(tacticId, status);
            return ResponseEntity.ok(tactic);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Lấy thống kê chiến thuật
    @GetMapping("/{clusterId}/tactics/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTacticStatistics(@PathVariable Integer clusterId) {
        Map<String, Object> stats = marketingService.getTacticStatistics(clusterId);
        return ResponseEntity.ok(stats);
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
    
    // ========== MARKETING OBJECTIVES API ENDPOINTS ==========
    
    // Lấy tất cả objectives theo cluster ID
    @GetMapping("/{clusterId}/objectives")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getObjectivesByClusterId(@PathVariable Integer clusterId) {
        try {
            List<Map<String, Object>> objectives = marketingService.getObjectivesByClusterId(clusterId);
            return ResponseEntity.ok(objectives);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of(Map.of("error", e.getMessage())));
        }
    }
    
    // Lấy objective theo ID
    @GetMapping("/objectives/{objectiveId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getObjectiveById(@PathVariable Long objectiveId) {
        try {
            Map<String, Object> objective = marketingService.getObjectiveById(objectiveId);
            return ResponseEntity.ok(objective);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Tạo objective mới
    @PostMapping("/objectives")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createObjective(@RequestBody Map<String, Object> objectiveData) {
        try {
            Map<String, Object> objective = marketingService.createObjective(objectiveData);
            return ResponseEntity.ok(objective);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Cập nhật objective
    @PutMapping("/objectives/{objectiveId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateObjective(@PathVariable Long objectiveId, @RequestBody Map<String, Object> objectiveData) {
        try {
            Map<String, Object> objective = marketingService.updateObjective(objectiveId, objectiveData);
            return ResponseEntity.ok(objective);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Xóa objective (permanent delete)
    @DeleteMapping("/objectives/{objectiveId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteObjective(@PathVariable Long objectiveId) {
        try {
            marketingService.deleteObjective(objectiveId);
            return ResponseEntity.ok(Map.of("message", "Mục tiêu marketing đã được xóa vĩnh viễn khỏi database!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Toggle trạng thái active của objective
    @PatchMapping("/objectives/{objectiveId}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleObjectiveStatus(@PathVariable Long objectiveId) {
        try {
            Map<String, Object> objective = marketingService.toggleObjectiveStatus(objectiveId);
            return ResponseEntity.ok(objective);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Cập nhật status của objective
    @PatchMapping("/objectives/{objectiveId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateObjectiveStatus(@PathVariable Long objectiveId, @RequestBody Map<String, String> statusData) {
        try {
            String status = statusData.get("status");
            Map<String, Object> objective = marketingService.updateObjectiveStatus(objectiveId, status);
            return ResponseEntity.ok(objective);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // ========== REPORT EXPORT ENDPOINTS ==========
    
    // Xuất báo cáo PDF cho marketing strategy
    @GetMapping("/{clusterId}/export/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportMarketingReportPdf(@PathVariable Integer clusterId) {
        try {
            byte[] pdfBytes = marketingService.exportMarketingReportToPdf(clusterId);
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"marketing-strategy-report-" + clusterId + ".pdf\"")
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Xuất báo cáo Excel cho marketing strategy
    @GetMapping("/{clusterId}/export/excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportMarketingReportExcel(@PathVariable Integer clusterId) {
        try {
            byte[] excelBytes = marketingService.exportMarketingReportToExcel(clusterId);
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .header("Content-Disposition", "attachment; filename=\"marketing-strategy-report-" + clusterId + ".xlsx\"")
                    .body(excelBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
















