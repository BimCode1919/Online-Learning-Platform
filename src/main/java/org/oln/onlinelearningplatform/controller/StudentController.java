package org.oln.onlinelearningplatform.controller;


import org.oln.onlinelearningplatform.entity.Course;
import org.oln.onlinelearningplatform.entity.DashboardStatsDTO;
import org.oln.onlinelearningplatform.entity.Lesson;
import org.oln.onlinelearningplatform.entity.User;
import org.oln.onlinelearningplatform.service.course.CourseService;
import org.oln.onlinelearningplatform.service.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/student")

public class StudentController {

    private final CourseService courseService;
    private final UserService userService;

    public StudentController(CourseService courseService, UserService userService) {
        this.courseService = courseService;
        this.userService = userService;
    }

    // Trang chủ của Student - hiển thị tất cả các courses với URL: /student/courses
    @GetMapping("/courses")
    public String viewAllCourses(Model model,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        // Lấy tất cả courses
        List<Course> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses);

        // Lấy thông tin user đang login (để hiển thị tên)
        String email = userDetails.getUsername(); // username chính là email
        Optional<User> userOpt = userService.findByUsername(email);
        userOpt.ifPresent(user -> model.addAttribute("currentUser", user));

        return "student/courses"; // Trả về template: templates/student/courses.html
    }

    /**
     * Xem chi tiết một course và danh sách lessons
     * URL: /student/courses/{courseId}
     */
    @GetMapping("/courses/{courseId}")
    public String viewCourseDetail(@PathVariable Long courseId,
                                   Model model,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        // Tìm course
        Optional<Course> courseOpt = courseService.getCourseById(courseId);

        if (courseOpt.isEmpty()) {
            model.addAttribute("error", "Course không tồn tại!");
            return "error/404"; // Trang 404
        }

        Course course = courseOpt.get();
        model.addAttribute("course", course);

        // Lấy danh sách lessons của course này
        List<Lesson> lessons = courseService.getLessonsByCourseId(courseId);
        model.addAttribute("lessons", lessons);

        // Lấy user hiện tại để check progress
        String email = userDetails.getUsername();
        Optional<User> userOpt = userService.findByUsername(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("currentUser", user);

            // Đánh dấu lessons nào đã completed
            for (Lesson lesson : lessons) {
                boolean completed = courseService.isLessonCompletedByUser(user.getId(), lesson.getId());
                lesson.setOrderIndex(completed ? 1 : 0); // Hack nhỏ: dùng orderIndex làm flag completed
                // Note: Trong thực tế nên tạo DTO riêng thay vì hack như này
            }
        }

        return "student/course-detail"; // templates/student/course-detail.html
    }

    /**
     * Xem nội dung chi tiết của một lesson
     * URL: /student/lessons/{lessonId}
     */
    @GetMapping("/lessons/{lessonId}")
    public String viewLessonContent(@PathVariable Long lessonId,
                                    Model model,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        // Tìm lesson
        Optional<Lesson> lessonOpt = courseService.getLessonById(lessonId);

        if (lessonOpt.isEmpty()) {
            model.addAttribute("error", "Lesson không tồn tại!");
            return "error/404";
        }

        Lesson lesson = lessonOpt.get();
        model.addAttribute("lesson", lesson);

        // Lấy thông tin course để hiển thị breadcrumb
        model.addAttribute("course", lesson.getCourse());

        // Check xem user đã complete lesson này chưa
        String email = userDetails.getUsername();
        Optional<User> userOpt = userService.findByUsername(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean completed = courseService.isLessonCompletedByUser(user.getId(), lessonId);
            model.addAttribute("isCompleted", completed);
            model.addAttribute("currentUser", user);
        }

        return "student/lesson-content"; // templates/student/lesson-content.html
    }

    /**
     * Đánh dấu lesson là completed
     * URL: POST /student/lessons/{lessonId}/complete
     */
    @PostMapping("/lessons/{lessonId}/complete")
    public String markLessonCompleted(@PathVariable Long lessonId,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      RedirectAttributes redirectAttributes) {
        // Lấy user hiện tại
        String email = userDetails.getUsername();
        Optional<User> userOpt = userService.findByUsername(email);

        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy user!");
            return "redirectstudent/courses";
        }

        User user = userOpt.get();

        // Gọi service để mark completed
        try {
            courseService.markLessonAsCompleted(user.getId(), lessonId);
            redirectAttributes.addFlashAttribute("success", "Đã hoàn thành lesson!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        // Redirect về trang lesson
        return "redirect:student/lessons/" + lessonId;
    }

    @GetMapping("/dashboard")
    public String viewDashboard(Model model,
                                @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User userOpt = userService.findByEmail(email);

        model.addAttribute("currentUser", userOpt);

        // Lấy dashboard stats
        DashboardStatsDTO stats = courseService.getDashboardStats(userOpt.getId());
        model.addAttribute("stats", stats);

        return "views/student/dashboard";
    }
}