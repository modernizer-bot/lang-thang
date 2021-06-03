package com.langthang.services;

import com.langthang.dto.PostRequestDTO;
import com.langthang.dto.PostResponseDTO;
import com.langthang.model.Post;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IPostServices {
    PostResponseDTO addNewPostOrDraft(PostRequestDTO postRequestDTO, String authorEmail, boolean isDraft);

    PostResponseDTO getPostDetailById(int postId);

    PostResponseDTO getPostDetailBySlug(String slug);

    PostResponseDTO getDraftById(int postId, String authorEmail);

    List<PostResponseDTO> getPreviewPost(Pageable pageable);

    List<PostResponseDTO> findPostByKeyword(String keyword, Pageable pageable);

    List<PostResponseDTO> getAllPostOfCategory(int categoryId, Pageable pageable);

    List<PostResponseDTO> getPopularPostByProperty(String propertyName, int size);

    List<PostResponseDTO> getAllPostOfUser(int accountId, Pageable pageable);

    List<PostResponseDTO> getAllPostOfUser(String accountEmail, Pageable pageable);

    List<PostResponseDTO> getAllDraftOfUser(String accountEmail, Pageable pageable);

    List<PostResponseDTO> getBookmarkedPostOfUser(String accEmail, Pageable pageable);

    Post verifyResourceOwner(int postId, String authorEmail);

    PostResponseDTO getPostOrDraftContent(String slug, String authorEmail);

    void deletePostById(int postId, String authorEmail, boolean isAdmin);

    String updatePostById(int postId, String authorEmail, PostRequestDTO postRequestDTO);

    void updateDraftById(int postId, String authorEmail, PostRequestDTO postRequestDTO);
}
