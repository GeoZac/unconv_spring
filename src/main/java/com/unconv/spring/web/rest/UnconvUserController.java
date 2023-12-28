package com.unconv.spring.web.rest;

import static com.unconv.spring.consts.MessageConstants.USER_PROVIDE_PASSWORD;
import static com.unconv.spring.consts.MessageConstants.USER_UPDATE_SUCCESS;
import static com.unconv.spring.consts.MessageConstants.USER_WRONG_PASSWORD;

import com.unconv.spring.consts.AppConstants;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.UnconvUserDTO;
import com.unconv.spring.model.response.MessageResponse;
import com.unconv.spring.model.response.PagedResult;
import com.unconv.spring.service.UnconvUserService;
import java.util.UUID;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/UnconvUser")
@Slf4j
public class UnconvUserController {

    @Autowired private UnconvUserService unconvUserService;

    @Autowired private ModelMapper modelMapper;

    @GetMapping
    public PagedResult<UnconvUser> getAllUnconvUsers(
            @RequestParam(
                            value = "pageNo",
                            defaultValue = AppConstants.DEFAULT_PAGE_NUMBER,
                            required = false)
                    int pageNo,
            @RequestParam(
                            value = "pageSize",
                            defaultValue = AppConstants.DEFAULT_PAGE_SIZE,
                            required = false)
                    int pageSize,
            @RequestParam(
                            value = "sortBy",
                            defaultValue = AppConstants.DEFAULT_SORT_BY,
                            required = false)
                    String sortBy,
            @RequestParam(
                            value = "sortDir",
                            defaultValue = AppConstants.DEFAULT_SORT_DIRECTION,
                            required = false)
                    String sortDir) {
        return unconvUserService.findAllUnconvUsers(pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnconvUser> getUnconvUserById(@PathVariable UUID id) {
        return unconvUserService
                .findUnconvUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MessageResponse<UnconvUserDTO>> createUnconvUser(
            @RequestBody @Validated UnconvUserDTO unconvUserDTO) {
        unconvUserDTO.setId(null);
        return unconvUserService.setUpRoleAndSaveAfterVerify(unconvUserDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse<UnconvUserDTO>> updateUnconvUser(
            @PathVariable UUID id, @RequestBody @Valid UnconvUserDTO unconvUserDTO) {

        return unconvUserService
                .findUnconvUserById(id)
                .map(
                        unconvUserObj -> {
                            if (unconvUserDTO.getCurrentPassword() == null) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(
                                                new MessageResponse<>(
                                                        unconvUserDTO, USER_PROVIDE_PASSWORD));
                            }
                            if (unconvUserService.checkPasswordMatch(
                                    unconvUserObj.getId(), unconvUserDTO.getCurrentPassword())) {
                                unconvUserDTO.setId(id);
                                unconvUserDTO.setUsername(unconvUserObj.getUsername());
                                UnconvUser unconvUser =
                                        unconvUserService.saveUnconvUser(
                                                modelMapper.map(unconvUserDTO, UnconvUser.class),
                                                unconvUserDTO.getPassword());
                                unconvUser.setPassword(null);
                                return ResponseEntity.ok(
                                        new MessageResponse<>(
                                                modelMapper.map(unconvUser, UnconvUserDTO.class),
                                                USER_UPDATE_SUCCESS));
                            } else {
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(
                                                new MessageResponse<>(
                                                        unconvUserDTO, USER_WRONG_PASSWORD));
                            }
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UnconvUser> deleteUnconvUser(@PathVariable UUID id) {
        return unconvUserService
                .findUnconvUserById(id)
                .map(
                        unconvUser -> {
                            unconvUserService.deleteUnconvUserById(id);
                            return ResponseEntity.ok(unconvUser);
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
