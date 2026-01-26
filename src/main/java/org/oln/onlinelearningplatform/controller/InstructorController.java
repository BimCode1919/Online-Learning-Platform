package org.oln.onlinelearningplatform.controller;

import org.oln.onlinelearningplatform.entity.Course;
import org.oln.onlinelearningplatform.entity.Lesson;
import org.oln.onlinelearningplatform.service.course.CourseService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/instructor")
public class InstructorController {

    private final CourseService courseService;

    public InstructorController(CourseService courseService) {
        this.courseService = courseService;
    }

    // 1. Dashboard: Hiển thị danh sách khóa học của Giảng viên
    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        model.addAttribute("courses", courseService.getCoursesByInstructorEmail(email));

        return "views/teacher/dashboard";
    }

    // 2. Hiển thị Form tạo khóa học mới
    @GetMapping("/create-course")
    public String showCreateCourseForm(Model model) {
        model.addAttribute("course", new Course());

        return "views/teacher/course-editor";
    }

    // 3. Xử lý lưu khóa học (Khi bấm nút Save)
    @PostMapping("/save-course")
    public String saveCourse(@ModelAttribute Course course,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            if (course.getId() != null) {
                // --- TRƯỜNG HỢP 1: CẬP NHẬT (UPDATE) ---
                courseService.updateCourse(course.getId(), course.getTitle(), course.getDescription());
                redirectAttributes.addFlashAttribute("success", "Cập nhật khóa học thành công!");
            } else {
                courseService.createCourse(course.getTitle(), course.getDescription(), userDetails.getUsername());
                redirectAttributes.addFlashAttribute("success", "Tạo khóa học mới thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/instructor/dashboard";
    }

    // 4. Xem chi tiết khóa học để thêm bài (Hoặc sửa khóa học)
    @GetMapping("/course/{id}")
    public String manageCourse(@PathVariable Long id, Model model) {
        Optional<Course> courseOpt = courseService.getCourseById(id);

        if (courseOpt.isEmpty()) {
            return "redirect:/instructor/dashboard";
        }

        model.addAttribute("course", courseOpt.get());

        //Trỏ đúng vào thư mục 'teacher'
        return "views/teacher/course-editor";
    }

    // 5. QUAN TRỌNG: Xử lý thêm bài học (Input cho AI)
    @PostMapping("/course/{courseId}/add-lesson")
    public String addLesson(@PathVariable Long courseId,
                            @RequestParam String title,
                            @RequestParam String content, // Nội dung text dài
                            @RequestParam(required = false) Integer orderIndex,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        try {
            courseService.addLesson(courseId, title, content, orderIndex, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("success", "Thêm bài học thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi thêm bài: " + e.getMessage());
        }

        return "redirect:/instructor/course/" + courseId;
    }

    @GetMapping("/lesson/delete/{id}")
    public String deleteLesson(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            // Lấy ID khóa học trước khi xóa để redirect về đúng chỗ
            Optional<Lesson> lesson = courseService.getLessonById(id);
            Long courseId = lesson.map(l -> l.getCourse().getId()).orElse(null);

            courseService.deleteLesson(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("success", "Xóa bài học thành công!");

            return "redirect:/instructor/course/" + courseId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi xóa: " + e.getMessage());
            return "redirect:/instructor/dashboard";
        }
    }

    // --- 7. HIỂN THỊ FORM SỬA BÀI HỌC (Tạo trang riêng cho đỡ rối) ---
    @GetMapping("/lesson/edit/{id}")
    public String showEditLessonForm(@PathVariable Long id, Model model) {
        Optional<Lesson> lessonOpt = courseService.getLessonById(id);
        if (lessonOpt.isEmpty()) {
            return "redirect:/instructor/dashboard";
        }
        model.addAttribute("lesson", lessonOpt.get());
        return "views/teacher/lesson-editor"; // File HTML mới sẽ tạo ở bước 4
    }

    // --- 8. XỬ LÝ LƯU SỬA BÀI HỌC ---
    @PostMapping("/lesson/update")
    public String updateLesson(@ModelAttribute Lesson lesson,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            courseService.updateLesson(lesson.getId(), lesson.getTitle(), lesson.getContent(), lesson.getOrderIndex(), userDetails.getUsername());
            redirectAttributes.addFlashAttribute("success", "Cập nhật bài học thành công!");

            // Lấy lại courseID để redirect
            Optional<Lesson> updatedLesson = courseService.getLessonById(lesson.getId());
            return "redirect:/instructor/course/" + updatedLesson.get().getCourse().getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi cập nhật: " + e.getMessage());
            return "redirect:/instructor/dashboard";
        }
    }
}