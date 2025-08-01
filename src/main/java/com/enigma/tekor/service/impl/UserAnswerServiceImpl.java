package com.enigma.tekor.service.impl;

import com.enigma.tekor.dto.request.SaveAnswerRequest;
import com.enigma.tekor.entity.Option;
import com.enigma.tekor.entity.Question;
import com.enigma.tekor.entity.TestAttempt;
import com.enigma.tekor.entity.UserAnswer;
import com.enigma.tekor.service.OptionService;
import com.enigma.tekor.service.QuestionService;
import com.enigma.tekor.repository.UserAnswerRepository;
import com.enigma.tekor.service.UserAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAnswerServiceImpl implements UserAnswerService {

    private final UserAnswerRepository userAnswerRepository;
    private final QuestionService questionService;
    private final OptionService optionService;

    @Override
    public UserAnswer saveAnswer(SaveAnswerRequest request, TestAttempt attempt) {
        Question question = questionService.getById(request.getQuestionId());
        Option chosenOption = optionService.getById(request.getOptionId());

        UserAnswer userAnswer = userAnswerRepository
                .findByTestAttemptAndQuestion(attempt, question)
                .orElse(new UserAnswer());

        userAnswer.setTestAttempt(attempt);
        userAnswer.setQuestion(question);
        userAnswer.setSelectedOption(chosenOption);
        userAnswer.setIsCorrect(chosenOption.getIsCorrect());

        return userAnswerRepository.save(userAnswer);
    }

    @Override
    public Integer calculateScore(TestAttempt attempt) {
        Integer totalQuestions = attempt.getTestPackage().getQuestions().size();

        List<UserAnswer> userAnswers = userAnswerRepository.findByTestAttemptId(attempt.getId());
        int correctAnswers = 0;
        for (UserAnswer userAnswer : userAnswers) {
            if (userAnswer.getIsCorrect()) {
                correctAnswers++;
            }
        }
        if (totalQuestions == 0) return 0;
        return (int) Math.round((double) correctAnswers / totalQuestions * 100);
    }
}
