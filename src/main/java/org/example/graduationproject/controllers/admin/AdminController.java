package org.example.graduationproject.controllers.admin;

import org.example.graduationproject.models.*;
import org.example.graduationproject.repositories.*;
import org.example.graduationproject.services.AbandonedCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private HoaDonRepository hoaDonRepository;
    
    @Autowired
    private SanPhamRepository sanPhamRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SanPhamBienTheRepository sanPhamBienTheRepository;
    
    @Autowired
    private GioHangRepository gioHangRepository;
    
    @Autowired
    private AbandonedCartService abandonedCartService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "admin";
        model.addAttribute("username", username);
        model.addAttribute("currentPage", "dashboard");

        // Thống kê tổng quan
        long totalProducts = sanPhamRepository.count();
        long totalUsers = userRepository.count();
        long totalOrders = hoaDonRepository.count();
        long totalProductVariants = sanPhamBienTheRepository.count();
        
        // Thống kê doanh thu
        List<HoaDon> allOrders = hoaDonRepository.findAll();
        
        // Debug: In ra các trạng thái đơn hàng để kiểm tra
        System.out.println("=== DEBUG: Trạng thái đơn hàng ===");
        allOrders.stream()
                .collect(Collectors.groupingBy(HoaDon::getTrangThai, Collectors.counting()))
                .forEach((status, count) -> System.out.println("Trạng thái: " + status + " - Số lượng: " + count));
        
        // Tính tổng doanh thu từ đơn hàng COMPLETED
        BigDecimal totalRevenue = allOrders.stream()
                .filter(order -> "COMPLETED".equalsIgnoreCase(order.getTrangThai()))
                .map(order -> order.getTongTienSauGiamGia() != null ? order.getTongTienSauGiamGia() : order.getTongTien())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Debug: In ra thông tin doanh thu
        System.out.println("Tổng doanh thu từ đơn COMPLETED: " + totalRevenue);
        System.out.println("Số đơn COMPLETED: " + allOrders.stream().filter(order -> "COMPLETED".equalsIgnoreCase(order.getTrangThai())).count());
        
        // Doanh thu tháng này
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        BigDecimal monthlyRevenue = allOrders.stream()
                .filter(order -> order.getNgayTao() != null && order.getNgayTao().isAfter(startOfMonth))
                .filter(order -> "COMPLETED".equalsIgnoreCase(order.getTrangThai()))
                .map(order -> order.getTongTienSauGiamGia() != null ? order.getTongTienSauGiamGia() : order.getTongTien())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Đơn hàng tháng này
        long monthlyOrders = allOrders.stream()
                .filter(order -> order.getNgayTao() != null && order.getNgayTao().isAfter(startOfMonth))
                .count();
        
        // Sản phẩm bán chạy (top 5)
        Map<SanPham, Long> productSales = allOrders.stream()
                .flatMap(order -> order.getChiTietHoaDons().stream())
                .collect(Collectors.groupingBy(
                    chiTiet -> chiTiet.getSanPhamBienThe().getSanPham(),
                    Collectors.summingLong(ChiTietHoaDon::getSoLuong)
                ));
        
        List<Map.Entry<SanPham, Long>> topProducts = productSales.entrySet().stream()
                .sorted(Map.Entry.<SanPham, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());
        
        
        // Thống kê giỏ hàng với dữ liệu chi tiết
        Map<String, Object> cartStats = abandonedCartService.getAbandonedCartStats();
        long activeCarts = (Long) cartStats.get("totalActiveCarts");
        long abandonedCarts = (Long) cartStats.get("totalAbandonedCarts");
        long potentialAbandonedCarts = (Long) cartStats.get("potentialAbandonedCarts");
        double abandonmentRate = (Double) cartStats.get("abandonmentRate");
        
        // Thêm dữ liệu vào model
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalProductVariants", totalProductVariants);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("monthlyOrders", monthlyOrders);
        model.addAttribute("topProducts", topProducts);
        model.addAttribute("activeCarts", activeCarts);
        model.addAttribute("abandonedCarts", abandonedCarts);
        model.addAttribute("potentialAbandonedCarts", potentialAbandonedCarts);
        model.addAttribute("abandonmentRate", abandonmentRate);
        
        return "admin/dashboard";
    }
}

