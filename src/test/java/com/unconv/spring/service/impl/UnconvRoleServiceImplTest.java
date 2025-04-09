package com.unconv.spring.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.UnconvRoleRepository;
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
class UnconvRoleServiceImplTest {

    @Mock private UnconvRoleRepository unconvRoleRepository;

    @InjectMocks private UnconvRoleServiceImpl unconvRoleService;

    private UnconvRole unconvRole;
    private UUID unconvRoleId;
    private String unconvRoleName;

    @BeforeEach
    void setUp() {
        unconvRoleId = UUID.randomUUID();
        unconvRoleName = "ROLE_NAME";
        unconvRole = new UnconvRole();
        unconvRole.setId(unconvRoleId);
        unconvRole.setName(unconvRoleName);
    }

    @Test
    void findAllUnconvRolesInAscendingOrder() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "ASC";
        List<UnconvRole> unconvRoleList = Collections.singletonList(unconvRole);
        Page<UnconvRole> unconvRolePage = new PageImpl<>(unconvRoleList);

        when(unconvRoleRepository.findAll(any(Pageable.class))).thenReturn(unconvRolePage);

        PagedResult<UnconvRole> result =
                unconvRoleService.findAllUnconvRoles(pageNo, pageSize, sortBy, sortDir);

        assertEquals(unconvRoleList.size(), result.data().size());
        assertEquals(unconvRoleList.get(0).getId(), result.data().get(0).getId());
    }

    @Test
    void findAllUnconvRolesInDescendingOrder() {
        int pageNo = 0;
        int pageSize = 10;
        String sortBy = "id";
        String sortDir = "DESC";
        List<UnconvRole> unconvRoleList = Collections.singletonList(unconvRole);
        Page<UnconvRole> unconvRolePage = new PageImpl<>(unconvRoleList);

        when(unconvRoleRepository.findAll(any(Pageable.class))).thenReturn(unconvRolePage);

        PagedResult<UnconvRole> result =
                unconvRoleService.findAllUnconvRoles(pageNo, pageSize, sortBy, sortDir);

        assertEquals(unconvRoleList.size(), result.data().size());
        assertEquals(unconvRoleList.get(0).getId(), result.data().get(0).getId());
    }

    @Test
    void findUnconvRoleById() {
        when(unconvRoleRepository.findById(any(UUID.class))).thenReturn(Optional.of(unconvRole));

        Optional<UnconvRole> result = unconvRoleService.findUnconvRoleById(unconvRoleId);

        assertTrue(result.isPresent());
        assertEquals(unconvRole.getId(), result.get().getId());
    }

    @Test
    void findUnconvRoleByName() {
        when(unconvRoleRepository.findByName(any(String.class))).thenReturn(unconvRole);

        UnconvRole resultUnconvRole = unconvRoleService.findUnconvRoleByName(unconvRoleName);

        assertEquals(unconvRole.getId(), resultUnconvRole.getId());
        assertEquals(unconvRole.getName(), resultUnconvRole.getName());
    }

    @Test
    void saveUnconvRole() {
        when(unconvRoleRepository.save(any(UnconvRole.class))).thenReturn(unconvRole);

        UnconvRole result = unconvRoleService.saveUnconvRole(unconvRole);

        assertEquals(unconvRole.getId(), result.getId());
    }

    @Test
    void deleteUnconvRoleById() {
        unconvRoleService.deleteUnconvRoleById(unconvRoleId);

        verify(unconvRoleRepository, times(1)).deleteById(unconvRoleId);
    }
}
