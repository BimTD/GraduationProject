package org.example.graduationproject.services.impl;

import org.example.graduationproject.models.SanPham;
import org.example.graduationproject.repositories.SanPhamRepository;
import org.example.graduationproject.services.SanPhamService;
import org.example.graduationproject.dto.ProductDTO;
import org.example.graduationproject.models.ImageSanPham;
import org.example.graduationproject.repositories.ImageSanPhamRepository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.graduationproject.repositories.LoaiRepository;
import org.example.graduationproject.repositories.NhanHieuRepository;
import org.example.graduationproject.repositories.NhaCungCapRepository;

import java.util.List;


@Service
public class SanPhamServiceImpl implements SanPhamService {

    @Autowired
    private SanPhamRepository sanPhamRepository;
    @Autowired
    private ImageSanPhamRepository imageSanPhamRepository;
    @Autowired
    private LoaiRepository loaiRepository;
    @Autowired
    private NhanHieuRepository nhanHieuRepository;
    @Autowired
    private NhaCungCapRepository nhaCungCapRepository;

    @Override
    public List<SanPham> getAll() {
        return this.sanPhamRepository.findAll();
    }

    @Override
    public Boolean create(SanPham sanPham) {
        try{
            this.sanPhamRepository.save(sanPham);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public SanPham findById(Integer id) {
        return this.sanPhamRepository.findById(id).orElse(null);
    }

    @Override
    public Boolean deleteById(Integer id) {
        try {
            // Xóa toàn bộ ảnh liên quan trước
            imageSanPhamRepository.deleteAll(imageSanPhamRepository.findAllBySanPham_Id(id));
            this.sanPhamRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean update(SanPham sanPham) {
        try {
            this.sanPhamRepository.save(sanPham);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public SanPham save(SanPham sanPham) {
        return this.sanPhamRepository.save(sanPham);
    }

    @Override
    public void updateActiveStatus(Integer id, boolean active) {
        SanPham sp = sanPhamRepository.findById(id).orElse(null);
        if (sp != null) {
            sp.setTrangThaiHoatDong(active);
            sanPhamRepository.save(sp);
        }
    }

    @Override
    public void saveProductWithUrls(ProductDTO productDTO, String imageUrls) throws Exception {
        SanPham sanPham = new SanPham();
        sanPham.setTen(productDTO.getTen());
        sanPham.setMoTa(productDTO.getMoTa());
        sanPham.setGiaBan(productDTO.getGiaBan());
        sanPham.setGiaNhap(productDTO.getGiaNhap());
        sanPham.setKhuyenMai(productDTO.getKhuyenMai());
        sanPham.setTag(productDTO.getTag());
        sanPham.setHuongDan(productDTO.getHuongDan());
        sanPham.setThanhPhan(productDTO.getThanhPhan());
        sanPham.setNgayTao(java.time.LocalDateTime.now());
        sanPham.setNgayCapNhat(java.time.LocalDateTime.now());
        sanPham.setTrangThaiSanPham("new");
        sanPham.setTrangThaiHoatDong(true);
        sanPham.setGioiTinh(productDTO.getGioiTinh());
        sanPham.setLoai(loaiRepository.findById(productDTO.getLoaiId()).orElse(null));
        sanPham.setNhanHieu(nhanHieuRepository.findById(productDTO.getNhanHieuId()).orElse(null));
        sanPham.setNhaCungCap(nhaCungCapRepository.findById(productDTO.getNhaCungCapId()).orElse(null));
        sanPham = sanPhamRepository.save(sanPham);
        if (imageUrls != null && !imageUrls.isEmpty()) {
            String[] urls = imageUrls.split(",");
            for (String url : urls) {
                ImageSanPham image = new ImageSanPham();
                image.setImageName(url.trim());
                image.setSanPham(sanPham);
                imageSanPhamRepository.save(image);
            }
        }
    }
}
