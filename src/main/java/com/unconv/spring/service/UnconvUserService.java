package com.unconv.spring.service;

import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.persistence.UnconvUserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UnconvUserService {

    @Autowired private UnconvUserRepository unconvUserRepository;

    public PagedResult<UnconvUser> findAllUnconvUsers(
            int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<UnconvUser> unconvUsersPage = unconvUserRepository.findAll(pageable);

        return new PagedResult<>(unconvUsersPage);
    }

    public Optional<UnconvUser> findUnconvUserById(UUID id) {
        return unconvUserRepository.findById(id);
    }

    public UnconvUser saveUnconvUser(UnconvUser unconvUser) {
        return unconvUserRepository.save(unconvUser);
    }

    public void deleteUnconvUserById(UUID id) {
        unconvUserRepository.deleteById(id);
    }
}
