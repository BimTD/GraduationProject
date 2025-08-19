package org.example.graduationproject.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.graduationproject.models.PhieuNhapHang;
import org.example.graduationproject.models.ChiTietPhieuNhapHang;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    public byte[] exportImportDetailToExcel(PhieuNhapHang phieu, List<ChiTietPhieuNhapHang> details) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Chi tiết phiếu nhập");

            // Tạo style cho header
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            int rowNum = 0;

            // Tiêu đề chính
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("CHI TIẾT PHIẾU NHẬP HÀNG");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 4));

            // Thông tin phiếu
            rowNum = createImportInfoSection(sheet, phieu, headerStyle, dataStyle, rowNum);
            
            // Thông tin nhà cung cấp
            rowNum = createSupplierInfoSection(sheet, phieu, headerStyle, dataStyle, rowNum);
            
            // Bảng chi tiết sản phẩm
            rowNum = createProductDetailsTable(sheet, details, headerStyle, dataStyle, rowNum);

            // Tổng kết
            rowNum = createSummarySection(sheet, phieu, headerStyle, dataStyle, rowNum);

            // Tự động điều chỉnh độ rộng cột
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            // Xuất ra byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private int createImportInfoSection(Sheet sheet, PhieuNhapHang phieu, CellStyle headerStyle, CellStyle dataStyle, int startRow) {
        int rowNum = startRow;
        
        // Header thông tin phiếu
        Row headerRow = sheet.createRow(rowNum++);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("THÔNG TIN PHIẾU NHẬP");
        headerCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum-1, rowNum-1, 0, 4));

        // Thông tin chi tiết
        Row infoRow1 = sheet.createRow(rowNum++);
        infoRow1.createCell(0).setCellValue("Mã phiếu:");
        infoRow1.createCell(1).setCellValue(phieu.getSoChungTu() != null ? phieu.getSoChungTu() : "");
        infoRow1.createCell(2).setCellValue("Ngày tạo:");
        infoRow1.createCell(3).setCellValue(phieu.getNgayTao() != null ? 
            phieu.getNgayTao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");

        Row infoRow2 = sheet.createRow(rowNum++);
        infoRow2.createCell(0).setCellValue("Người lập:");
        infoRow2.createCell(1).setCellValue(phieu.getNguoiLapPhieu() != null ? phieu.getNguoiLapPhieu() : "");
        infoRow2.createCell(2).setCellValue("Ghi chú:");
        infoRow2.createCell(3).setCellValue(phieu.getGhiChu() != null ? phieu.getGhiChu() : "");

        // Áp dụng style cho các ô dữ liệu
        for (int i = 0; i < 4; i++) {
            infoRow1.getCell(i).setCellStyle(dataStyle);
            infoRow2.getCell(i).setCellStyle(dataStyle);
        }

        rowNum++; // Thêm một dòng trống
        return rowNum;
    }

    private int createSupplierInfoSection(Sheet sheet, PhieuNhapHang phieu, CellStyle headerStyle, CellStyle dataStyle, int startRow) {
        int rowNum = startRow;
        
        // Header thông tin nhà cung cấp
        Row headerRow = sheet.createRow(rowNum++);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("THÔNG TIN NHÀ CUNG CẤP");
        headerCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum-1, rowNum-1, 0, 4));

        // Thông tin chi tiết
        Row infoRow1 = sheet.createRow(rowNum++);
        infoRow1.createCell(0).setCellValue("Tên:");
        infoRow1.createCell(1).setCellValue(phieu.getNhaCungCap() != null && phieu.getNhaCungCap().getTen() != null ? 
            phieu.getNhaCungCap().getTen() : "");
        infoRow1.createCell(2).setCellValue("Email:");
        infoRow1.createCell(3).setCellValue(phieu.getNhaCungCap() != null && phieu.getNhaCungCap().getEmail() != null ? 
            phieu.getNhaCungCap().getEmail() : "");

        Row infoRow2 = sheet.createRow(rowNum++);
        infoRow2.createCell(0).setCellValue("SĐT:");
        infoRow2.createCell(1).setCellValue(phieu.getNhaCungCap() != null && phieu.getNhaCungCap().getSdt() != null ? 
            phieu.getNhaCungCap().getSdt() : "");
        infoRow2.createCell(2).setCellValue("Địa chỉ:");
        infoRow2.createCell(3).setCellValue(phieu.getNhaCungCap() != null && phieu.getNhaCungCap().getDiaChi() != null ? 
            phieu.getNhaCungCap().getDiaChi() : "");

        // Áp dụng style cho các ô dữ liệu
        for (int i = 0; i < 4; i++) {
            infoRow1.getCell(i).setCellStyle(dataStyle);
            infoRow2.getCell(i).setCellStyle(dataStyle);
        }

        rowNum++; // Thêm một dòng trống
        return rowNum;
    }

    private int createProductDetailsTable(Sheet sheet, List<ChiTietPhieuNhapHang> details, CellStyle headerStyle, CellStyle dataStyle, int startRow) {
        int rowNum = startRow;
        
        // Header bảng chi tiết
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"STT", "Sản phẩm", "Biến thể", "Số lượng", "Thành tiền (VNĐ)"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Dữ liệu chi tiết
        for (int i = 0; i < details.size(); i++) {
            ChiTietPhieuNhapHang detail = details.get(i);
            Row dataRow = sheet.createRow(rowNum++);
            
            dataRow.createCell(0).setCellValue(i + 1);
            dataRow.createCell(1).setCellValue(detail.getSanPhamBienThe() != null && 
                detail.getSanPhamBienThe().getSanPham() != null ? 
                detail.getSanPhamBienThe().getSanPham().getTen() : "N/A");
            
            // Biến thể (màu sắc + size)
            String variant = "";
            if (detail.getSanPhamBienThe() != null) {
                if (detail.getSanPhamBienThe().getMauSac() != null) {
                    variant += detail.getSanPhamBienThe().getMauSac().getMaMau();
                }
                if (detail.getSanPhamBienThe().getSize() != null) {
                    variant += " - " + detail.getSanPhamBienThe().getSize().getTenSize();
                }
            }
            dataRow.createCell(2).setCellValue(variant.isEmpty() ? "N/A" : variant);
            
            dataRow.createCell(3).setCellValue(detail.getSoLuongNhap() != null ? detail.getSoLuongNhap() : 0);
            dataRow.createCell(4).setCellValue(detail.getThanhTienNhap() != null ? 
                detail.getThanhTienNhap().toString() : "0");

            // Áp dụng style cho các ô dữ liệu
            for (int j = 0; j < 5; j++) {
                dataRow.getCell(j).setCellStyle(dataStyle);
            }
        }

        return rowNum;
    }

    private int createSummarySection(Sheet sheet, PhieuNhapHang phieu, CellStyle headerStyle, CellStyle dataStyle, int startRow) {
        int rowNum = startRow;
        
        // Dòng trống
        rowNum++;
        
        // Tổng kết
        Row summaryRow = sheet.createRow(rowNum++);
        summaryRow.createCell(0).setCellValue("TỔNG KẾT:");
        summaryRow.createCell(1).setCellValue("");
        summaryRow.createCell(2).setCellValue("Tổng tiền:");
        summaryRow.createCell(3).setCellValue(phieu.getTongTien() != null ? 
            phieu.getTongTien().toString() + " VNĐ" : "0 VNĐ");

        // Áp dụng style
        summaryRow.getCell(0).setCellStyle(headerStyle);
        summaryRow.getCell(2).setCellStyle(headerStyle);
        summaryRow.getCell(3).setCellStyle(dataStyle);

        return rowNum;
    }
}


