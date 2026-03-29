package com.example.tobacco.supplier;

import com.example.tobacco.mapper.supplier.SupplierMapper;
import com.example.tobacco.model.SupplierItem;
import com.example.tobacco.model.SupplierRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierMapper supplierMapper;

    public SupplierService(SupplierMapper supplierMapper) {
        this.supplierMapper = supplierMapper;
    }

    public List<SupplierItem> listSuppliers(String keyword, String status) {
        return supplierMapper.list(trim(keyword), likeValue(keyword), trim(status));
    }

    public void createSupplier(SupplierRequest request) {
        supplierMapper.insert(request.getName(), request.getContactName(), request.getContactPhone(), request.getAddress());
    }

    public void updateSupplier(Long id, SupplierRequest request) {
        supplierMapper.update(id, request.getName(), request.getContactName(), request.getContactPhone(), request.getAddress());
    }

    public void disableSupplier(Long id) {
        supplierMapper.disable(id);
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }

    private String trim(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private String likeValue(String value) {
        return hasText(value) ? "%" + value.trim() + "%" : null;
    }
}
