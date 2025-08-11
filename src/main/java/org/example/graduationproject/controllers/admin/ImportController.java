package org.example.graduationproject.controllers.admin;

import org.example.graduationproject.dto.ImportRequestDTO;
import org.example.graduationproject.dto.ImportDetailDTO;
import org.example.graduationproject.models.NhaCungCap;
import org.example.graduationproject.models.SanPham;
import org.example.graduationproject.models.SanPhamBienThe;
import org.example.graduationproject.models.PhieuNhapHang;
import org.example.graduationproject.models.ChiTietPhieuNhapHang;
import org.example.graduationproject.services.NhaCungCapService;
import org.example.graduationproject.services.SanPhamService;
import org.example.graduationproject.services.SanPhamBienTheService;
import org.example.graduationproject.services.PhieuNhapHangService;
import org.example.graduationproject.services.ChiTietPhieuNhapHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class ImportController {
    @Autowired
    private NhaCungCapService nhaCungCapService;
    
    @Autowired
    private SanPhamService sanPhamService;
    
    @Autowired
    private SanPhamBienTheService sanPhamBienTheService;
    
    @Autowired
    private PhieuNhapHangService phieuNhapHangService;
    
    @Autowired
    private ChiTietPhieuNhapHangService chiTietPhieuNhapHangService;

    @GetMapping("/import")
    public String importPage(Model model,
                            @RequestParam(value = "search", required = false) String search,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "10") int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Lấy danh sách phiếu nhập hàng từ database với phân trang và tìm kiếm
        Page<PhieuNhapHang> phieuNhapHangPage;
        if (search != null && !search.trim().isEmpty()) {
            // Tìm kiếm theo số chứng từ hoặc tên nhà cung cấp
            phieuNhapHangPage = phieuNhapHangService.searchBySoChungTuContainingIgnoreCase(search, PageRequest.of(page, size));
            if (phieuNhapHangPage.getTotalElements() == 0) {
                // Nếu không tìm thấy theo số chứng từ, tìm theo tên nhà cung cấp
                phieuNhapHangPage = phieuNhapHangService.searchByNhaCungCapTenContainingIgnoreCase(search, PageRequest.of(page, size));
            }
            model.addAttribute("search", search);
        } else {
            phieuNhapHangPage = phieuNhapHangService.getAllPaging(PageRequest.of(page, size));
        }

        model.addAttribute("username", username);
        model.addAttribute("currentPage", "import");
        model.addAttribute("phieuNhapHangs", phieuNhapHangPage.getContent());
        model.addAttribute("phieuNhapHangPage", phieuNhapHangPage);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", phieuNhapHangPage.getTotalPages());
        model.addAttribute("totalElements", phieuNhapHangPage.getTotalElements());
        
        // Tính toán các trang cho navigation
        int lastPage = phieuNhapHangPage.getTotalPages() > 0 ? phieuNhapHangPage.getTotalPages() - 1 : 0;
        int prevPage = page > 0 ? page - 1 : 0;
        int nextPage = (page + 1 < phieuNhapHangPage.getTotalPages()) ? page + 1 : lastPage;
        model.addAttribute("lastPage", lastPage);
        model.addAttribute("prevPage", prevPage);
        model.addAttribute("nextPage", nextPage);
        
        return "admin/import";
    }

    @GetMapping("/import/create")
    public String createImportForm(Model model) {
        List<NhaCungCap> suppliers = nhaCungCapService.getAllNhaCungCap();
        List<SanPham> products = sanPhamService.getAll();
        List<SanPhamBienThe> variants = sanPhamBienTheService.getAllSanPhamBienThe();
        
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("products", products);
        model.addAttribute("variants", variants);
        return "admin/import-form";
    }

    @PostMapping("/import/create")
    @ResponseBody
    @Transactional
    public ResponseEntity<Map<String, Object>> createImport(@RequestBody ImportRequestDTO request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. Validate request
            if (request.getSupplierId() == null || request.getDetails() == null || request.getDetails().isEmpty()) {
                response.put("success", false);
                response.put("message", "Dữ liệu không hợp lệ!");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 2. Get supplier
            Optional<NhaCungCap> supplierOpt = nhaCungCapService.findById(request.getSupplierId());
            if (supplierOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Không tìm thấy nhà cung cấp!");
                return ResponseEntity.badRequest().body(response);
            }
            NhaCungCap supplier = supplierOpt.get();
            
            // 3. Calculate total amount
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (ImportDetailDTO detail : request.getDetails()) {
                String numeric = detail.getImportPrice() != null ? detail.getImportPrice().replaceAll("[^\\d]", "") : "0";
                if (numeric.isEmpty()) numeric = "0";
                BigDecimal price = new BigDecimal(numeric);
                totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(detail.getQuantity())));
            }
            
            // 4. Create PhieuNhapHang
            PhieuNhapHang phieuNhapHang = new PhieuNhapHang();
            phieuNhapHang.setSoChungTu("PN" + System.currentTimeMillis()); // Generate receipt number
            phieuNhapHang.setNgayTao(LocalDateTime.now());
            phieuNhapHang.setTongTien(totalAmount);
            phieuNhapHang.setNguoiLapPhieu(getCurrentUsername());
            phieuNhapHang.setGhiChu(request.getNote());
            phieuNhapHang.setNhaCungCap(supplier);
            
            // Save PhieuNhapHang
            phieuNhapHang = phieuNhapHangService.save(phieuNhapHang);
            
            // 5. Create ChiTietPhieuNhapHang for each detail
            for (ImportDetailDTO detail : request.getDetails()) {
                // Get SanPhamBienThe
                Optional<SanPhamBienThe> variantOpt = sanPhamBienTheService.getSanPhamBienTheById(detail.getVariantId());
                if (variantOpt.isEmpty()) {
                    throw new RuntimeException("Không tìm thấy biến thể sản phẩm với ID: " + detail.getVariantId());
                }
                SanPhamBienThe variant = variantOpt.get();
                
                // Create ChiTietPhieuNhapHang
                ChiTietPhieuNhapHang chiTiet = new ChiTietPhieuNhapHang();
                chiTiet.setSoLuongNhap(detail.getQuantity());
                String numericDetail = detail.getImportPrice() != null ? detail.getImportPrice().replaceAll("[^\\d]", "") : "0";
                if (numericDetail.isEmpty()) numericDetail = "0";
                BigDecimal price = new BigDecimal(numericDetail);
                chiTiet.setThanhTienNhap(price.multiply(BigDecimal.valueOf(detail.getQuantity())));
                chiTiet.setPhieuNhapHang(phieuNhapHang);
                chiTiet.setSanPhamBienThe(variant);
                
                // Save ChiTietPhieuNhapHang
                chiTietPhieuNhapHangService.save(chiTiet);
                
                // 6. Update product variant quantity
                int currentQuantity = variant.getSoLuongTon() != null ? variant.getSoLuongTon() : 0;
                variant.setSoLuongTon(currentQuantity + detail.getQuantity());
                sanPhamBienTheService.saveSanPhamBienThe(variant);
            }
            
            response.put("success", true);
            response.put("message", "Tạo phiếu nhập thành công! Mã phiếu: " + phieuNhapHang.getSoChungTu());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/import/{id}")
    public String viewImportDetail(@PathVariable("id") Integer id, Model model) {
        Optional<PhieuNhapHang> phieuOpt = phieuNhapHangService.findById(id);
        if (phieuOpt.isEmpty()) {
            return "redirect:/admin/import";
        }
        PhieuNhapHang phieu = phieuOpt.get();
        List<ChiTietPhieuNhapHang> details = chiTietPhieuNhapHangService.findByPhieuNhapHangId(id);
        
        model.addAttribute("currentPage", "import");
        model.addAttribute("phieu", phieu);
        model.addAttribute("details", details);
        
        return "admin/import-detail";
    }
    
    @PostMapping("/import/{id}/delete")
    @Transactional
    public String deleteImport(@PathVariable("id") Integer id) {
        Optional<PhieuNhapHang> phieuOpt = phieuNhapHangService.findById(id);
        if (phieuOpt.isEmpty()) {
            return "redirect:/admin/import";
        }
        // Rollback tồn kho từ các chi tiết
        List<ChiTietPhieuNhapHang> details = chiTietPhieuNhapHangService.findByPhieuNhapHangId(id);
        for (ChiTietPhieuNhapHang ct : details) {
            if (ct.getSanPhamBienThe() != null) {
                SanPhamBienThe variant = ct.getSanPhamBienThe();
                int currentQuantity = variant.getSoLuongTon() != null ? variant.getSoLuongTon() : 0;
                int newQuantity = currentQuantity - (ct.getSoLuongNhap() != null ? ct.getSoLuongNhap() : 0);
                variant.setSoLuongTon(Math.max(newQuantity, 0));
                sanPhamBienTheService.saveSanPhamBienThe(variant);
            }
        }
        // Xóa phiếu (cascade sẽ xóa chi tiết)
        phieuNhapHangService.deleteById(id);
        return "redirect:/admin/import";
    }
    
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "Unknown";
    }
}
