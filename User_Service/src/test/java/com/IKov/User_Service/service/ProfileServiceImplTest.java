package com.IKov.User_Service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.IKov.User_Service.config.MailConfig;
import com.IKov.User_Service.config.PasswordEncoderConfig;
import com.IKov.User_Service.config.RedisConfig;
import com.IKov.User_Service.entity.Profile.Profile;
import com.IKov.User_Service.entity.Profile.Status;
import com.IKov.User_Service.repository.ProfileRepository;
import com.IKov.User_Service.service.Impl.ProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ProfileServiceImpl.class)
@ImportAutoConfiguration(exclude = {
        MailConfig.class,
        PasswordEncoderConfig.class,
        RedisConfig.class
})
@ActiveProfiles("test")
public class ProfileServiceImplTest {

    @Autowired
    private ProfileServiceImpl profileService;

    @MockitoBean
    private MailSender mailSender;

    @MockitoBean
    private ProfileRepository profileRepository;

    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @BeforeEach
    void setUp() {
        // Mock the opsForValue() to return our valueOperations mock
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void saveProfileTest() {
        // Arrange
        Profile input = new Profile();
        input.setEmail("user@example.com");
        input.setPassword("plainpwd");

        // Mock password encoding
        when(passwordEncoder.encode("plainpwd")).thenReturn("encodedpwd");

        // Simulate saving: set ID on returned profile
        Profile saved = new Profile();
        saved.setId(123L);
        saved.setEmail(input.getEmail());
        saved.setPassword("encodedpwd");
        saved.setStatus(Status.UNCONFIRMED);
        when(profileRepository.save(any(Profile.class))).thenReturn(saved);

        // Act
        boolean result = profileService.saveProfile(input);

        // Assert
        assertTrue(result);

        // Verify encoding
        verify(passwordEncoder, times(1)).encode("plainpwd");
        // Verify repository save received profile with encoded password and UNCONFIRMED status
        verify(profileRepository).save(argThat(p ->
                Objects.equals(p.getPassword(), "encodedpwd") &&
                        p.getStatus() == Status.UNCONFIRMED
        ));
        // Verify redis calls
        verify(valueOperations).set(eq("user@example.com"), any(Integer.class));
        verify(redisTemplate).expire(eq("user@example.com"), eq(30L), eq(TimeUnit.MINUTES));
        // Verify mail sender
        verify(mailSender).sendAuthConfirmation(eq("user@example.com"), any(Integer.class));
    }

    @Test
    void confirmProfileTest_whenCodeMatches_thenStatusUpdated() {
        // Arrange
        String email = "user@example.com";
        Integer code = 654321;
        // Mock stored code
        when(valueOperations.get(email)).thenReturn(code);

        // Act
        boolean confirmed = profileService.confirmProfile(email, code);

        // Assert
        assertTrue(confirmed);
        verify(profileRepository).updateProfileStatus(email, Status.CONFIRMED);
    }

    @Test
    void confirmProfileTest_whenCodeDoesNotMatch_thenNoUpdate() {
        // Arrange
        String email = "user@example.com";
        when(valueOperations.get(email)).thenReturn(111111);

        // Act
        boolean confirmed = profileService.confirmProfile(email, 123456);

        // Assert
        assertFalse(confirmed);
        verify(profileRepository, never()).updateProfileStatus(anyString(), any());
    }
}
