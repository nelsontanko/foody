package dev.services.comment;

import dev.services.comment.CommentDTO.Request;
import dev.services.comment.CommentDTO.Response;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Nelson Tanko
 */
@RestController
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<Response> addComment(@Valid @RequestBody Request request) {
        Response savedComment = commentService.addComment(request);
        return new ResponseEntity<>(savedComment, HttpStatus.CREATED);
    }

    @GetMapping("/food/{foodId}")
    public ResponseEntity<CommentPageDTO> getCommentsByFoodId(@PathVariable Long foodId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "1") int size) {
        CommentPageDTO commentPage = commentService.getCommentsByFoodId(foodId, page, size);
        return ResponseEntity.ok(commentPage);
    }
}
