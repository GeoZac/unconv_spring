package com.unconv.spring.web.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/UnconvUser")
@Slf4j
public class UnconvUserController {
    /*
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
        if (!unconvUserService.isUsernameUnique(unconvUserDTO.getUsername())) {
            unconvUserDTO.setPassword(null);
            return new ResponseEntity<>(
                    new MessageResponse<>(unconvUserDTO, USER_NAME_IN_USE), HttpStatus.BAD_REQUEST);
        }
        unconvUserDTO.setId(null);
        UnconvUserDTO savedUnconvUserDTO = unconvUserService.createUnconvUser(unconvUserDTO);
        return new ResponseEntity<>(
                new MessageResponse<>(savedUnconvUserDTO, USER_CREATE_SUCCESS), HttpStatus.CREATED);
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
                                UnconvUserDTO updatedUnconvUserDTO =
                                        unconvUserService.updateUnconvUser(
                                                unconvUserObj, unconvUserDTO);
                                return ResponseEntity.ok(
                                        new MessageResponse<>(
                                                updatedUnconvUserDTO, USER_UPDATE_SUCCESS));
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
    */
}
