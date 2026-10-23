package br.com.cursohub.student.application.model;

import java.util.List;

public record StudentDashboard(int enrolledCourses, int inProgressCourses,
                               int completedCourses, List<StudentCourseOverview> courses) {
    public StudentDashboard {
        courses = List.copyOf(courses);
    }
}
