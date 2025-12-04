package com.profileMangager.profile.service;

import com.profileMangager.profile.dto.request.ProfileCreationRequest;
import com.profileMangager.profile.dto.request.ProfileUpdateRequest;
import com.profileMangager.profile.entity.Profile;
import com.profileMangager.profile.repository.ProfileRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final ProfileRepository profileRepository;
    private final ProfileAsyncWriter profileAsyncWriter;
    private final ProfileRedisCache profileRedisCache;

    // Generator ID trong memory
    private final AtomicLong idGenerator = new AtomicLong(0);

    public ProfileService(ProfileRepository profileRepository,
                          ProfileAsyncWriter profileAsyncWriter,
                          ProfileRedisCache profileRedisCache) {
        this.profileRepository = profileRepository;
        this.profileAsyncWriter = profileAsyncWriter;
        this.profileRedisCache = profileRedisCache;
    }

    // ====== INIT ID GENERATOR DỰA TRÊN DB HIỆN TẠI ======
    @PostConstruct
    public void initIdGenerator() {
        Profile last = profileRepository.findTopByOrderByUserIdDesc();
        long startId = (last != null && last.getUserId() != null)
                ? last.getUserId()
                : 0L;

        idGenerator.set(startId);
        log.info("[ID-GEN] Init idGenerator start={}", startId);
    }

    private long nextId() {
        long id = idGenerator.incrementAndGet();
        log.debug("[ID-GEN] nextId={}", id);
        return id;
    }

    // ================== CREATE ==================
    // Cơ chế: sinh ID -> lưu Redis trước -> lưu DB async
    public Profile createProfile(ProfileCreationRequest request) {
        // 1. Tạo ID mới
        long newId = nextId();

        // 2. Tạo profile object
        Profile profile = new Profile();
        profile.setUserId(newId);
        profile.setUsername(request.getUsername());
        profile.setGender(request.isGender());
        profile.setAge(request.getAge());

        // 3. Lưu Redis trước (cache là nguồn đọc nhanh)
        profileRedisCache.save(profile);
        log.info("Created profile id={} -> saved to Redis", newId);

        // 4. Lưu DB async (write-behind)
        profileAsyncWriter.saveToDbAsync(profile);
        log.info("Created profile id={} -> scheduled async save to DB", newId);

        // 5. Trả về cho client (lúc này DB có thể chưa xong, nhưng Redis đã có)
        return profile;
    }

    // ================== GET 1 PROFILE ==================
    // Ưu tiên Redis, nếu miss thì fallback DB và warm lại Redis
    public Profile getProfile(Long id) {
        Profile cached = profileRedisCache.get(id);
        if (cached != null) {
            log.info("getProfile ({}) -> HIT in Redis", id);
            return cached;
        }

        log.info("getProfile ({}) -> MISS in Redis, fallback DB", id);
        Profile dbProfile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));

        profileRedisCache.save(dbProfile);
        log.info("Warm Redis với profile id={}", id);

        return dbProfile;
    }

    // ================== GET LIST ==================
    public List<Profile> getProfiles() {
        // List thường vẫn nên lấy trực tiếp DB
        return profileRepository.findAll();
    }

    // ================== UPDATE ==================
    // Cơ chế: lấy từ Redis/DB -> update field -> lưu Redis -> DB async
    public Profile updateProfile(Long id, ProfileUpdateRequest request) {
        // 1. Lấy từ cache hoặc DB
        Profile profile = profileRedisCache.get(id);

        if (profile == null) {
            log.info("updateProfile ({}) -> MISS Redis, fallback DB", id);
            profile = profileRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));
        } else {
            log.info("updateProfile ({}) -> HIT in Redis", id);
        }

        // 2. Update fields được phép
        profile.setGender(request.isGender());
        profile.setAge(request.getAge());
        // Username không update theo spec request

        // 3. Lưu Redis trước
        profileRedisCache.save(profile);

        // 4. Lưu DB async
        profileAsyncWriter.saveToDbAsync(profile);

        log.info("Updated profile id={} in Redis & scheduled async update to DB", id);
        return profile;
    }

    // ================== DELETE ==================
    // Cơ chế: xóa Redis -> xóa DB async
    public void deleteProfile(Long id) {
        // 1. Xóa Redis
        profileRedisCache.delete(id);
        log.info("Deleted profile id={} trong Redis", id);

        // 2. Xóa DB async
        profileAsyncWriter.deleteFromDbAsync(id);
        log.info("Scheduled async delete profile id={} trong DB", id);
    }
}
