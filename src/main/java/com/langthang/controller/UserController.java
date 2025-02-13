package com.langthang.controller;

import com.langthang.annotation.PasswordMatches;
import com.langthang.annotation.ValidEmail;
import com.langthang.dto.AccountDTO;
import com.langthang.dto.AccountInfoDTO;
import com.langthang.dto.PasswordDTO;
import com.langthang.dto.PostResponseDTO;
import com.langthang.services.IPostServices;
import com.langthang.services.IUserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;

@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@RestController
@Validated
@CacheConfig(cacheNames = "userCache")
public class UserController {

    private final IUserServices userServices;

    private final IPostServices postServices;

    @GetMapping("/user/{account_id}")
    public ResponseEntity<Object> getInformationOfUser(
            @PathVariable("account_id") int accountId) {

        AccountDTO accountDTO = userServices.getDetailInformation(accountId);

        return ResponseEntity.ok(accountDTO);
    }

    @GetMapping(value = "/user", params = {"email"})
    public ResponseEntity<Object> getInformationOfUser(
            @RequestParam("email") @ValidEmail String email) {

        AccountDTO accountDTO = userServices.getDetailInformation(email);

        return ResponseEntity.ok(accountDTO);
    }

    @GetMapping("/whoami")
    @PreAuthorize("isAuthenticated()")
    @Cacheable(key = "{@securityUtils.getLoggedInEmail()}")
    public ResponseEntity<Object> getCurrentUserInfo(
            Authentication authentication) {

        String currentEmail = authentication.getName();

        AccountDTO accountDTO = userServices.getDetailInformation(currentEmail);

        return ResponseEntity.ok(accountDTO);
    }

    @GetMapping("/user/posts/{account_id}")
    public ResponseEntity<Object> getAllPostsOfUser(
            @PathVariable("account_id") int accountId,
            @PageableDefault(sort = {"publishedDate"}) Pageable pageable) {

        List<PostResponseDTO> responseList = postServices.getAllPostOfUser(accountId, pageable);

        return ResponseEntity.ok(responseList);
    }

    @GetMapping(value = "/user/posts", params = {"email"})
    public ResponseEntity<Object> getAllPostsOfUser(
            @RequestParam("email") String accountEmail,
            @PageableDefault(sort = {"publishedDate"}) Pageable pageable) {

        List<PostResponseDTO> responseList = postServices.getAllPostOfUser(accountEmail, pageable);

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/user/posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> getAllPostsOfCurrentUser(
            @PageableDefault(sort = {"publishedDate"}) Pageable pageable,
            Authentication authentication) {

        String accountEmail = authentication.getName();

        List<PostResponseDTO> responseList = postServices.getAllPostOfUser(accountEmail, pageable);

        return ResponseEntity.ok(responseList);
    }


    @GetMapping("/user/drafts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> getAllDraftsOfUser(
            @PageableDefault(sort = {"publishedDate"}) Pageable pageable,
            Authentication authentication) {

        String accountEmail = authentication.getName();

        List<PostResponseDTO> draftList = postServices.getAllDraftOfUser(accountEmail, pageable);

        return ResponseEntity.ok(draftList);
    }

    @PutMapping("/user/follow/{account_id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> followOrUnfollow(
            @PathVariable("account_id") int accountId,
            Authentication authentication) {

        String currentAccount = authentication.getName();

        int currentFollowCount = userServices.followOrUnfollow(currentAccount, accountId);

        return ResponseEntity.accepted().body(currentFollowCount);
    }

    @GetMapping("/user/{account_id}/follow")
    @ResponseStatus(HttpStatus.OK)
    public Object getFollowers(
            @PathVariable("account_id") int accountId,
            @PageableDefault(sort = {"id"}) Pageable pageable) {

        return userServices.getFollower(accountId, pageable);
    }

    @PutMapping("/user/update/info")
    @PreAuthorize("isAuthenticated()")
    @CacheEvict(key = "{@securityUtils.getLoggedInEmail()}")
    public ResponseEntity<Object> updateUserBasicInfo(
            @Valid AccountInfoDTO newInfo,
            Authentication authentication) {

        String currentEmail = authentication.getName();

        AccountDTO updated = userServices.updateBasicInfo(currentEmail, newInfo);

        return ResponseEntity.accepted().body(updated);
    }

    @PutMapping("/user/update/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> updateUserPassword(
            @RequestParam("oldPassword") @Size(min = 6, max = 32) String oldPassword,
            @Valid @PasswordMatches PasswordDTO newPassword,
            Authentication authentication) {

        String currentEmail = authentication.getName();

        userServices.checkEmailAndPassword(currentEmail, oldPassword);

        userServices.updatePassword(currentEmail, newPassword.getPassword());

        return ResponseEntity.accepted().build();
    }
}
