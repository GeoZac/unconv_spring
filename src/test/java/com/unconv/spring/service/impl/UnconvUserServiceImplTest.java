package com.unconv.spring.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.UnconvUserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class UnconvUserServiceImplTest {

    @Mock private UnconvUserRepository unconvUserRepository;

    @InjectMocks private UnconvUserServiceImpl unconvUserService;

    private UnconvUser unconvUser;
    private UUID unconvUserId;

    @BeforeEach
    void setUp() {
        unconvUserId = UUID.randomUUID();
        unconvUser = new UnconvUser();
        unconvUser.setId(unconvUserId);
    }

    @Test
    void findAllUnconvUsers() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "ASC";
        List<UnconvUser> unconvUserList = Collections.singletonList(unconvUser);
        Page<UnconvUser> unconvUserPage = new PageImpl<>(unconvUserList);

        when(unconvUserRepository.findAll(any(Pageable.class))).thenReturn(unconvUserPage);

        PagedResult<UnconvUser> result =
                unconvUserService.findAllUnconvUsers(pageNo, pageSize, sortBy, sortDir);

        assertEquals(unconvUserList.size(), result.data().size());
        assertEquals(unconvUserList.get(0).getId(), result.data().get(0).getId());
    }

    @Test
    void findUnconvUserById() {
        when(unconvUserRepository.findById(any(UUID.class))).thenReturn(Optional.of(unconvUser));

        Optional<UnconvUser> result = unconvUserService.findUnconvUserById(unconvUserId);

        assertTrue(result.isPresent());
        assertEquals(unconvUser.getId(), result.get().getId());
    }

    @Test
    void isUsernameUnique() {
        when(unconvUserRepository.existsByUsername(any(String.class))).thenReturn(true);

        boolean result = unconvUserService.isUsernameUnique("U$erName");

        assertFalse(result);
    }

    @Test
    void findUnconvUserByUserName() {}

    @Test
    void saveUnconvUser() {}

    @Test
    void checkPasswordMatch() {}

    @Test
    void createUnconvUser() {}

    @Test
    void updateUnconvUser() {}

    @Test
    void deleteUnconvUserById() {
        unconvUserService.deleteUnconvUserById(unconvUserId);

        verify(unconvUserRepository, times(1)).deleteById(unconvUserId);
    }

    @Test
    void bCryptPasswordEncoder() {}
}
