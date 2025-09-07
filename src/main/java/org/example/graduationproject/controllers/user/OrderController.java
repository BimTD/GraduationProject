package org.example.graduationproject.controllers.user;

import org.example.graduationproject.controllers.BaseController;
import org.example.graduationproject.dto.OrderResponseDTO;
import org.example.graduationproject.dto.CancelOrderDTO;
import org.example.graduationproject.models.HoaDon;
import org.example.graduationproject.services.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController extends BaseController {

    @Autowired
    private HoaDonService hoaDonService;

    @GetMapping
    public String showOrdersPage(Model model) {
        try {
            OrderResponseDTO response = hoaDonService.getUserOrdersWithValidation();
            @SuppressWarnings("unchecked")
            List<HoaDon> orders = (List<HoaDon>) response.getData();
            model.addAttribute("orders", orders);
            return "user/orders";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/{orderId:[0-9]+}")
    public String showOrderDetail(@PathVariable Integer orderId, Model model) {
        try {
            OrderResponseDTO response = hoaDonService.getUserOrderDetailWithValidation(orderId);
            HoaDon order = (HoaDon) response.getData();
            model.addAttribute("order", order);
            return "user/order-detail";
        } catch (Exception e) {
            return "redirect:/orders";
        }
    }

    @PostMapping("/{orderId:[0-9]+}/cancel")
    @ResponseBody
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Integer orderId) {
        CancelOrderDTO cancelOrderDTO = new CancelOrderDTO(orderId);
        OrderResponseDTO response = hoaDonService.cancelOrderWithValidation(cancelOrderDTO);
        return ResponseEntity.ok(response);
    }
}
