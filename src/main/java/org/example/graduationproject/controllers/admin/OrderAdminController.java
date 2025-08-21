package org.example.graduationproject.controllers.admin;

import org.example.graduationproject.models.HoaDon;
import org.example.graduationproject.repositories.HoaDonRepository;
import org.example.graduationproject.services.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class OrderAdminController {

	@Autowired
	private HoaDonRepository hoaDonRepository;

	@Autowired
	private HoaDonService hoaDonService;

	@GetMapping("/order")
	public String orderPage(Model model,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "10") int size) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication != null ? authentication.getName() : "admin";
		model.addAttribute("username", username);
		model.addAttribute("currentPage", "order");

		// Load orders (optionally by status), sorted by created date desc
		List<HoaDon> orders;
		if (status != null && !status.isBlank()) {
			orders = new ArrayList<>(hoaDonRepository.findByTrangThai(status));
			orders.sort(Comparator.comparing(HoaDon::getNgayTao, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
		} else {
			orders = hoaDonRepository.findAll(Sort.by(Sort.Direction.DESC, "ngayTao"));
		}

		// Apply search filter in-memory
		if (search != null && !search.isBlank()) {
			final String q = search.toLowerCase(Locale.ROOT).trim();
			orders = orders.stream().filter(o -> {
				String idStr = o.getId() != null ? String.valueOf(o.getId()) : "";
				boolean idMatch = idStr.contains(q);
				boolean userMatch = false;
				if (o.getUser() != null) {
					String hoTen = o.getUser().getHoTen() != null ? o.getUser().getHoTen().toLowerCase(Locale.ROOT) : "";
					String uname = o.getUser().getUsername() != null ? o.getUser().getUsername().toLowerCase(Locale.ROOT) : "";
					String phone = o.getUser().getSoDienThoai() != null ? o.getUser().getSoDienThoai().toLowerCase(Locale.ROOT) : "";
					userMatch = hoTen.contains(q) || uname.contains(q) || phone.contains(q);
				}
				return idMatch || userMatch;
			}).collect(Collectors.toList());
		}

		long totalElements = orders.size();
		int totalPages = (int) Math.ceil(totalElements / (double) size);
		int lastPage = totalPages > 0 ? totalPages - 1 : 0;
		int prevPage = page > 0 ? page - 1 : 0;
		int nextPage = (page + 1 < totalPages) ? page + 1 : lastPage;

		int fromIndex = Math.min(page * size, orders.size());
		int toIndex = Math.min(fromIndex + size, orders.size());
		List<HoaDon> pageContent = fromIndex < toIndex ? orders.subList(fromIndex, toIndex) : new ArrayList<>();

		model.addAttribute("orders", pageContent);
		model.addAttribute("page", page);
		model.addAttribute("size", size);
		model.addAttribute("totalElements", totalElements);
		model.addAttribute("lastPage", lastPage);
		model.addAttribute("prevPage", prevPage);
		model.addAttribute("nextPage", nextPage);
		model.addAttribute("search", search);
		model.addAttribute("status", status);

		return "admin/order";
	}

	@PostMapping("/order/{id}/approve")
	public String approveOrder(@PathVariable("id") Integer id,
			RedirectAttributes redirectAttributes) {
		HoaDon hoaDon = hoaDonService.getOrderById(id);
		if (hoaDon == null) {
			redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
			return "redirect:/admin/order";
		}
		if (!"PENDING".equalsIgnoreCase(hoaDon.getTrangThai())) {
			redirectAttributes.addFlashAttribute("error", "Chỉ có thể duyệt đơn đang ở trạng thái PENDING.");
			return "redirect:/admin/order";
		}
		boolean ok = hoaDonService.updateOrderStatus(id, "CONFIRMED");
		if (ok) {
			redirectAttributes.addFlashAttribute("success", "Đã duyệt đơn hàng #" + id + ".");
		} else {
			redirectAttributes.addFlashAttribute("error", "Không thể duyệt đơn hàng.");
		}
		return "redirect:/admin/order";
	}

	@PostMapping("/order/{id}/cancel")
	public String cancelOrder(@PathVariable("id") Integer id,
			RedirectAttributes redirectAttributes) {
		HoaDon hoaDon = hoaDonService.getOrderById(id);
		if (hoaDon == null) {
			redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
			return "redirect:/admin/order";
		}
		if (!"PENDING".equalsIgnoreCase(hoaDon.getTrangThai())) {
			redirectAttributes.addFlashAttribute("error", "Chỉ có thể hủy đơn đang ở trạng thái PENDING.");
			return "redirect:/admin/order";
		}
		boolean ok = hoaDonService.updateOrderStatus(id, "CANCELLED");
		if (ok) {
			redirectAttributes.addFlashAttribute("success", "Đã hủy đơn hàng #" + id + ".");
		} else {
			redirectAttributes.addFlashAttribute("error", "Không thể hủy đơn hàng.");
		}
		return "redirect:/admin/order";
	}

	@PostMapping("/order/{id}/update-status")
	public String updateOrderStatus(@PathVariable("id") Integer id, 
								   @RequestParam("newStatus") String newStatus,
								   RedirectAttributes redirectAttributes) {
		HoaDon hoaDon = hoaDonService.getOrderById(id);
		if (hoaDon == null) {
			redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
			return "redirect:/admin/order";
		}

		String currentStatus = hoaDon.getTrangThai();
		
		// Kiểm tra logic chuyển trạng thái
		if ("PENDING".equalsIgnoreCase(currentStatus) && "CONFIRMED".equalsIgnoreCase(newStatus)) {
			// Chuyển từ PENDING sang CONFIRMED - cần kiểm tra tồn kho
			if (!hoaDonService.updateOrderStatus(id, newStatus)) {
				redirectAttributes.addFlashAttribute("error", "Không thể cập nhật trạng thái đơn hàng #" + id + ". Không đủ tồn kho hoặc thay đổi trạng thái không hợp lệ.");
				return "redirect:/admin/order";
			}
		} else if ("CONFIRMED".equalsIgnoreCase(currentStatus) && "CANCELLED".equalsIgnoreCase(newStatus)) {
			// Chuyển từ CONFIRMED sang CANCELLED - cần hoàn lại tồn kho
			if (hoaDonService.updateOrderStatusAndRestoreStock(id, newStatus)) {
				redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái đơn hàng #" + id + " thành " + newStatus + " và hoàn lại tồn kho.");
			} else {
				redirectAttributes.addFlashAttribute("error", "Không thể cập nhật trạng thái đơn hàng #" + id + ".");
			}
			return "redirect:/admin/order";
		} else {
			// Các trường hợp khác - cập nhật bình thường
			if (hoaDonService.updateOrderStatus(id, newStatus)) {
				redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái đơn hàng #" + id + " thành " + newStatus + ".");
			} else {
				redirectAttributes.addFlashAttribute("error", "Không thể cập nhật trạng thái đơn hàng #" + id + ".");
			}
		}



		return "redirect:/admin/order";
	}

	@GetMapping("/order/{id}")
	public String orderDetail(@PathVariable("id") Integer id, Model model) {
		HoaDon hoaDon = hoaDonService.getOrderById(id);
		if (hoaDon == null) {
			return "redirect:/admin/order";
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication != null ? authentication.getName() : "admin";
		model.addAttribute("username", username);
		model.addAttribute("currentPage", "order");
		model.addAttribute("order", hoaDon);

		return "admin/order-detail";
	}
}


