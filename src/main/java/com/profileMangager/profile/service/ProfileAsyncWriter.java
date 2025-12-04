package com.profileMangager.profile.service;


import com.profileMangager.profile.entity.Profile;
import com.profileMangager.profile.repository.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;

@Component
public class ProfileAsyncWriter {
    private static final Logger log = LoggerFactory.getLogger(ProfileAsyncWriter.class);

    private final ProfileRepository profileRepository;

    public ProfileAsyncWriter(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Async("profileExecutor")
    @Transactional
    public void saveToDbAsync(Profile profile) {
        log.info("[ASYNC-SAVE] start id={} | thread={}",
                profile.getUserId(), Thread.currentThread().getName());
        try {
            profileRepository.save(profile);
            log.info("[ASYNC-SAVE] done  id={} | thread={}",
                    profile.getUserId(), Thread.currentThread().getName());
        } catch (Exception e) {
            log.error("[ASYNC-SAVE] ERROR id={} | thread={}",
                    profile.getUserId(), Thread.currentThread().getName(), e);
        }
    }
    @Async("profileExecutor")
    @Transactional
    public void deleteFromDbAsync(Long profileId) {
        log.info("[ASYNC-DELETE] start id={} | thread={}",
                profileId, Thread.currentThread().getName());
        try {
            profileRepository.deleteById(profileId);
            log.info("[ASYNC-DELETE] done  id={} | thread={}",
                    profileId, Thread.currentThread().getName());
        } catch (Exception e) {
            log.error("[ASYNC-DELETE] ERROR id={} | thread={}",
                    profileId, Thread.currentThread().getName(), e);
        }
    }

}
