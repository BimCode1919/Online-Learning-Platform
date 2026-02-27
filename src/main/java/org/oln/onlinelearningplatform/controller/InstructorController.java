package org.oln.onlinelearningplatform.controller;

import org.oln.onlinelearningplatform.dto.OptionRequestDTO;
import org.oln.onlinelearningplatform.entity.Course;
import org.oln.onlinelearningplatform.entity.Enrollment;
import org.oln.onlinelearningplatform.entity.Lesson;
import org.oln.onlinelearningplatform.entity.Quiz;
import org.oln.onlinelearningplatform.entity.User;
import org.oln.onlinelearningplatform.repository.CourseRepository;
import org.oln.onlinelearningplatform.repository.LessonRepository;
import org.oln.onlinelearningplatform.repository.QuizRepository;
import org.oln.onlinelearningplatform.service.aiagent.AIQuizService;
import org.oln.onlinelearningplatform.service.aiagent.YouTubeService;
import org.oln.onlinelearningplatform.service.course.CourseService;
import org.oln.onlinelearningplatform.service.quiz.QuestionService;
import org.oln.onlinelearningplatform.service.quiz.QuizService;
import org.oln.onlinelearningplatform.service.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/instructor")
public class InstructorController {

    private final CourseService courseService;
    private final CourseRepository courseRepository;
    private final UserService userService;
    private final QuizService quizService;
    private final QuestionService questionService;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final AIQuizService aiQuizService;
    private final YouTubeService youTubeService;

    public InstructorController(CourseService courseService,
                                CourseRepository courseRepository,
                                UserService userService,
                                QuizService quizService,
                                QuestionService questionService,
                                LessonRepository lessonRepository,
                                QuizRepository quizRepository, AIQuizService aiQuizService, YouTubeService youTubeService) {
        this.courseService = courseService;
        this.courseRepository = courseRepository;
        this.userService = userService;
        this.quizService = quizService;
        this.questionService = questionService;
        this.lessonRepository = lessonRepository;
        this.quizRepository = quizRepository;
        this.aiQuizService = aiQuizService;
        this.youTubeService = youTubeService;
    }

    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User instructor = userService.findByEmail(userDetails.getUsername());
        List<Course> courses = courseService.getCoursesByInstructor(instructor);

        double totalRevenue = courses.stream()
                .flatMap(course -> course.getEnrollments().stream())
                .filter((Enrollment enrollment) -> "COMPLETED".equals(enrollment.getPaymentStatus()))
                .mapToDouble(Enrollment::getInstructorShare)
                .sum();

