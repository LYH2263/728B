package com.steam.service;

import com.steam.dto.PageResult;
import com.steam.entity.GameAnswer;
import com.steam.entity.GameQuestion;
import com.steam.mapper.GameAnswerLikeMapper;
import com.steam.mapper.GameAnswerMapper;
import com.steam.mapper.GameQuestionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameQAService {

    private final GameQuestionMapper questionMapper;
    private final GameAnswerMapper answerMapper;
    private final GameAnswerLikeMapper answerLikeMapper;
    private final RateLimitService rateLimitService;

    public PageResult<GameQuestion> getQuestions(Long gameId, String filter, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<GameQuestion> questions;
        Long total;

        if ("pending".equals(filter)) {
            questions = questionMapper.findByGameIdAndResolved(gameId, 0, offset, size);
            total = questionMapper.countByGameIdAndResolved(gameId, 0);
        } else if ("resolved".equals(filter)) {
            questions = questionMapper.findByGameIdAndResolved(gameId, 1, offset, size);
            total = questionMapper.countByGameIdAndResolved(gameId, 1);
        } else if ("hot".equals(filter)) {
            questions = questionMapper.findHotByGameId(gameId, offset, size);
            total = questionMapper.countByGameId(gameId);
        } else {
            questions = questionMapper.findByGameId(gameId, offset, size);
            total = questionMapper.countByGameId(gameId);
        }

        return PageResult.of(questions, total, page, size);
    }

    @Transactional
    public GameQuestion getQuestionDetail(Long questionId) {
        questionMapper.incrementViewCount(questionId);
        return questionMapper.findById(questionId);
    }

    public List<GameAnswer> getAnswers(Long questionId, Long currentUserId) {
        if (currentUserId != null) {
            return answerMapper.findByQuestionIdWithLikeStatus(questionId, currentUserId);
        }
        return answerMapper.findByQuestionId(questionId);
    }

    @Transactional
    public GameQuestion createQuestion(Long userId, Long gameId, String title, String content) {
        if (title == null || title.trim().isEmpty()) {
            throw new RuntimeException("问题标题不能为空");
        }
        if (title.length() > 200) {
            throw new RuntimeException("问题标题不能超过200字");
        }
        if (content != null && content.length() > 2000) {
            throw new RuntimeException("问题内容不能超过2000字");
        }

        if (!rateLimitService.isAllowed("question_create", userId, 5, 3600)) {
            throw new RuntimeException("操作过于频繁，每小时最多提问5次");
        }

        GameQuestion question = new GameQuestion();
        question.setUserId(userId);
        question.setGameId(gameId);
        question.setTitle(title.trim());
        question.setContent(content != null ? content.trim() : null);

        questionMapper.insert(question);
        log.info("用户 {} 在游戏 {} 发布了问题: {}", userId, gameId, title);
        return question;
    }

    @Transactional
    public GameAnswer createAnswer(Long userId, Long questionId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("回答内容不能为空");
        }
        if (content.length() > 2000) {
            throw new RuntimeException("回答内容不能超过2000字");
        }

        GameQuestion question = questionMapper.findById(questionId);
        if (question == null) {
            throw new RuntimeException("问题不存在");
        }
        if (question.getIsResolved() != null && question.getIsResolved() == 1) {
            throw new RuntimeException("该问题已解决，无法再回答");
        }

        if (!rateLimitService.isAllowed("answer_create", userId, 10, 3600)) {
            throw new RuntimeException("操作过于频繁，每小时最多回答10次");
        }

        GameAnswer answer = new GameAnswer();
        answer.setUserId(userId);
        answer.setQuestionId(questionId);
        answer.setGameId(question.getGameId());
        answer.setContent(content.trim());

        answerMapper.insert(answer);
        questionMapper.incrementAnswerCount(questionId);

        log.info("用户 {} 回答了问题 {}: {}", userId, questionId, content.substring(0, Math.min(30, content.length())));
        return answer;
    }

    @Transactional
    public void toggleLikeAnswer(Long userId, Long answerId) {
        GameAnswer answer = answerMapper.findById(answerId);
        if (answer == null) {
            throw new RuntimeException("回答不存在");
        }

        if (!rateLimitService.isAllowed("answer_like", userId, 20, 60)) {
            throw new RuntimeException("操作过于频繁，请稍后再试");
        }

        int exists = answerLikeMapper.existsByUserIdAndAnswerId(userId, answerId);
        if (exists > 0) {
            answerLikeMapper.delete(userId, answerId);
            answerMapper.decrementLikeCount(answerId);
        } else {
            answerLikeMapper.insert(userId, answerId);
            answerMapper.incrementLikeCount(answerId);
        }
    }

    @Transactional
    public void adoptAnswer(Long userId, Long answerId) {
        GameAnswer answer = answerMapper.findById(answerId);
        if (answer == null) {
            throw new RuntimeException("回答不存在");
        }

        GameQuestion question = questionMapper.findById(answer.getQuestionId());
        if (question == null) {
            throw new RuntimeException("问题不存在");
        }

        if (!question.getUserId().equals(userId)) {
            throw new RuntimeException("只有提问者才能采纳答案");
        }
        if (question.getIsResolved() != null && question.getIsResolved() == 1) {
            throw new RuntimeException("该问题已采纳过答案");
        }

        if (!rateLimitService.isAllowed("answer_adopt", userId, 10, 3600)) {
            throw new RuntimeException("操作过于频繁，请稍后再试");
        }

        answerMapper.cancelAdoptedByQuestionId(answer.getQuestionId());
        answerMapper.markAdopted(answerId);
        questionMapper.markResolved(answer.getQuestionId());

        log.info("用户 {} 采纳了回答 {}", userId, answerId);
    }
}
