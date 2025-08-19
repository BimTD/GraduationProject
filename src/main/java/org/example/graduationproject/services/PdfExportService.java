package org.example.graduationproject.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Div;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import org.example.graduationproject.models.PhieuNhapHang;
import org.example.graduationproject.models.ChiTietPhieuNhapHang;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfExportService {

    public byte[] exportImportDetailToPdf(PhieuNhapHang phieu, List<ChiTietPhieuNhapHang> details) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        try {
            // Tạo font với fallback
            PdfFont font;
            PdfFont boldFont;
            try {
                // Sử dụng font Times-Roman hỗ trợ tốt cho tiếng Việt
                font = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
                boldFont = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
            } catch (Exception e) {
                // Fallback về font cơ bản
                font = PdfFontFactory.createFont(StandardFonts.COURIER);
                boldFont = PdfFontFactory.createFont(StandardFonts.COURIER_BOLD);
            }

            // Tiêu đề chính
            Paragraph title = new Paragraph("CHI TIẾT PHIẾU NHẬP HÀNG")
                    .setFont(boldFont)
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // Thông tin phiếu
            document.add(createImportInfoSection(phieu, font, boldFont));
            document.add(new Paragraph("").setMarginBottom(15));

            // Thông tin nhà cung cấp
            document.add(createSupplierInfoSection(phieu, font, boldFont));
            document.add(new Paragraph("").setMarginBottom(15));

            // Bảng chi tiết sản phẩm
            document.add(createProductDetailsTable(details, font, boldFont));
            document.add(new Paragraph("").setMarginBottom(15));

            // Tổng kết
            document.add(createSummarySection(phieu, font, boldFont));

        } finally {
            document.close();
        }

        return outputStream.toByteArray();
    }

    private Div createImportInfoSection(PhieuNhapHang phieu, PdfFont font, PdfFont boldFont) {
        Div section = new Div();
        
        // Header
        Paragraph header = new Paragraph("THÔNG TIN PHIẾU NHẬP")
                .setFont(boldFont)
                .setFontSize(14)
                .setBackgroundColor(new DeviceRgb(59, 130, 246))
                .setFontColor(ColorConstants.WHITE)
                .setPadding(8)
                .setMarginBottom(10);
        section.add(header);

        // Bảng thông tin
        Table table = new Table(UnitValue.createPercentArray(new float[]{25, 75, 25, 75}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(10);

        // Dòng 1
        table.addCell(createCell("Mã phiếu:", font, boldFont, true));
        table.addCell(createCell(phieu.getSoChungTu() != null ? phieu.getSoChungTu() : "", font, boldFont, false));
        table.addCell(createCell("Ngày tạo:", font, boldFont, true));
        table.addCell(createCell(phieu.getNgayTao() != null ? 
            phieu.getNgayTao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "", font, boldFont, false));

        // Dòng 2
        table.addCell(createCell("Người lập:", font, boldFont, true));
        table.addCell(createCell(phieu.getNguoiLapPhieu() != null ? phieu.getNguoiLapPhieu() : "", font, boldFont, false));
        table.addCell(createCell("Ghi chú:", font, boldFont, true));
        table.addCell(createCell(phieu.getGhiChu() != null ? phieu.getGhiChu() : "", font, boldFont, false));

        section.add(table);
        return section;
    }

    private Div createSupplierInfoSection(PhieuNhapHang phieu, PdfFont font, PdfFont boldFont) {
        Div section = new Div();
        
        // Header
        Paragraph header = new Paragraph("THÔNG TIN NHÀ CUNG CẤP")
                .setFont(boldFont)
                .setFontSize(14)
                .setBackgroundColor(new DeviceRgb(59, 130, 246))
                .setFontColor(ColorConstants.WHITE)
                .setPadding(8)
                .setMarginBottom(10);
        section.add(header);

        // Bảng thông tin
        Table table = new Table(UnitValue.createPercentArray(new float[]{25, 75, 25, 75}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(10);

        // Dòng 1
        table.addCell(createCell("Tên:", font, boldFont, true));
        table.addCell(createCell(phieu.getNhaCungCap() != null && phieu.getNhaCungCap().getTen() != null ? 
            phieu.getNhaCungCap().getTen() : "", font, boldFont, false));
        table.addCell(createCell("Email:", font, boldFont, true));
        table.addCell(createCell(phieu.getNhaCungCap() != null && phieu.getNhaCungCap().getEmail() != null ? 
            phieu.getNhaCungCap().getEmail() : "", font, boldFont, false));

        // Dòng 2
        table.addCell(createCell("SĐT:", font, boldFont, true));
        table.addCell(createCell(phieu.getNhaCungCap() != null && phieu.getNhaCungCap().getSdt() != null ? 
            phieu.getNhaCungCap().getSdt() : "", font, boldFont, false));
        table.addCell(createCell("Địa chỉ:", font, boldFont, true));
        table.addCell(createCell(phieu.getNhaCungCap() != null && phieu.getNhaCungCap().getDiaChi() != null ? 
            phieu.getNhaCungCap().getDiaChi() : "", font, boldFont, false));

        section.add(table);
        return section;
    }

    private Div createProductDetailsTable(List<ChiTietPhieuNhapHang> details, PdfFont font, PdfFont boldFont) {
        Div section = new Div();
        
        // Header
        Paragraph header = new Paragraph("CHI TIẾT SẢN PHẨM NHẬP")
                .setFont(boldFont)
                .setFontSize(14)
                .setBackgroundColor(new DeviceRgb(59, 130, 246))
                .setFontColor(ColorConstants.WHITE)
                .setPadding(8)
                .setMarginBottom(10);
        section.add(header);

        // Bảng chi tiết
        Table table = new Table(UnitValue.createPercentArray(new float[]{10, 30, 25, 15, 20}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(10);

        // Header bảng
        String[] headers = {"STT", "Sản phẩm", "Biến thể", "Số lượng", "Thành tiền (VNĐ)"};
        for (String headerText : headers) {
            Cell cell = new Cell().add(new Paragraph(headerText).setFont(boldFont));
            cell.setBackgroundColor(new DeviceRgb(59, 130, 246));
            cell.setFontColor(ColorConstants.WHITE);
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setPadding(8);
            table.addHeaderCell(cell);
        }

        // Dữ liệu chi tiết
        for (int i = 0; i < details.size(); i++) {
            ChiTietPhieuNhapHang detail = details.get(i);
            
            // STT
            Cell sttCell = new Cell().add(new Paragraph(String.valueOf(i + 1)).setFont(font));
            sttCell.setTextAlignment(TextAlignment.CENTER);
            sttCell.setPadding(5);
            table.addCell(sttCell);
            
            // Tên sản phẩm
            String productName = detail.getSanPhamBienThe() != null && 
                detail.getSanPhamBienThe().getSanPham() != null ? 
                detail.getSanPhamBienThe().getSanPham().getTen() : "N/A";
            Cell productCell = new Cell().add(new Paragraph(productName).setFont(font));
            productCell.setPadding(5);
            table.addCell(productCell);
            
            // Biến thể
            String variant = "";
            if (detail.getSanPhamBienThe() != null) {
                if (detail.getSanPhamBienThe().getMauSac() != null) {
                    variant += detail.getSanPhamBienThe().getMauSac().getMaMau();
                }
                if (detail.getSanPhamBienThe().getSize() != null) {
                    variant += " - " + detail.getSanPhamBienThe().getSize().getTenSize();
                }
            }
            Cell variantCell = new Cell().add(new Paragraph(variant.isEmpty() ? "N/A" : variant).setFont(font));
            variantCell.setPadding(5);
            table.addCell(variantCell);
            
            // Số lượng
            Cell quantityCell = new Cell().add(new Paragraph(
                String.valueOf(detail.getSoLuongNhap() != null ? detail.getSoLuongNhap() : 0)).setFont(font));
            quantityCell.setTextAlignment(TextAlignment.CENTER);
            quantityCell.setPadding(5);
            table.addCell(quantityCell);
            
            // Thành tiền
            String amount = detail.getThanhTienNhap() != null ? 
                detail.getThanhTienNhap().toString() : "0";
            Cell amountCell = new Cell().add(new Paragraph(amount).setFont(font));
            amountCell.setTextAlignment(TextAlignment.RIGHT);
            amountCell.setPadding(5);
            table.addCell(amountCell);
        }

        section.add(table);
        return section;
    }

    private Div createSummarySection(PhieuNhapHang phieu, PdfFont font, PdfFont boldFont) {
        Div section = new Div();
        
        // Header
        Paragraph header = new Paragraph("TỔNG KẾT")
                .setFont(boldFont)
                .setFontSize(14)
                .setBackgroundColor(new DeviceRgb(59, 130, 246))
                .setFontColor(ColorConstants.WHITE)
                .setPadding(8)
                .setMarginBottom(10);
        section.add(header);

        // Bảng tổng kết
        Table table = new Table(UnitValue.createPercentArray(new float[]{25, 75}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(10);

        // Tổng tiền
        table.addCell(createCell("Tổng tiền:", font, boldFont, true));
        String totalAmount = phieu.getTongTien() != null ? 
            phieu.getTongTien().toString() + " VNĐ" : "0 VNĐ";
        table.addCell(createCell(totalAmount, font, boldFont, false));

        section.add(table);
        return section;
    }

    private Cell createCell(String text, PdfFont font, PdfFont boldFont, boolean isBold) {
        // Xử lý text tiếng Việt
        String processedText = processVietnameseText(text);
        Cell cell = new Cell().add(new Paragraph(processedText).setFont(isBold ? boldFont : font));
        cell.setPadding(5);
        cell.setBorder(null);
        return cell;
    }
    
    private String processVietnameseText(String text) {
        if (text == null) return "";
        // Xử lý text để đảm bảo hiển thị đúng trong PDF
        try {
            // Kiểm tra xem text có ký tự đặc biệt không
            if (text.matches(".*[àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ].*")) {
                // Nếu có ký tự tiếng Việt, giữ nguyên
                return text;
            } else {
                // Nếu không có ký tự đặc biệt, trả về text gốc
                return text;
            }
        } catch (Exception e) {
            // Nếu có lỗi, trả về text gốc
            return text;
        }
    }
}
