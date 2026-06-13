package com.steam.controller;

import com.steam.dto.PageResult;
import com.steam.dto.Result;
import com.steam.entity.GameAnswer;
import com.steam.entity.GameQuestion;
import com.steam.service.GameQAService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/game-qa")
@RequiredArgsConstructor
public class GameQAController {

    private final GameQAService gameQAService;

    @GetMapping("/questions/game/{gameId}")
    public Result<PageResult<GameQuestion>> getQuestions(
            @PathVariable Long gameId,
            @RequestParam(required = false, defaultValue = "all") String filter,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResult<GameQuestion> result = gameQAService.getQuestions(gameId, filter, page, size);
        return Result.success(result);
    }

    @GetMapping("/questions/{questionId}")
    public Result<GameQuestion> getQuestionDetail(@PathVariable Long questionId) {
        GameQuestion question = gameQAService.getQuestionDetail(questionId);
        return Result.success(question);
    }

    @GetMapping("/questions/{questionId}/answers")
    public Result<List<GameAnswer>> getAnswers(
            @PathVariable Long questionId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<GameAnswer> answers = gameQAService.getAnswers(questionId, userId);
        return Result.success(answers);
    }

    @PostMapping("/questions")
    public Result<GameQuestion> createQuestion(
            HttpServletRequest request,
            @RequestBody Map<String, Object> body) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Long gameId = Long.parseLong(body.get("gameId").toString());
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        GameQuestion question = gameQAService.createQuestion(userId, gameId, title, content);
        return Result.success("提问成功", question);
    }

    @PostMapping("/answers")
    public Result<GameAnswer> createAnswer(
            HttpServletRequest request,
            @RequestBody Map<String, Object> body) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Long questionId = Long.parseLong(body.get("questionId").toString());
        String content = (String) body.get("content");
        GameAnswer answer = gameQAService.createAnswer(userId, questionId, content);
        return Result.success("回答成功", answer);
    }

    @PostMapping("/answers/{answerId}/like")
    public Result<Void> toggleLikeAnswer(
            HttpServletRequest request,
            @PathVariable Long answerId) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        gameQAService.toggleLikeAnswer(userId, answerId);
        return Result.successMessage("操作成功");
    }

    @PostMapping("/answers/{answerId}/adopt")
    public Result<Void> adoptAnswer(
            HttpServletRequest request,
            @PathVariable Long answerId) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        gameQAService.adoptAnswer(userId, answerId);
        return Result.successMessage("采纳成功");
    }
}
