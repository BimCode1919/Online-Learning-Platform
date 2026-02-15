package org.oln.onlinelearningplatform.controller;

import org.oln.onlinelearningplatform.entity.Course;
import org.oln.onlinelearningplatform.entity.Enrollment;
import org.oln.onlinelearningplatform.entity.User;
import org.oln.onlinelearningplatform.repository.CourseRepository;
import org.oln.onlinelearningplatform.service.course.CourseService;
import org.oln.onlinelearningplatform.service.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/instructor")
public class InstructorController {

    private final CourseService courseService;
    private final CourseRepository courseRepository;
    private final UserService userService;

    public InstructorController(CourseService courseService, CourseRepository courseRepository, UserService userService) {
        this.courseService = courseService;
        this.courseRepository = courseRepository;
        this.userService = userService;
    }

    // 1. Dashboard: Hiển thị danh sách khóa học của Giảng viên
    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User instructor = userService.findByEmail(userDetails.getUsername());
        List<Course> courses = courseService.getCoursesByInstructor(instructor);

        // Tính tổng thu nhập
        double totalRevenue = courses.stream()
                .flatMap(course -> course.getEnrollments().stream())
                .filter((Enrollment enrollment) -> "COMPLETED".equals(enrollment.getPaymentStatus())) // Chỉ định rõ kiểu Enrollment
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

    // 3. Xử lý lưu khóa học (Khi bấm nút Save)
    @PostMapping("/save-course")
    public String saveCourse(@ModelAttribute Course course, // Course ở đây đã có ID từ thẻ input hidden
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            // Truyền cả object course vào service
            Course savedCourse = courseService.saveOrUpdateCourse(course, userDetails.getUsername());

            redirectAttributes.addFlashAttribute("success", "Lưu thông tin thành công!");
            return "redirect:/instructor/course/" + savedCourse.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/instructor/dashboard";
        }
    }

    // Bạn cần có thêm API này để hiển thị lại trang Editor sau khi Redirect
    @GetMapping("/course/{id}")
    public String editCourse(@PathVariable("id") Long id, Model model) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học với ID: " + id));

        model.addAttribute("course", course);
        return "views/teacher/course-editor";
    }

    @PostMapping("/course/{id}/add-lesson")
    public String addLesson(@PathVariable("id") Long courseId,
                            @RequestParam(value = "lessonId", required = false) Long lessonId, // Nhận ID từ input hidden
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
}