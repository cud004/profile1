package com.profileMangager.profile.store;

import com.profileMangager.profile.entity.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * InMemoryProfileStore:
 * - Lưu Profile trong RAM bằng ConcurrentHashMap.
 * - Đóng vai trò "kho dữ liệu trong memory" cho tuần 2.
 */
@Component
public class InMemoryProfileStore {

    /**
     * ConcurrentHashMap:
     * - key   : userId (Long)
     * - value : Profile tương ứng
     *
     * Dùng ConcurrentHashMap thay vì HashMap vì:
     * - Backend Spring Boot là multi-thread (nhiều request cùng lúc).
     * - ConcurrentHashMap hỗ trợ thread-safe cho nhiều luồng đọc/ghi.
     */
    private final ConcurrentHashMap<Long, Profile> store = new ConcurrentHashMap<>();

    /**
     * Lưu hoặc cập nhật 1 Profile vào store.
     * Nếu userId đã tồn tại -> ghi đè.
     */
    public Profile save(Profile profile) {
        if (profile == null || profile.getUserId() == null) {
            throw new IllegalArgumentException("Profile hoặc userId không được null");
        }
        store.put(profile.getUserId(), profile);
        return profile;
    }

    /**
     * Tìm 1 Profile theo id trong store.
     * Nếu không có -> trả về null.
     */
    public Profile findById(Long id) {
        if (id == null) return null;
        return store.get(id);
    }

    /**
     * Trả về danh sách tất cả Profile đang lưu trong memory.
     *
     * new ArrayList<>(store.values()):
     * - Lấy tất cả value (Profile) trong map,
     * - "bọc" thành 1 List mới, tách khỏi map bên dưới.
     */
    public List<Profile> findAll() {
        return new ArrayList<>(store.values());
    }

    /**
     * Xóa 1 Profile theo id khỏi store (nếu tồn tại).
     */
    public void deleteById(Long id) {
        if (id == null) return;
        store.remove(id);
    }

    /**
     * Xóa tất cả Profile trong memory.
     * Có thể dùng khi test.
     */
    public void clear() {
        store.clear();
    }

    /**
     * Trả về số lượng profile đang lưu trong memory.
     * Hữu ích để debug / log.
     */
    public int size() {
        return store.size();
    }
}
