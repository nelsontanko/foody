package dev.services.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Nelson Tanko
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentPageDTO {
    private List<CommentDTO.Response> comments;
    private int currentPage;
    private int totalPages;
    private long totalComments;
    private int size;
}
