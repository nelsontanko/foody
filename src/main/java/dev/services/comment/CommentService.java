package dev.services.comment;

import dev.account.user.User;
import dev.core.exception.ErrorCode;
import dev.core.exception.GenericApiException;
import dev.services.food.Food;
import dev.services.food.FoodRepository;
import dev.services.util.AuthenticatedUser;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Nelson Tanko
 */
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final FoodRepository foodRepository;
    private final CommentMapper commentMapper;
    private final AuthenticatedUser auth;


    public CommentService(CommentRepository commentRepository, FoodRepository foodRepository, CommentMapper commentMapper, AuthenticatedUser auth) {
        this.commentRepository = commentRepository;
        this.foodRepository = foodRepository;
        this.commentMapper = commentMapper;
        this.auth = auth;
    }

    @Transactional
    public CommentDTO.Response addComment(CommentDTO.Request commentDTO) {
        Food food = foodRepository.findById(commentDTO.getFoodId())
                .orElseThrow(() -> new GenericApiException(ErrorCode.FOOD_NOT_FOUND));

        User user = auth.getAuthenticatedUser();

        Comment comment = commentMapper.toEntity(commentDTO);
        comment.setFood(food);
        comment.setUser(user);

        comment = commentRepository.save(comment);
        return commentMapper.toDto(comment);
    }

    @Transactional(readOnly = true)
    public CommentPageDTO getCommentsByFoodId(Long foodId, int page, int size) {
        if (!foodRepository.existsById(foodId)) {
            throw new GenericApiException(ErrorCode.FOOD_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Comment> commentsPage = commentRepository.findByFoodId(foodId, pageable);

        List<CommentDTO.Response> commentDTOs = commentMapper.toDtoList(commentsPage.getContent());

        return new CommentPageDTO(commentDTOs, commentsPage.getNumber(), commentsPage.getTotalPages(),
                commentsPage.getTotalElements(), commentsPage.getSize()
        );
    }
}
