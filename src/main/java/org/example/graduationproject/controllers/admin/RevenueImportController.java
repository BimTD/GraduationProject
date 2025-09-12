package org.example.graduationproject.controllers.admin;

import org.example.graduationproject.models.*;
import org.example.graduationproject.repositories.*;
import org.example.graduationproject.services.PhieuNhapHangService;
import org.example.graduationproject.services.ChiTietPhieuNhapHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class RevenueImportController {

    @Autowired
    private PhieuNhapHangRepository phieuNhapHangRepository;
    
    @Autowired
    private ChiTietPhieuNhapHangRepository chiTietPhieuNhapHangRepository;
    
    @Autowired
    private NhaCungCapRepository nhaCungCapRepository;
    
    @Autowired
    private SanPhamRepository sanPhamRepository;
    
    @Autowired
    private SanPhamBienTheRepository sanPhamBienTheRepository;

    @GetMapping("/revenue-import")
    public String revenueImportPage(Model model, 
                                   @RequestParam(value = "period", defaultValue = "month") String period,
                                   @RequestParam(value = "year", required = false) Integer year) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "admin";
        model.addAttribute("username", username);
        model.addAttribute("currentPage", "revenue-import");
        model.addAttribute("selectedPeriod", period);
        model.addAttribute("selectedYear", year);

        // Lấy tất cả phiếu nhập hàng với chi tiết
        List<PhieuNhapHang> allImports = phieuNhapHangRepository.findAll();
        
        // Không cần load chi tiết phức tạp vì đã đơn giản hóa logic
        
        // Tính toán dữ liệu thống kê nhập hàng
        Map<String, Object> importData = calculateImportData(allImports, period, year);
        
        // Thêm dữ liệu vào model
        model.addAllAttributes(importData);
        
        return "admin/revenue-import";
    }
    
    private Map<String, Object> calculateImportData(List<PhieuNhapHang> allImports, String period, Integer year) {
        Map<String, Object> data = new HashMap<>();
        
        // Tổng chi phí nhập hàng
        BigDecimal totalImportCost = allImports.stream()
                .map(PhieuNhapHang::getTongTien)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        data.put("totalImportCost", totalImportCost);
        data.put("totalImports", (long) allImports.size());
        
        // Tính giá trị nhập hàng trung bình
        BigDecimal averageImportValue = BigDecimal.ZERO;
        if (allImports.size() > 0) {
            averageImportValue = totalImportCost.divide(new BigDecimal(allImports.size()), 0, RoundingMode.HALF_UP);
        }
        data.put("averageImportValue", averageImportValue);
        
        // Chi phí nhập hàng theo thời gian
        Map<String, Double> importCostByTime = calculateImportCostByTime(allImports, period, year);
        data.put("importCostByTime", importCostByTime);
        
        // Tính tổng chi phí kỳ và trung bình kỳ
        Double periodTotalCost = importCostByTime.values().stream()
                .filter(Objects::nonNull)
                .reduce(0.0, Double::sum);
        Double periodAverageCost = 0.0;
        if (!importCostByTime.isEmpty()) {
            periodAverageCost = periodTotalCost / importCostByTime.size();
        }
        data.put("periodTotalCost", periodTotalCost);
        data.put("periodAverageCost", periodAverageCost);
        
        // Chi phí nhập hàng theo nhà cung cấp
        Map<String, Double> importCostBySupplier = calculateImportCostBySupplier(allImports);
        data.put("importCostBySupplier", importCostBySupplier);
        
        // Chi phí nhập hàng theo danh mục sản phẩm
        // Tổng quan nhập hàng (thay thế cho biểu đồ tháng)
        Map<String, Object> importOverview = calculateImportOverview(allImports);
        data.put("importOverview", importOverview);
        
        // Danh sách màu sắc cho biểu đồ
        List<String> chartColors = Arrays.asList(
            "#3B82F6", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6", 
            "#EC4899", "#06B6D4", "#22C55E", "#F97316", "#84CC16"
        );
        data.put("chartColors", chartColors);
        
        // Chi phí nhập hàng theo tháng (12 tháng gần nhất)
        Map<String, Double> monthlyImportCost = calculateMonthlyImportCost(allImports);
        data.put("monthlyImportCost", monthlyImportCost);
        
        // Tính thống kê tháng
        String highestMonth = "";
        Double highestCost = 0.0;
        String lowestMonth = "";
        Double lowestCost = 0.0;
        Double monthlyAverage = 0.0;
        
        if (!monthlyImportCost.isEmpty()) {
            // Tìm tháng cao nhất và thấp nhất
            for (Map.Entry<String, Double> entry : monthlyImportCost.entrySet()) {
                if (entry.getValue() > highestCost) {
                    highestCost = entry.getValue();
                    highestMonth = entry.getKey();
                }
                if (lowestCost == 0.0 || entry.getValue() < lowestCost) {
                    lowestCost = entry.getValue();
                    lowestMonth = entry.getKey();
                }
            }
            
            // Tính trung bình tháng
            Double totalMonthly = monthlyImportCost.values().stream()
                    .filter(Objects::nonNull)
                    .reduce(0.0, Double::sum);
            monthlyAverage = totalMonthly / monthlyImportCost.size();
        }
        
        data.put("highestMonth", highestMonth);
        data.put("highestCost", highestCost);
        data.put("lowestMonth", lowestMonth);
        data.put("lowestCost", lowestCost);
        data.put("monthlyAverage", monthlyAverage);
        
        // Tạo chuỗi hiển thị cho tháng cao nhất và thấp nhất
        String highestMonthDisplay = highestMonth + ": " + String.format("%,.0f", highestCost) + " VNĐ";
        String lowestMonthDisplay = lowestMonth + ": " + String.format("%,.0f", lowestCost) + " VNĐ";
        data.put("highestMonthDisplay", highestMonthDisplay);
        data.put("lowestMonthDisplay", lowestMonthDisplay);
        
        // Top sản phẩm nhập nhiều nhất
        Map<String, Object> topImportedProducts = calculateTopImportedProducts(allImports);
        data.put("topImportedProducts", topImportedProducts);
        
        // Thống kê số lượng nhập theo nhà cung cấp
        Map<String, Long> importsBySupplier = allImports.stream()
                .collect(Collectors.groupingBy(
                    importItem -> importItem.getNhaCungCap() != null ? importItem.getNhaCungCap().getTen() : "Không xác định",
                    Collectors.counting()
                ));
        data.put("importsBySupplier", importsBySupplier);
        
        // Thống kê chi phí nhập hàng theo người lập phiếu
        Map<String, BigDecimal> creatorCosts = allImports.stream()
                .collect(Collectors.groupingBy(
                    importItem -> importItem.getNguoiLapPhieu() != null ? importItem.getNguoiLapPhieu() : "Không xác định",
                    Collectors.reducing(BigDecimal.ZERO,
                        importItem -> importItem.getTongTien() != null ? importItem.getTongTien() : BigDecimal.ZERO,
                        BigDecimal::add)
                ));
        
        Map<String, Double> importCostByCreator = new HashMap<>();
        creatorCosts.forEach((key, value) -> importCostByCreator.put(key, value.doubleValue()));
        data.put("importCostByCreator", importCostByCreator);
        
        return data;
    }
    
    private Map<String, Double> calculateImportCostByTime(List<PhieuNhapHang> imports, String period, Integer year) {
        Map<String, Double> importCostByTime = new HashMap<>();
        
        if (year == null) {
            year = LocalDateTime.now().getYear();
        }
        
        if ("day".equals(period)) {
            // Chi phí nhập hàng theo ngày trong tháng hiện tại
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfMonth = LocalDateTime.now().withDayOfMonth(LocalDateTime.now().toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
            
            for (int day = 1; day <= LocalDateTime.now().getDayOfMonth(); day++) {
                LocalDateTime dayStart = startOfMonth.withDayOfMonth(day);
                LocalDateTime dayEnd = dayStart.withHour(23).withMinute(59).withSecond(59);
                
                BigDecimal dayCost = imports.stream()
                        .filter(importItem -> importItem.getNgayTao() != null && 
                                importItem.getNgayTao().isAfter(dayStart.minusSeconds(1)) && 
                                importItem.getNgayTao().isBefore(dayEnd.plusSeconds(1)))
                        .map(PhieuNhapHang::getTongTien)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                importCostByTime.put(String.valueOf(day), dayCost.doubleValue());
            }
        } else if ("month".equals(period)) {
            // Chi phí nhập hàng theo tháng trong năm
            for (int month = 1; month <= 12; month++) {
                LocalDateTime monthStart = LocalDateTime.of(year, month, 1, 0, 0, 0);
                LocalDateTime monthEnd = monthStart.withDayOfMonth(monthStart.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
                
                BigDecimal monthCost = imports.stream()
                        .filter(importItem -> importItem.getNgayTao() != null && 
                                importItem.getNgayTao().isAfter(monthStart.minusSeconds(1)) && 
                                importItem.getNgayTao().isBefore(monthEnd.plusSeconds(1)))
                        .map(PhieuNhapHang::getTongTien)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                String monthName = monthStart.format(DateTimeFormatter.ofPattern("MM/yyyy"));
                importCostByTime.put(monthName, monthCost.doubleValue());
            }
        } else if ("year".equals(period)) {
            // Chi phí nhập hàng theo năm (5 năm gần nhất)
            int currentYear = LocalDateTime.now().getYear();
            for (int y = currentYear - 4; y <= currentYear; y++) {
                LocalDateTime yearStart = LocalDateTime.of(y, 1, 1, 0, 0, 0);
                LocalDateTime yearEnd = LocalDateTime.of(y, 12, 31, 23, 59, 59);
                
                BigDecimal yearCost = imports.stream()
                        .filter(importItem -> importItem.getNgayTao() != null && 
                                importItem.getNgayTao().isAfter(yearStart.minusSeconds(1)) && 
                                importItem.getNgayTao().isBefore(yearEnd.plusSeconds(1)))
                        .map(PhieuNhapHang::getTongTien)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                importCostByTime.put(String.valueOf(y), yearCost.doubleValue());
            }
        }
        
        return importCostByTime;
    }
    
    private Map<String, Double> calculateImportCostBySupplier(List<PhieuNhapHang> imports) {
        Map<String, BigDecimal> supplierCosts = imports.stream()
                .collect(Collectors.groupingBy(
                    importItem -> importItem.getNhaCungCap() != null ? importItem.getNhaCungCap().getTen() : "Không xác định",
                    Collectors.reducing(BigDecimal.ZERO,
                        importItem -> importItem.getTongTien() != null ? importItem.getTongTien() : BigDecimal.ZERO,
                        BigDecimal::add)
                ));
        
        Map<String, Double> result = new HashMap<>();
        supplierCosts.forEach((key, value) -> result.put(key, value.doubleValue()));
        return result;
    }
    
    private Map<String, Object> calculateImportOverview(List<PhieuNhapHang> imports) {
        Map<String, Object> overview = new HashMap<>();
        
        
        // Thống kê cơ bản
        int totalImports = imports.size();
        double totalCost = imports.stream()
            .mapToDouble(importItem -> importItem.getTongTien() != null ? importItem.getTongTien().doubleValue() : 0.0)
            .sum();
        
        // Thống kê theo thời gian (thay thế cho trạng thái)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        LocalDateTime threeMonthsAgo = now.minusMonths(3);
        
        long recentImports = imports.stream()
            .filter(importItem -> importItem.getNgayTao() != null && importItem.getNgayTao().isAfter(oneMonthAgo))
            .count();
        long mediumImports = imports.stream()
            .filter(importItem -> importItem.getNgayTao() != null && 
                    importItem.getNgayTao().isAfter(threeMonthsAgo) && 
                    importItem.getNgayTao().isBefore(oneMonthAgo))
            .count();
        long oldImports = imports.stream()
            .filter(importItem -> importItem.getNgayTao() != null && importItem.getNgayTao().isBefore(threeMonthsAgo))
            .count();
        
        // Thống kê theo nhà cung cấp
        long uniqueSuppliers = imports.stream()
            .filter(importItem -> importItem.getNhaCungCap() != null)
            .map(importItem -> importItem.getNhaCungCap().getId())
            .distinct()
            .count();
        
        // Chi phí trung bình
        double averageCost = totalImports > 0 ? totalCost / totalImports : 0.0;
        
        // Dữ liệu cho biểu đồ tròn (theo thời gian)
        Map<String, Double> timeData = new HashMap<>();
        timeData.put("Gần đây (1 tháng)", (double) recentImports);
        timeData.put("Trung bình (1-3 tháng)", (double) mediumImports);
        timeData.put("Cũ hơn (3+ tháng)", (double) oldImports);
        
        overview.put("totalImports", totalImports);
        overview.put("totalCost", totalCost);
        overview.put("averageCost", averageCost);
        overview.put("uniqueSuppliers", uniqueSuppliers);
        overview.put("timeData", timeData);
        overview.put("recentImports", recentImports);
        overview.put("mediumImports", mediumImports);
        overview.put("oldImports", oldImports);
        
        
        return overview;
    }
    
    private Map<String, Double> calculateMonthlyImportCost(List<PhieuNhapHang> imports) {
        Map<String, Double> monthlyImportCost = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 11; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime monthEnd = monthStart.withDayOfMonth(monthStart.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
            
            BigDecimal monthCost = imports.stream()
                    .filter(importItem -> importItem.getNgayTao() != null && 
                            importItem.getNgayTao().isAfter(monthStart.minusSeconds(1)) && 
                            importItem.getNgayTao().isBefore(monthEnd.plusSeconds(1)))
                    .map(PhieuNhapHang::getTongTien)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            String monthLabel = monthStart.format(DateTimeFormatter.ofPattern("MM/yyyy"));
            monthlyImportCost.put(monthLabel, monthCost.doubleValue());
        }
        
        return monthlyImportCost;
    }
    
    private Map<String, Object> calculateTopImportedProducts(List<PhieuNhapHang> imports) {
        Map<SanPham, Long> productImports = new HashMap<>();
        
        for (PhieuNhapHang importItem : imports) {
            if (importItem.getChiTietPhieuNhaps() != null) {
                for (ChiTietPhieuNhapHang detail : importItem.getChiTietPhieuNhaps()) {
                    if (detail.getSanPhamBienThe() != null && 
                        detail.getSanPhamBienThe().getSanPham() != null) {
                        
                        SanPham product = detail.getSanPhamBienThe().getSanPham();
                        Long quantity = detail.getSoLuongNhap() != null ? detail.getSoLuongNhap().longValue() : 0L;
                        
                        productImports.merge(product, quantity, Long::sum);
                    }
                }
            }
        }
        
        List<Map.Entry<SanPham, Long>> topProducts = productImports.entrySet().stream()
                .sorted(Map.Entry.<SanPham, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());
        
        // Convert to simple format for JavaScript
        List<Map<String, Object>> productList = new ArrayList<>();
        for (Map.Entry<SanPham, Long> entry : topProducts) {
            Map<String, Object> productData = new HashMap<>();
            productData.put("ten", entry.getKey().getTen());
            productData.put("soLuong", entry.getValue());
            productList.add(productData);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("products", productList);
        result.put("totalProducts", productImports.size());
        
        return result;
    }
}
