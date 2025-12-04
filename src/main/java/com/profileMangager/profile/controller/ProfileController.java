package com.profileMangager.profile.controller;

import com.profileMangager.profile.dto.request.ProfileCreationRequest;
import com.profileMangager.profile.dto.request.ProfileUpdateRequest;
import com.profileMangager.profile.entity.Profile;
import com.profileMangager.profile.monitor.ApiStats;
import com.profileMangager.profile.service.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    private final ProfileService profileService;

    @Autowired
    private ApiStats apiStats;   // Bean đo thống kê

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // ================== CREATE ==================
    @PostMapping
    public Profile createProfile(@RequestBody ProfileCreationRequest request) {

        long start = apiStats.onRequestStart();   // bắt đầu đo stats

        try {
            Profile profile = profileService.createProfile(request);

            long duration = System.currentTimeMillis() - start;
            log.info("[API] POST /profiles done in {} ms, id={}", duration, profile.getUserId());

            return profile;
        } finally {
            apiStats.onRequestEnd(start);         // kết thúc đo stats
        }
    }

    // ================== GET 1 PROFILE ==================
    @GetMapping("/{profileId}")
    public Profile getProfile(@PathVariable("profileId") Long profileId) {

        long start = apiStats.onRequestStart();

        try {
            Profile profile = profileService.getProfile(profileId);

            long duration = System.currentTimeMillis() - start;
            log.info("[API] GET /profiles/{} done in {} ms", profileId, duration);

            return profile;
        } finally {
            apiStats.onRequestEnd(start);
        }
    }

    // ================== GET LIST ==================
    @GetMapping
    public List<Profile> getProfiles() {

        long start = apiStats.onRequestStart();

        try {
            List<Profile> profiles = profileService.getProfiles();

            long duration = System.currentTimeMillis() - start;
            log.info("[API] GET /profiles done in {} ms", duration);

            return profiles;
        } finally {
            apiStats.onRequestEnd(start);
        }
    }

    // ================== UPDATE ==================
    @PutMapping("/{profileId}")
    public Profile updateProfile(@PathVariable Long profileId,
                                 @RequestBody ProfileUpdateRequest request) {

        long start = apiStats.onRequestStart();

        try {
            Profile profile = profileService.updateProfile(profileId, request);

            long duration = System.currentTimeMillis() - start;
            log.info("[API] PUT /profiles/{} done in {} ms", profileId, duration);

            return profile;
        } finally {
            apiStats.onRequestEnd(start);
        }
    }

    // ================== DELETE ==================
    @DeleteMapping("/{profileId}")
    public String deleteProfile(@PathVariable("profileId") Long profileId) {

        long start = apiStats.onRequestStart();

        try {
            profileService.deleteProfile(profileId);

            long duration = System.currentTimeMillis() - start;
            log.info("[API] DELETE /profiles/{} done in {} ms", profileId, duration);

            return "Profile deleted";
        } finally {
            apiStats.onRequestEnd(start);
        }
    }

    // ================== STATS ENDPOINT ==================
    @GetMapping("/stats")
    public ApiStats.ApiStatsSnapshot getStats() {
        return apiStats.snapshot();
    }
}
