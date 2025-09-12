package org.example.graduationproject.controllers.admin;

import org.example.graduationproject.models.*;
import org.example.graduationproject.repositories.*;
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
public class RevenueController {

    @Autowired
    private HoaDonRepository hoaDonRepository;
    
    @Autowired
    private ChiTietHoaDonRepository chiTietHoaDonRepository;
    
    @Autowired
    private SanPhamRepository sanPhamRepository;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/revenue")
    public String revenuePage(Model model, 
                             @RequestParam(value = "period", defaultValue = "month") String period,
                             @RequestParam(value = "year", required = false) Integer year) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "admin";
        model.addAttribute("username", username);
        model.addAttribute("currentPage", "revenue");
        model.addAttribute("selectedPeriod", period);
        model.addAttribute("selectedYear", year);

        // Lấy tất cả đơn hàng
        List<HoaDon> allOrders = hoaDonRepository.findAll();
        
        // Tính toán dữ liệu thống kê
        Map<String, Object> revenueData = calculateRevenueData(allOrders, period, year);
        
        // Thêm dữ liệu vào model
        model.addAllAttributes(revenueData);
        
        return "admin/revenue";
    }
    
    private Map<String, Object> calculateRevenueData(List<HoaDon> allOrders, String period, Integer year) {
        Map<String, Object> data = new HashMap<>();
        
        // Lọc đơn hàng theo trạng thái COMPLETED
        List<HoaDon> completedOrders = allOrders.stream()
                .filter(order -> "COMPLETED".equalsIgnoreCase(order.getTrangThai()))
                .collect(Collectors.toList());
        
        // Tổng doanh thu
        BigDecimal totalRevenue = completedOrders.stream()
                .map(order -> order.getTongTienSauGiamGia() != null ? order.getTongTienSauGiamGia() : order.getTongTien())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        data.put("totalRevenue", totalRevenue);
        data.put("totalOrders", (long) completedOrders.size());
        
        // Tính giá trị đơn hàng trung bình
        BigDecimal averageOrderValue = BigDecimal.ZERO;
        if (completedOrders.size() > 0) {
            averageOrderValue = totalRevenue.divide(new BigDecimal(completedOrders.size()), 0, RoundingMode.HALF_UP);
        }
        data.put("averageOrderValue", averageOrderValue);
        
        // Doanh thu theo thời gian
        Map<String, BigDecimal> revenueByTime = calculateRevenueByTime(completedOrders, period, year);
        data.put("revenueByTime", revenueByTime);
        
        // Tính tổng doanh thu kỳ và trung bình kỳ
        BigDecimal periodTotalRevenue = revenueByTime.values().stream()
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal periodAverageRevenue = BigDecimal.ZERO;
        if (!revenueByTime.isEmpty()) {
            periodAverageRevenue = periodTotalRevenue.divide(new BigDecimal(revenueByTime.size()), 0, RoundingMode.HALF_UP);
        }
        data.put("periodTotalRevenue", periodTotalRevenue);
        data.put("periodAverageRevenue", periodAverageRevenue);
        
        // Doanh thu theo danh mục
        Map<String, BigDecimal> revenueByCategory = calculateRevenueByCategory(completedOrders);
        data.put("revenueByCategory", revenueByCategory);
        
        // Danh sách màu sắc cho biểu đồ
        List<String> chartColors = Arrays.asList(
            "#3B82F6", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6", 
            "#EC4899", "#06B6D4", "#22C55E", "#F97316", "#84CC16"
        );
        data.put("chartColors", chartColors);
        
        // Doanh thu theo tháng (12 tháng gần nhất)
        Map<String, BigDecimal> monthlyRevenue = calculateMonthlyRevenue(completedOrders);
        data.put("monthlyRevenue", monthlyRevenue);
        
        // Tính thống kê tháng
        String highestMonth = "";
        BigDecimal highestRevenue = BigDecimal.ZERO;
        String lowestMonth = "";
        BigDecimal lowestRevenue = BigDecimal.ZERO;
        BigDecimal monthlyAverage = BigDecimal.ZERO;
        
        if (!monthlyRevenue.isEmpty()) {
            // Tìm tháng cao nhất và thấp nhất
            for (Map.Entry<String, BigDecimal> entry : monthlyRevenue.entrySet()) {
                if (entry.getValue().compareTo(highestRevenue) > 0) {
                    highestRevenue = entry.getValue();
                    highestMonth = entry.getKey();
                }
                if (lowestRevenue.equals(BigDecimal.ZERO) || entry.getValue().compareTo(lowestRevenue) < 0) {
                    lowestRevenue = entry.getValue();
                    lowestMonth = entry.getKey();
                }
            }
            
            // Tính trung bình tháng
            BigDecimal totalMonthly = monthlyRevenue.values().stream()
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthlyAverage = totalMonthly.divide(new BigDecimal(monthlyRevenue.size()), 0, RoundingMode.HALF_UP);
        }
        
        data.put("highestMonth", highestMonth);
        data.put("highestRevenue", highestRevenue);
        data.put("lowestMonth", lowestMonth);
        data.put("lowestRevenue", lowestRevenue);
        data.put("monthlyAverage", monthlyAverage);
        
        // Tạo chuỗi hiển thị cho tháng cao nhất và thấp nhất
        String highestMonthDisplay = highestMonth + ": " + String.format("%,.0f", highestRevenue) + " VNĐ";
        String lowestMonthDisplay = lowestMonth + ": " + String.format("%,.0f", lowestRevenue) + " VNĐ";
        data.put("highestMonthDisplay", highestMonthDisplay);
        data.put("lowestMonthDisplay", lowestMonthDisplay);
        
        // Top sản phẩm bán chạy
        Map<String, Object> topProducts = calculateTopProducts(completedOrders);
        data.put("topProducts", topProducts);
        
        // Thống kê đơn hàng theo trạng thái
        Map<String, Long> ordersByStatus = allOrders.stream()
                .collect(Collectors.groupingBy(
                    order -> order.getTrangThai() != null ? order.getTrangThai() : "UNKNOWN",
                    Collectors.counting()
                ));
        data.put("ordersByStatus", ordersByStatus);
        
        // Doanh thu theo phương thức thanh toán
        Map<String, BigDecimal> revenueByPaymentMethod = calculateRevenueByPaymentMethod(completedOrders);
        data.put("revenueByPaymentMethod", revenueByPaymentMethod);
        
        return data;
    }
    
    private Map<String, BigDecimal> calculateRevenueByTime(List<HoaDon> orders, String period, Integer year) {
        Map<String, BigDecimal> revenueByTime = new HashMap<>();
        
        if (year == null) {
            year = LocalDateTime.now().getYear();
        }
        
        if ("day".equals(period)) {
            // Doanh thu theo ngày trong tháng hiện tại
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfMonth = LocalDateTime.now().withDayOfMonth(LocalDateTime.now().toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
            
            for (int day = 1; day <= LocalDateTime.now().getDayOfMonth(); day++) {
                LocalDateTime dayStart = startOfMonth.withDayOfMonth(day);
                LocalDateTime dayEnd = dayStart.withHour(23).withMinute(59).withSecond(59);
                
                BigDecimal dayRevenue = orders.stream()
                        .filter(order -> order.getNgayTao() != null && 
                                order.getNgayTao().isAfter(dayStart.minusSeconds(1)) && 
                                order.getNgayTao().isBefore(dayEnd.plusSeconds(1)))
                        .map(order -> order.getTongTienSauGiamGia() != null ? order.getTongTienSauGiamGia() : order.getTongTien())
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                revenueByTime.put(String.valueOf(day), dayRevenue);
            }
        } else if ("month".equals(period)) {
            // Doanh thu theo tháng trong năm
            for (int month = 1; month <= 12; month++) {
                LocalDateTime monthStart = LocalDateTime.of(year, month, 1, 0, 0, 0);
                LocalDateTime monthEnd = monthStart.withDayOfMonth(monthStart.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
                
                BigDecimal monthRevenue = orders.stream()
                        .filter(order -> order.getNgayTao() != null && 
                                order.getNgayTao().isAfter(monthStart.minusSeconds(1)) && 
                                order.getNgayTao().isBefore(monthEnd.plusSeconds(1)))
                        .map(order -> order.getTongTienSauGiamGia() != null ? order.getTongTienSauGiamGia() : order.getTongTien())
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                String monthName = monthStart.format(DateTimeFormatter.ofPattern("MM/yyyy"));
                revenueByTime.put(monthName, monthRevenue);
            }
        } else if ("year".equals(period)) {
            // Doanh thu theo năm (5 năm gần nhất)
            int currentYear = LocalDateTime.now().getYear();
            for (int y = currentYear - 4; y <= currentYear; y++) {
                LocalDateTime yearStart = LocalDateTime.of(y, 1, 1, 0, 0, 0);
                LocalDateTime yearEnd = LocalDateTime.of(y, 12, 31, 23, 59, 59);
                
                BigDecimal yearRevenue = orders.stream()
                        .filter(order -> order.getNgayTao() != null && 
                                order.getNgayTao().isAfter(yearStart.minusSeconds(1)) && 
                                order.getNgayTao().isBefore(yearEnd.plusSeconds(1)))
                        .map(order -> order.getTongTienSauGiamGia() != null ? order.getTongTienSauGiamGia() : order.getTongTien())
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                revenueByTime.put(String.valueOf(y), yearRevenue);
            }
        }
        
        return revenueByTime;
    }
    
    private Map<String, BigDecimal> calculateRevenueByCategory(List<HoaDon> orders) {
        return orders.stream()
                .flatMap(order -> order.getChiTietHoaDons().stream())
                .collect(Collectors.groupingBy(
                    chiTiet -> chiTiet.getSanPhamBienThe().getSanPham().getLoai().getTen(),
                    Collectors.reducing(BigDecimal.ZERO, 
                        chiTiet -> chiTiet.getThanhTien() != null ? chiTiet.getThanhTien() : BigDecimal.ZERO,
                        BigDecimal::add)
                ));
    }
    
    private Map<String, BigDecimal> calculateMonthlyRevenue(List<HoaDon> orders) {
        Map<String, BigDecimal> monthlyRevenue = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 11; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime monthEnd = monthStart.withDayOfMonth(monthStart.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
            
            BigDecimal monthRevenue = orders.stream()
                    .filter(order -> order.getNgayTao() != null && 
                            order.getNgayTao().isAfter(monthStart.minusSeconds(1)) && 
                            order.getNgayTao().isBefore(monthEnd.plusSeconds(1)))
                    .map(order -> order.getTongTienSauGiamGia() != null ? order.getTongTienSauGiamGia() : order.getTongTien())
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            String monthLabel = monthStart.format(DateTimeFormatter.ofPattern("MM/yyyy"));
            monthlyRevenue.put(monthLabel, monthRevenue);
        }
        
        return monthlyRevenue;
    }
    
    private Map<String, Object> calculateTopProducts(List<HoaDon> orders) {
        Map<SanPham, Long> productSales = orders.stream()
                .flatMap(order -> order.getChiTietHoaDons().stream())
                .collect(Collectors.groupingBy(
                    chiTiet -> chiTiet.getSanPhamBienThe().getSanPham(),
                    Collectors.summingLong(ChiTietHoaDon::getSoLuong)
                ));
        
        List<Map.Entry<SanPham, Long>> topProducts = productSales.entrySet().stream()
                .sorted(Map.Entry.<SanPham, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("products", topProducts);
        result.put("totalProducts", productSales.size());
        
        return result;
    }
    
    private Map<String, BigDecimal> calculateRevenueByPaymentMethod(List<HoaDon> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(
                    order -> order.getLoaiThanhToan() != null ? order.getLoaiThanhToan() : "UNKNOWN",
                    Collectors.reducing(BigDecimal.ZERO,
                        order -> order.getTongTienSauGiamGia() != null ? order.getTongTienSauGiamGia() : order.getTongTien(),
                        (a, b) -> a.add(b != null ? b : BigDecimal.ZERO))
                ));
    }
}