        String email = userDetails.getUsername();
        model.addAttribute("courses", courseService.getCoursesByInstructorEmail(email));
        model.addAttribute("totalRevenue", totalRevenue);
        return "views/teacher/dashboard";
    }

    @GetMapping("/create-course")
    public String showCreateCourseForm(Model model) {
        model.addAttribute("course", new Course());
        return "views/teacher/course-editor";
    }

    @PostMapping("/save-course")
    public String saveCourse(@ModelAttribute Course course,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            Course savedCourse = courseService.saveOrUpdateCourse(course, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("success", "Lưu thông tin thành công!");
            return "redirect:/instructor/course/" + savedCourse.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/instructor/dashboard";
        }
    }

    @GetMapping("/course/{id}")
    public String editCourse(@PathVariable("id") Long id, Model model) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học với ID: " + id));
        model.addAttribute("course", course);
        return "views/teacher/course-editor";
    }

    @PostMapping("/course/{id}/add-lesson")
    public String addLesson(@PathVariable("id") Long courseId,
                            @RequestParam(value = "lessonId", required = false) Long lessonId,
                            @RequestParam("title") String title,
                            @RequestParam("content") String content,
                            @RequestParam("videoUrl") String videoUrl,
                            RedirectAttributes redirectAttributes) {
        try {
            courseService.addOrUpdateLesson(courseId, lessonId, title, content, videoUrl);
            String msg = (lessonId != null) ? "Cập nhật bài học thành công!" : "Thêm bài học mới thành công!";
            redirectAttributes.addFlashAttribute("success", msg);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi xử lý bài học: " + e.getMessage());
        }
        return "redirect:/instructor/course/" + courseId;
    }

    @PostMapping("/course/{courseId}/delete-lesson/{lessonId}")
    public String deleteLesson(@PathVariable Long courseId,
                               @PathVariable Long lessonId,
                               RedirectAttributes redirectAttributes) {
        try {
            courseService.deleteLesson(lessonId);
            redirectAttributes.addFlashAttribute("success", "Đã xóa bài học thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa: " + e.getMessage());
        }
        return "redirect:/instructor/course/" + courseId;
    }

    @PostMapping("/course/delete/{id}")
    public String deleteCourse(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            courseService.deleteCourse(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa khóa học thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa khóa học: " + e.getMessage());
        }
        return "redirect:/instructor/dashboard";
    }

    // Tạo quiz cho lesson
    @PostMapping("/lesson/{lessonId}/create-quiz")
    public String createQuiz(@PathVariable Long lessonId,
                             @RequestParam String difficulty,
                             RedirectAttributes redirectAttributes) {
        try {
            Quiz quiz = quizService.createQuizForLesson(lessonId, difficulty);
            redirectAttributes.addFlashAttribute("success", "Tạo quiz thành công!");
            return "redirect:/instructor/quiz/" + quiz.getId() + "/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Lesson không tồn tại"));
            return "redirect:/instructor/course/" + lesson.getCourse().getId();
        }
    }

    // Hiển thị trang chỉnh sửa quiz
    @GetMapping("/quiz/{quizId}/edit")
    public String editQuiz(@PathVariable Long quizId, Model model, RedirectAttributes redirectAttributes) {
        System.out.println("=== EDIT QUIZ METHOD CALLED ===");
        System.out.println("Quiz ID: " + quizId);

        try {
            Quiz quiz = quizRepository.findById(quizId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy quiz với ID: " + quizId));

            System.out.println("Quiz found: " + quiz.getId());
            System.out.println("Quiz difficulty: " + quiz.getDifficulty());
            System.out.println("Quiz status: " + quiz.getStatus());
            System.out.println("Course: " + (quiz.getCourse() != null ? quiz.getCourse().getTitle() : "null"));
            System.out.println("Lesson: " + (quiz.getLesson() != null ? quiz.getLesson().getTitle() : "null"));
            System.out.println("Questions count: " + (quiz.getQuestions() != null ? quiz.getQuestions().size() : 0));

            // Kiểm tra và log chi tiết
            if (quiz.getCourse() == null) {
                System.out.println("WARNING: Quiz " + quizId + " has no course associated!");
            }

            if (quiz.getLesson() == null) {
                System.out.println("WARNING: Quiz " + quizId + " has no lesson associated!");
            }

            model.addAttribute("quiz", quiz);
            return "views/teacher/quiz-editor";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/instructor/dashboard";
        }
    }

    // Thêm câu hỏi vào quiz
    @PostMapping("/quiz/{quizId}/add-question")
    public String addQuestion(@PathVariable Long quizId,
                              @RequestParam String questionText,
                              @RequestParam(required = false) String explanation,
                              @RequestParam List<String> optionText,
                              @RequestParam Integer correctIndex,
                              RedirectAttributes redirectAttributes) {
        try {
            List<OptionRequestDTO> optionDTOs = new ArrayList<>();
            for (int i = 0; i < optionText.size(); i++) {
                OptionRequestDTO dto = new OptionRequestDTO();
                dto.setOptionText(optionText.get(i));
                dto.setCorrect(i == correctIndex);
                optionDTOs.add(dto);
            }

            questionService.addQuestion(quizId, questionText, explanation, optionDTOs);
            redirectAttributes.addFlashAttribute("success", "Thêm câu hỏi thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/instructor/quiz/" + quizId + "/edit";
    }

    // Xóa câu hỏi
    @PostMapping("/question/{questionId}/delete")
    public String deleteQuestion(@PathVariable Long questionId,
                                 RedirectAttributes redirectAttributes) {
        try {
            Long quizId = questionService.deleteQuestion(questionId);
            redirectAttributes.addFlashAttribute("success", "Xóa câu hỏi thành công!");
            return "redirect:/instructor/quiz/" + quizId + "/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/instructor/dashboard";
        }
    }

    // Xóa quiz
    @PostMapping("/quiz/{quizId}/delete")
    public String deleteQuiz(@PathVariable Long quizId,
                             RedirectAttributes redirectAttributes) {
        try {
            Quiz quiz = quizRepository.findById(quizId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy quiz"));
            Long courseId = quiz.getLesson().getCourse().getId();

            quizService.deleteQuiz(quizId);
            redirectAttributes.addFlashAttribute("success", "Xóa quiz thành công!");
            return "redirect:/instructor/course/" + courseId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/instructor/dashboard";
        }
    }

    @PostMapping("/generate-from-youtube")
    public String generateFromYoutube(@RequestParam("lessonId") Long lessonId,
                                      @RequestParam("youtubeUrl") String youtubeUrl,
                                      RedirectAttributes redirectAttributes) {
        Long courseId = null;
        try {
            // 0. Lấy thông tin bài học để biết courseId (dùng cho việc redirect nếu lỗi)
            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học"));
            courseId = lesson.getCourse().getId();

            // 1. Trích xuất Transcript từ YouTube
            String transcript = youTubeService.getTranscript(youtubeUrl);

            // 2. Gọi AI xử lý transcript và lưu vào Database
            // Lưu ý: Đảm bảo phương thức này bên Service đã xử lý convertOptions như mình đã viết
            aiQuizService.createQuizFromYoutubeContent(lessonId, transcript);

            // 3. Thông báo thành công (Dùng "success" để khớp với các hàm khác trong Controller)
            redirectAttributes.addFlashAttribute("success", "AI đã soạn 10 câu hỏi trắc nghiệm dựa trên video thành công!");

            return "redirect:/instructor/course/" + courseId;

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi AI: " + e.getMessage());

            // Nếu có courseId thì về trang edit course, không thì về dashboard
            return (courseId != null) ? "redirect:/instructor/course/" + courseId : "redirect:/instructor/dashboard";
        }
    }
}