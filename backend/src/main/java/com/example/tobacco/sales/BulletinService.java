package com.example.tobacco.sales;

import com.example.tobacco.mapper.sales.BulletinMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BulletinService {
    private final BulletinMapper bulletinMapper;

    public BulletinService(BulletinMapper bulletinMapper) {
        this.bulletinMapper = bulletinMapper;
    }

    public List<Map<String, Object>> list() {
        return bulletinMapper.selectBulletins();
    }

    public void create(String title, String content, String category, String expiredAt, String createdBy) {
        if (expiredAt != null && expiredAt.trim().length() > 0) {
            bulletinMapper.insertBulletinWithExpireAt(title, content, category, expiredAt, createdBy);
        } else {
            bulletinMapper.insertBulletin(title, content, category, createdBy);
        }
    }
}
