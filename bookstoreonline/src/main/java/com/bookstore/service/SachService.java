package com.bookstore.service;

import com.bookstore.dto.SachDTO;
import com.bookstore.repository.SachRepository;
import com.bookstore.dto.SachCreateRequest;
import com.bookstore.dto.SachUpdateRequest;
import com.bookstore.repository.DanhMucRepository;
import com.bookstore.repository.NxbRepository;
import com.bookstore.repository.TacGiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class SachService {

    private final SachRepository sachRepository;
    private final DanhMucRepository danhMucRepository;
    private final NxbRepository nxbRepository;
    private final TacGiaRepository tacGiaRepository;

    public SachService(SachRepository sachRepository,
            DanhMucRepository danhMucRepository,
            NxbRepository nxbRepository,
            TacGiaRepository tacGiaRepository) {
        this.sachRepository = sachRepository;
        this.danhMucRepository = danhMucRepository;
        this.nxbRepository = nxbRepository;
        this.tacGiaRepository = tacGiaRepository;
    }

    @Transactional(readOnly = true)
    public List<SachDTO> searchAndFilterBooks(String keyword, String tenDanhMuc, String tenNxb,
            BigDecimal minPrice, BigDecimal maxPrice, Integer minSoTrang, Integer maxSoTrang) {
        return sachRepository.searchAndFilterBooks(keyword, tenDanhMuc, tenNxb, minPrice, maxPrice, minSoTrang, maxSoTrang)
                .stream()
                .map(SachDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SachDTO getBookByIsbn(String isbn) {
        com.bookstore.entity.Sach sach = sachRepository.findById(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách với mã ISBN: " + isbn));
        
        if (Boolean.TRUE.equals(sach.getDaXoa())) {
             throw new IllegalArgumentException("Sách này đã bị xóa khỏi hệ thống");
        }
        return SachDTO.fromEntity(sach);
    }

    @Transactional
    public SachDTO createBook(SachCreateRequest request) {
        if (sachRepository.existsById(request.getIsbn())) {
            throw new IllegalArgumentException("Sách với mã ISBN này đã tồn tại: " + request.getIsbn());
        }

        com.bookstore.entity.Sach sach = new com.bookstore.entity.Sach();
        sach.setIsbn(request.getIsbn());
        sach.setTenSach(request.getTenSach());
        sach.setGiaNiemYet(request.getGiaNiemYet());
        sach.setSoTrang(request.getSoTrang());
        sach.setMoTaNguNghia(request.getMoTaNguNghia());
        sach.setAnhBia(request.getAnhBia());

        com.bookstore.entity.DanhMuc danhMuc = danhMucRepository.findById(request.getMaDanhMuc())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy danh mục với mã: " + request.getMaDanhMuc()));
        sach.setDanhMuc(danhMuc);

        com.bookstore.entity.Nxb nxb = nxbRepository.findById(request.getMaNxb())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy nhà xuất bản với mã: " + request.getMaNxb()));
        sach.setNxb(nxb);

        if (request.getTacGiaIds() != null && !request.getTacGiaIds().isEmpty()) {
            java.util.List<com.bookstore.entity.TacGia> tacGias = tacGiaRepository.findAllById(request.getTacGiaIds());
            if (tacGias.size() != request.getTacGiaIds().size()) {
                throw new IllegalArgumentException("Một hoặc nhiều mã tác giả không hợp lệ");
            }
            sach.setDanhSachTacGia(new java.util.HashSet<>(tacGias));
        }

        return SachDTO.fromEntity(sachRepository.save(sach));
    }

    @Transactional
    public SachDTO updateBook(String isbn, SachUpdateRequest request) {
        com.bookstore.entity.Sach sach = sachRepository.findById(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách với mã ISBN: " + isbn));

        if (Boolean.TRUE.equals(sach.getDaXoa())) {
            throw new IllegalArgumentException("Sách này đã bị xóa, không thể sửa đổi thông tin");
        }

        sach.setTenSach(request.getTenSach());
        sach.setGiaNiemYet(request.getGiaNiemYet());
        sach.setSoTrang(request.getSoTrang());
        sach.setMoTaNguNghia(request.getMoTaNguNghia());
        sach.setAnhBia(request.getAnhBia());

        com.bookstore.entity.DanhMuc danhMuc = danhMucRepository.findById(request.getMaDanhMuc())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy danh mục với mã: " + request.getMaDanhMuc()));
        sach.setDanhMuc(danhMuc);

        com.bookstore.entity.Nxb nxb = nxbRepository.findById(request.getMaNxb())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy nhà xuất bản với mã: " + request.getMaNxb()));
        sach.setNxb(nxb);

        if (request.getTacGiaIds() != null && !request.getTacGiaIds().isEmpty()) {
            java.util.List<com.bookstore.entity.TacGia> tacGias = tacGiaRepository.findAllById(request.getTacGiaIds());
            if (tacGias.size() != request.getTacGiaIds().size()) {
                throw new IllegalArgumentException("Một hoặc nhiều mã tác giả không hợp lệ");
            }
            sach.setDanhSachTacGia(new java.util.HashSet<>(tacGias));
        } else {
            sach.setDanhSachTacGia(new java.util.HashSet<>());
        }

        return SachDTO.fromEntity(sachRepository.save(sach));
    }

    @Transactional
    public void softDeleteBook(String isbn) {
        com.bookstore.entity.Sach sach = sachRepository.findById(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách với mã ISBN: " + isbn));
        
        if (Boolean.TRUE.equals(sach.getDaXoa())) {
             throw new IllegalArgumentException("Sách này đã bị xóa trước đó");
        }
        
        sach.setDaXoa(true);
        sachRepository.save(sach);
    }
}
