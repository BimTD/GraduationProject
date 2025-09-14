package org.example.graduationproject.services.impl;

import org.example.graduationproject.dto.CheckoutDTO;
import org.example.graduationproject.dto.OrderResponseDTO;
import org.example.graduationproject.dto.CancelOrderDTO;
import org.example.graduationproject.exceptions.AuthenticationException;
import org.example.graduationproject.exceptions.ResourceNotFoundException;
import org.example.graduationproject.exceptions.ValidationException;
import org.example.graduationproject.models.*;
import org.example.graduationproject.repositories.ChiTietHoaDonRepository;
import org.example.graduationproject.repositories.HoaDonRepository;
import org.example.graduationproject.repositories.SanPhamBienTheRepository;
import org.example.graduationproject.services.AuthenticationService;
import org.example.graduationproject.services.GioHangService;
import org.example.graduationproject.services.HoaDonService;
import org.example.graduationproject.services.MaGiamGiaService;
import org.example.graduationproject.services.LichSuSuDungMaGiamGiaService;
import org.example.graduationproject.services.NotificationService;
import org.example.graduationproject.analytics.services.RFMAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class HoaDonServiceImpl implements HoaDonService {

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private ChiTietHoaDonRepository chiTietHoaDonRepository;

    @Autowired
    private GioHangService gioHangService;

    @Autowired
    private SanPhamBienTheRepository sanPhamBienTheRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MaGiamGiaService maGiamGiaService;
    
    @Autowired
    private LichSuSuDungMaGiamGiaService lichSuSuDungMaGiamGiaService;

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private RFMAnalysisService rfmAnalysisService;

    @Override
    @Transactional
    public HoaDon createOrderFromCart(User user, CheckoutDTO checkoutDTO) {
        // Lấy giỏ hàng active của user
        GioHang activeCart = gioHangService.getActiveCart(user);
        if (activeCart == null || activeCart.getChiTietGioHangs() == null || activeCart.getChiTietGioHangs().isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể tạo đơn hàng");
        }

        // Tạo hóa đơn mới
        HoaDon hoaDon = new HoaDon();
        hoaDon.setUser(user);
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setGhiChu(checkoutDTO.getGhiChu());
        hoaDon.setTrangThai("PENDING"); // PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED
        hoaDon.setLoaiThanhToan(checkoutDTO.getLoaiThanhToan());
        hoaDon.setDaLayTien("NO"); // NO, YES
        
        // Lưu thông tin địa chỉ giao hàng
        hoaDon.setDiaChiGiaoHang(checkoutDTO.getDiaChiGiaoHang());
        hoaDon.setTenNguoiNhan(checkoutDTO.getTenNguoiNhan());
        hoaDon.setSoDienThoaiGiaoHang(checkoutDTO.getSoDienThoai());
        
        // Tính tổng tiền
        BigDecimal tongTien = checkoutDTO.getTongTien() != null ? checkoutDTO.getTongTien() : 
            activeCart.getChiTietGioHangs().stream()
                .map(ChiTietGioHang::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Cộng phí giao hàng
        if (checkoutDTO.getPhiGiaoHang() != null) {
            tongTien = tongTien.add(checkoutDTO.getPhiGiaoHang());
        }
        
        hoaDon.setTongTien(tongTien);
        
        // Xử lý mã giảm giá (hỗ trợ nhiều mã)
        BigDecimal giaTriGiamGia = checkoutDTO.getGiaTriGiamGia() != null ? checkoutDTO.getGiaTriGiamGia() : BigDecimal.ZERO;
        BigDecimal tongTienSauGiamGia = checkoutDTO.getTongTienSauGiamGia() != null ? checkoutDTO.getTongTienSauGiamGia() : tongTien;
        MaGiamGia maGiamGia = null;
        String maGiamGiaSuDung = "";
        
        // Xử lý nhiều mã giảm giá (ưu tiên)
        if (checkoutDTO.getMaGiamGiaList() != null && checkoutDTO.getMaGiamGiaList().length > 0) {
            StringBuilder maGiamGiaBuilder = new StringBuilder();
            BigDecimal totalDiscount = BigDecimal.ZERO;
            List<MaGiamGia> usedMaGiamGiaList = new ArrayList<>();
            List<BigDecimal> usedDiscountAmounts = new ArrayList<>();
            
            for (String code : checkoutDTO.getMaGiamGiaList()) {
                if (code != null && !code.trim().isEmpty()) {
                    try {
                        // Lấy thông tin mã giảm giá
                        MaGiamGia discountCode = maGiamGiaService.getMaGiamGiaByCode(code.trim());
                        
                        // Tính giá trị giảm giá cho mã này
                        BigDecimal discountAmount = maGiamGiaService.calculateDiscountAmount(code.trim(), tongTien, null);
                        totalDiscount = totalDiscount.add(discountAmount);
                        
                        // Áp dụng mã giảm giá (tăng số lần sử dụng)
                        maGiamGiaService.applyMaGiamGia(code.trim());
                        
                        // Lưu thông tin để tạo lịch sử
                        usedMaGiamGiaList.add(discountCode);
                        usedDiscountAmounts.add(discountAmount);
                        
                        // Thêm vào danh sách
                        if (maGiamGiaBuilder.length() > 0) {
                            maGiamGiaBuilder.append(",");
                        }
                        maGiamGiaBuilder.append(code.trim());
                        
                        // Lưu mã đầu tiên làm mã chính (để tương thích)
                        if (maGiamGia == null) {
                            maGiamGia = discountCode;
                        }
                        
                    } catch (Exception e) {
                        // Nếu mã giảm giá không hợp lệ, bỏ qua và tiếp tục
                        System.out.println("Mã giảm giá không hợp lệ: " + code + " - " + e.getMessage());
                    }
                }
            }
            
            maGiamGiaSuDung = maGiamGiaBuilder.toString();
            giaTriGiamGia = totalDiscount;
            tongTienSauGiamGia = tongTien.subtract(totalDiscount);
            
            // Lưu lịch sử sử dụng mã giảm giá (sẽ lưu sau khi có ID hóa đơn)
            if (!usedMaGiamGiaList.isEmpty()) {
                // Tạm thời lưu vào biến để sử dụng sau khi lưu hóa đơn
                hoaDon.setMaGiamGiaList(usedMaGiamGiaList);
                hoaDon.setGiaTriGiamGiaList(usedDiscountAmounts);
            }
            
        } else if (checkoutDTO.getMaGiamGia() != null && !checkoutDTO.getMaGiamGia().trim().isEmpty()) {
            // Xử lý 1 mã giảm giá (tương thích ngược)
            try {
                // Lấy thông tin mã giảm giá
                maGiamGia = maGiamGiaService.getMaGiamGiaByCode(checkoutDTO.getMaGiamGia());
                
                // Tính giá trị giảm giá
                BigDecimal discountAmount = maGiamGiaService.calculateDiscountAmount(checkoutDTO.getMaGiamGia(), tongTien, null);
                giaTriGiamGia = discountAmount;
                tongTienSauGiamGia = tongTien.subtract(discountAmount);
                
                // Áp dụng mã giảm giá (tăng số lần sử dụng)
                maGiamGiaService.applyMaGiamGia(checkoutDTO.getMaGiamGia());
                
                maGiamGiaSuDung = checkoutDTO.getMaGiamGia();
                
                // Lưu lịch sử sử dụng mã giảm giá (sẽ lưu sau khi có ID hóa đơn)
                hoaDon.setMaGiamGiaList(List.of(maGiamGia));
                hoaDon.setGiaTriGiamGiaList(List.of(discountAmount));
                
            } catch (Exception e) {
                // Nếu mã giảm giá không hợp lệ, bỏ qua và tiếp tục
                System.out.println("Mã giảm giá không hợp lệ: " + e.getMessage());
            }
        }
        
        // Lưu thông tin mã giảm giá vào hóa đơn
        hoaDon.setMaGiamGiaSuDung(maGiamGiaSuDung);
        hoaDon.setGiaTriGiamGia(giaTriGiamGia);
        hoaDon.setTongTienSauGiamGia(tongTienSauGiamGia);
        hoaDon.setMaGiamGia(maGiamGia);
        
        // Lưu hóa đơn
        hoaDon = hoaDonRepository.save(hoaDon);
        
        // Lưu lịch sử sử dụng mã giảm giá
        if (hoaDon.getMaGiamGiaList() != null && !hoaDon.getMaGiamGiaList().isEmpty()) {
            try {
                lichSuSuDungMaGiamGiaService.saveMultipleUsageHistory(
                    user, 
                    hoaDon.getMaGiamGiaList(), 
                    hoaDon.getGiaTriGiamGiaList(), 
                    hoaDon.getId().longValue()
                );
                System.out.println("Đã lưu lịch sử sử dụng " + hoaDon.getMaGiamGiaList().size() + " mã giảm giá cho đơn hàng " + hoaDon.getId());
            } catch (Exception e) {
                System.err.println("Lỗi khi lưu lịch sử sử dụng mã giảm giá: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Tạo chi tiết hóa đơn
        List<ChiTietHoaDon> chiTietHoaDons = new ArrayList<>();
        for (ChiTietGioHang cartItem : activeCart.getChiTietGioHangs()) {
            ChiTietHoaDon chiTietHoaDon = new ChiTietHoaDon();
            chiTietHoaDon.setHoaDon(hoaDon);
            chiTietHoaDon.setSanPhamBienThe(cartItem.getSanPhamBienThe());
            chiTietHoaDon.setSoLuong(cartItem.getSoLuong());
            chiTietHoaDon.setThanhTien(cartItem.getThanhTien());
            
            chiTietHoaDons.add(chiTietHoaDon);
        }
        
        // Lưu chi tiết hóa đơn
        chiTietHoaDonRepository.saveAll(chiTietHoaDons);
        
        // Trừ tồn kho ngay khi tạo đơn hàng (PENDING)
        Map<Integer, Integer> variantIdToRequiredQty = new HashMap<>();
        for (ChiTietHoaDon cthd : chiTietHoaDons) {
            if (cthd.getSanPhamBienThe() == null) continue;
            Integer variantId = cthd.getSanPhamBienThe().getId();
            if (variantId == null) continue;
            int required = cthd.getSoLuong() != null ? cthd.getSoLuong() : 0;
            variantIdToRequiredQty.merge(variantId, required, Integer::sum);
        }

        // Kiểm tra và trừ tồn kho
        List<org.example.graduationproject.models.SanPhamBienThe> toUpdate = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : variantIdToRequiredQty.entrySet()) {
            org.example.graduationproject.models.SanPhamBienThe variant = sanPhamBienTheRepository.findById(entry.getKey()).orElse(null);
            if (variant == null) {
                throw new RuntimeException("Sản phẩm không tồn tại");
            }
            int stock = variant.getSoLuongTon() != null ? variant.getSoLuongTon() : 0;
            int need = entry.getValue() != null ? entry.getValue() : 0;
            if (stock < need) {
                throw new RuntimeException("Không đủ hàng cho sản phẩm: " + variant.getSanPham().getTen());
            }
            variant.setSoLuongTon(stock - need);
            toUpdate.add(variant);
        }

        // Cập nhật tồn kho
        if (!toUpdate.isEmpty()) {
            sanPhamBienTheRepository.saveAll(toUpdate);
        }
        
        // Cập nhật trạng thái giỏ hàng thành "ordered"
        activeCart.setTrangThai("ordered");
        activeCart.setNgayCapNhat(LocalDateTime.now());
        gioHangService.updateCartStatus(activeCart);

        // Tạo thông báo cho admin khi có đơn hàng mới
        String adminTitle = "Đơn hàng mới #" + hoaDon.getId();
        String adminMessage = "Khách hàng " + user.getHoTen() + " vừa đặt đơn hàng #" + hoaDon.getId() + 
                            " với tổng tiền " + hoaDon.getTongTien() + " VNĐ";
        notificationService.createAdminNotification(adminTitle, adminMessage, "ORDER_CREATED", hoaDon.getId());
        
        // Tạo thông báo cho user
        String userTitle = "Đặt hàng thành công";
        String userMessage = "Đơn hàng #" + hoaDon.getId() + " của bạn đã được tạo thành công. " +
                           "Chúng tôi sẽ xử lý đơn hàng trong thời gian sớm nhất.";
        notificationService.createNotification(userTitle, userMessage, "ORDER_CREATED", user, hoaDon.getId());
        
        // Gửi thông báo real-time cho user
        notificationService.sendNotificationToUser(user, userTitle, userMessage, "ORDER_CREATED", hoaDon.getId());

        return hoaDon;
    }

    @Override
    public List<HoaDon> getUserOrders(User user) {
        return hoaDonRepository.findByUserOrderByNgayTaoDesc(user);
    }

    @Override
    public HoaDon getOrderById(Integer orderId) {
        return hoaDonRepository.findById(orderId).orElse(null);
    }

    @Override
    public HoaDon getUserOrderById(User user, Integer orderId) {
        HoaDon hoaDon = hoaDonRepository.findById(orderId).orElse(null);
        if (hoaDon != null && hoaDon.getUser().getId().equals(user.getId())) {
            return hoaDon;
        }
        return null;
    }

    @Override
    @Transactional
    public boolean updateOrderStatus(Integer orderId, String newStatus) {
        HoaDon hoaDon = hoaDonRepository.findById(orderId).orElse(null);
        if (hoaDon != null) {
            String currentStatus = hoaDon.getTrangThai();
            String oldStatus = currentStatus;

            // Không cho phép thay đổi trạng thái nếu đã COMPLETED hoặc CANCELLED
            if ("COMPLETED".equalsIgnoreCase(currentStatus) || "CANCELLED".equalsIgnoreCase(currentStatus)) {
                return false; // Đơn đã hoàn thành hoặc đã hủy, không thể thay đổi
            }

            // Nếu chuyển sang CANCELLED từ bất kỳ trạng thái nào (trừ COMPLETED)
            if ("CANCELLED".equalsIgnoreCase(newStatus)) {
                // Hoàn lại tồn kho
                List<ChiTietHoaDon> chiTietList = chiTietHoaDonRepository.findByHoaDon(hoaDon);
                
                // Gom số lượng theo từng biến thể để cộng lại
                Map<Integer, Integer> variantIdToReturnQty = new HashMap<>();
                for (ChiTietHoaDon cthd : chiTietList) {
                    if (cthd.getSanPhamBienThe() == null) continue;
                    Integer variantId = cthd.getSanPhamBienThe().getId();
                    if (variantId == null) continue;
                    int returnQty = cthd.getSoLuong() != null ? cthd.getSoLuong() : 0;
                    variantIdToReturnQty.merge(variantId, returnQty, Integer::sum);
                }

                // Cộng lại tồn kho
                List<org.example.graduationproject.models.SanPhamBienThe> toUpdate = new ArrayList<>();
                for (Map.Entry<Integer, Integer> entry : variantIdToReturnQty.entrySet()) {
                    org.example.graduationproject.models.SanPhamBienThe variant = sanPhamBienTheRepository.findById(entry.getKey()).orElse(null);
                    if (variant != null) {
                        int currentStock = variant.getSoLuongTon() != null ? variant.getSoLuongTon() : 0;
                        int returnQty = entry.getValue() != null ? entry.getValue() : 0;
                        variant.setSoLuongTon(currentStock + returnQty);
                        toUpdate.add(variant);
                    }
                }

                // Cập nhật tồn kho
                if (!toUpdate.isEmpty()) {
                    sanPhamBienTheRepository.saveAll(toUpdate);
                }
            }

            hoaDon.setTrangThai(newStatus);
            
            // Cập nhật trường daLayTien khi trạng thái là COMPLETED
            if ("COMPLETED".equalsIgnoreCase(newStatus)) {
                hoaDon.setDaLayTien("YES");
                
                // Cập nhật RFM analysis khi đơn hàng hoàn thành
                try {
                    rfmAnalysisService.calculateRFMForUser(hoaDon.getUser());
                    System.out.println("RFM analysis updated for user: " + hoaDon.getUser().getId());
                } catch (Exception e) {
                    System.err.println("Error updating RFM analysis: " + e.getMessage());
                }
            }
            
            hoaDonRepository.save(hoaDon);
            
            // Gửi thông báo cho user khi admin thay đổi trạng thái
            String userTitle = "Cập nhật đơn hàng #" + orderId;
            String userMessage = "Trạng thái đơn hàng #" + orderId + " đã được cập nhật từ " + 
                               oldStatus + " thành " + newStatus;
            notificationService.createNotification(userTitle, userMessage, "ORDER_STATUS_CHANGED", 
                                                 hoaDon.getUser(), orderId);
            
            // Gửi thông báo real-time cho user
            notificationService.sendNotificationToUser(hoaDon.getUser(), userTitle, userMessage, 
                                                     "ORDER_STATUS_CHANGED", orderId);
            
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean updateOrderStatusAndRestoreStock(Integer orderId, String newStatus) {
        HoaDon hoaDon = hoaDonRepository.findById(orderId).orElse(null);
        if (hoaDon != null) {
            String currentStatus = hoaDon.getTrangThai();

            // Nếu chuyển từ CONFIRMED sang CANCELLED thì hoàn lại tồn kho
            if ("CONFIRMED".equalsIgnoreCase(currentStatus) && "CANCELLED".equalsIgnoreCase(newStatus)) {
                List<ChiTietHoaDon> chiTietList = chiTietHoaDonRepository.findByHoaDon(hoaDon);

                // Gom số lượng theo từng biến thể để hoàn lại tồn kho
                Map<Integer, Integer> variantIdToRestoreQty = new HashMap<>();
                for (ChiTietHoaDon cthd : chiTietList) {
                    if (cthd.getSanPhamBienThe() == null) continue;
                    Integer variantId = cthd.getSanPhamBienThe().getId();
                    if (variantId == null) continue;
                    int restore = cthd.getSoLuong() != null ? cthd.getSoLuong() : 0;
                    variantIdToRestoreQty.merge(variantId, restore, Integer::sum);
                }

                // Hoàn lại tồn kho
                List<org.example.graduationproject.models.SanPhamBienThe> toUpdate = new ArrayList<>();
                for (Map.Entry<Integer, Integer> entry : variantIdToRestoreQty.entrySet()) {
                    org.example.graduationproject.models.SanPhamBienThe variant = sanPhamBienTheRepository.findById(entry.getKey()).orElse(null);
                    if (variant != null) {
                        int stock = variant.getSoLuongTon() != null ? variant.getSoLuongTon() : 0;
                        int restore = entry.getValue() != null ? entry.getValue() : 0;
                        variant.setSoLuongTon(stock + restore);
                        toUpdate.add(variant);
                    }
                }

                // Cập nhật tồn kho
                if (!toUpdate.isEmpty()) {
                    sanPhamBienTheRepository.saveAll(toUpdate);
                }
            }

            hoaDon.setTrangThai(newStatus);
            
            // Cập nhật trường daLayTien khi trạng thái là COMPLETED
            if ("COMPLETED".equalsIgnoreCase(newStatus)) {
                hoaDon.setDaLayTien("YES");
            }
            
            hoaDonRepository.save(hoaDon);
            
            // Gửi thông báo cho user khi admin thay đổi trạng thái
            String userTitle = "Cập nhật đơn hàng #" + orderId;
            String userMessage = "Trạng thái đơn hàng #" + orderId + " đã được cập nhật thành " + newStatus;
            notificationService.createNotification(userTitle, userMessage, "ORDER_STATUS_CHANGED", 
                                                 hoaDon.getUser(), orderId);
            
            // Gửi thông báo real-time cho user
            notificationService.sendNotificationToUser(hoaDon.getUser(), userTitle, userMessage, 
                                                     "ORDER_STATUS_CHANGED", orderId);
            
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean cancelOrder(User user, Integer orderId) {
        HoaDon hoaDon = getUserOrderById(user, orderId);
        if (hoaDon != null && "PENDING".equals(hoaDon.getTrangThai())) {
            // Hoàn lại tồn kho khi user hủy đơn
            List<ChiTietHoaDon> chiTietList = chiTietHoaDonRepository.findByHoaDon(hoaDon);
            
            // Gom số lượng theo từng biến thể để cộng lại
            Map<Integer, Integer> variantIdToReturnQty = new HashMap<>();
            for (ChiTietHoaDon cthd : chiTietList) {
                if (cthd.getSanPhamBienThe() == null) continue;
                Integer variantId = cthd.getSanPhamBienThe().getId();
                if (variantId == null) continue;
                int returnQty = cthd.getSoLuong() != null ? cthd.getSoLuong() : 0;
                variantIdToReturnQty.merge(variantId, returnQty, Integer::sum);
            }

            // Cộng lại tồn kho
            List<org.example.graduationproject.models.SanPhamBienThe> toUpdate = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : variantIdToReturnQty.entrySet()) {
                org.example.graduationproject.models.SanPhamBienThe variant = sanPhamBienTheRepository.findById(entry.getKey()).orElse(null);
                if (variant != null) {
                    int currentStock = variant.getSoLuongTon() != null ? variant.getSoLuongTon() : 0;
                    int returnQty = entry.getValue() != null ? entry.getValue() : 0;
                    variant.setSoLuongTon(currentStock + returnQty);
                    toUpdate.add(variant);
                }
            }

            // Cập nhật tồn kho
            if (!toUpdate.isEmpty()) {
                sanPhamBienTheRepository.saveAll(toUpdate);
            }
            
            hoaDon.setTrangThai("CANCELLED");
            hoaDonRepository.save(hoaDon);
            
            // Gửi thông báo cho user khi hủy đơn hàng
            String userTitle = "Đơn hàng đã được hủy #" + orderId;
            String userMessage = "Đơn hàng #" + orderId + " của bạn đã được hủy thành công.";
            notificationService.createNotification(userTitle, userMessage, "ORDER_CANCELLED", user, orderId);
            
            // Gửi thông báo real-time cho user
            notificationService.sendNotificationToUser(user, userTitle, userMessage, "ORDER_CANCELLED", orderId);
            
            // Gửi thông báo cho admin khi user hủy đơn hàng
            String adminTitle = "Đơn hàng bị hủy #" + orderId;
            String adminMessage = "Khách hàng " + user.getHoTen() + " đã hủy đơn hàng #" + orderId + 
                                " với tổng tiền " + hoaDon.getTongTien() + " VNĐ";
            notificationService.createAdminNotification(adminTitle, adminMessage, "ORDER_CANCELLED", orderId);
            
            return true;
        }
        return false;
    }

    @Override
    public HoaDon saveOrder(HoaDon hoaDon) {
        return hoaDonRepository.save(hoaDon);
    }

    @Override
    public OrderResponseDTO getUserOrdersWithValidation() {
        // Kiểm tra authentication
        if (!authenticationService.isAuthenticated()) {
            throw new AuthenticationException("Vui lòng đăng nhập");
        }
        
        User user = authenticationService.getCurrentUser();
        if (user == null) {
            throw new AuthenticationException("Không tìm thấy thông tin người dùng");
        }

        List<HoaDon> orders = getUserOrders(user);
        return OrderResponseDTO.success("Lấy danh sách đơn hàng thành công", orders);
    }

    @Override
    public OrderResponseDTO getUserOrderDetailWithValidation(Integer orderId) {
        // Kiểm tra authentication
        if (!authenticationService.isAuthenticated()) {
            throw new AuthenticationException("Vui lòng đăng nhập");
        }
        
        User user = authenticationService.getCurrentUser();
        if (user == null) {
            throw new AuthenticationException("Không tìm thấy thông tin người dùng");
        }

        // Validate input
        if (orderId == null || orderId <= 0) {
            throw new ValidationException("ID đơn hàng không hợp lệ");
        }

        HoaDon order = getUserOrderById(user, orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Không tìm thấy đơn hàng");
        }

        return OrderResponseDTO.success("Lấy chi tiết đơn hàng thành công", order);
    }

    @Override
    public OrderResponseDTO cancelOrderWithValidation(CancelOrderDTO cancelOrderDTO) {
        // Kiểm tra authentication
        if (!authenticationService.isAuthenticated()) {
            throw new AuthenticationException("Vui lòng đăng nhập");
        }
        
        User user = authenticationService.getCurrentUser();
        if (user == null) {
            throw new AuthenticationException("Không tìm thấy thông tin người dùng");
        }

        // Validate input
        if (cancelOrderDTO.getOrderId() == null || cancelOrderDTO.getOrderId() <= 0) {
            throw new ValidationException("ID đơn hàng không hợp lệ");
        }

        boolean success = cancelOrder(user, cancelOrderDTO.getOrderId());
        if (!success) {
            throw new ValidationException("Không thể hủy đơn hàng này");
        }

        return OrderResponseDTO.success("Hủy đơn hàng thành công");
    }
}
