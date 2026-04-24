package com.unconv.spring.service.impl;

import static com.unconv.spring.enums.DefaultUserRole.UNCONV_USER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.UnconvUserDTO;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.UnconvUserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class UnconvUserServiceImplTest {
    @Spy private ModelMapper modelMapper;

    @Mock private UnconvUserRepository unconvUserRepository;

    @InjectMocks private UnconvUserServiceImpl unconvUserService;

    @Mock private UnconvRoleServiceImpl unconvRoleService;

    private UnconvUser unconvUser;
    private UUID unconvUserId;

    @BeforeEach
    void setUp() {
        unconvUserId = UUID.randomUUID();
        unconvUser = new UnconvUser();
        unconvUser.setId(unconvUserId);
    }

    @Test
    void findAllUnconvUsersInAscendingOrder() {
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
    void findAllUnconvUsersInDescendingOrder() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "DESC";
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
    void isUsernameUniqueWhenUnique() {
        when(unconvUserRepository.existsByUsername(any(String.class))).thenReturn(true);

        boolean result = unconvUserService.isUsernameUnique("U$erName");

        assertFalse(result);
    }

    @Test
    void isUsernameUniqueWhenNotUnique() {
        when(unconvUserRepository.existsByUsername(any(String.class))).thenReturn(false);

        boolean result = unconvUserService.isUsernameUnique("U$erName");

        assertTrue(result);
    }

    @Test
    void findUnconvUserByUserName() {
        when(unconvUserRepository.findByUsername(any(String.class))).thenReturn(unconvUser);

        UnconvUser result = unconvUserService.findUnconvUserByUserName("U$erName");
        assertEquals(unconvUser.getId(), result.getId());
    }

    @Test
    void saveUnconvUser() {

        when(unconvUserRepository.save(any(UnconvUser.class))).thenReturn(unconvUser);

        UnconvUser result = unconvUserService.saveUnconvUser(unconvUser, "Pa$sW0rd");
        assertEquals(unconvUser.getId(), result.getId());
    }

    @Test
    void checkPasswordMatch() {
        String encodedPass = unconvUserService.bCryptPasswordEncoder().encode("Pa$sw0rd");
        unconvUser.setPassword(encodedPass);
        when(unconvUserRepository.findUnconvUserById(unconvUserId)).thenReturn(unconvUser);

        boolean result = unconvUserService.checkPasswordMatch(unconvUserId, "Pa$sw0rd");
        assertTrue(result);
    }

    @Test
    void createUnconvUser() {
        unconvUser.setUsername("NewUnconvUser");
        unconvUser.setEmail("newuser@email.com");
        unconvUser.setPassword("Pa$sw0rd");
        UnconvUserDTO unconvUserDTO = modelMapper.map(unconvUser, UnconvUserDTO.class);

        when(unconvUserRepository.save(any(UnconvUser.class))).thenReturn(unconvUser);

        UnconvUserDTO result = unconvUserService.createUnconvUser(unconvUserDTO);

        assertEquals(unconvUser.getId(), result.getId());
    }

    @Test
    void updateUnconvUser() {
        UnconvUser user = new UnconvUser();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setUnconvRoles(
                Set.of(
                        UnconvRole.create(
                                UUID.randomUUID(), UNCONV_USER.toString(), this.getClass())));

        UnconvUserDTO dto = new UnconvUserDTO();
        dto.setPassword("newPass");

        UnconvUser savedUser = new UnconvUser();
        savedUser.setId(user.getId());
        savedUser.setUsername("testuser");
        user.setUnconvRoles(
                Set.of(
                        UnconvRole.create(
                                UUID.randomUUID(), UNCONV_USER.toString(), this.getClass())));

        when(unconvUserRepository.save(any(UnconvUser.class))).thenReturn(savedUser);

        UnconvUserDTO result = unconvUserService.updateUnconvUser(user, dto);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertNull(result.getPassword());
    }

    @Test
    void deleteUnconvUserById() {
        unconvUserService.deleteUnconvUserById(unconvUserId);

        verify(unconvUserRepository, times(1)).deleteById(unconvUserId);
    }
}
