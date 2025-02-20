package com.example.student.service;

import com.example.student.model.Student;
import com.example.student.repository.StudentRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class StudentService {
    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // Helper method to update student properties
    private void updateStudentProperties(Student existingStudent, Student studentDetails) {
        String newName = studentDetails.getName();
        Integer newAge = studentDetails.getAge();
        String newClassName = studentDetails.getClassName();
        String newPhoneNumber = studentDetails.getPhoneNumber();

        if (newName != null && !newName.trim().isEmpty()) {
            existingStudent.setName(newName);
        }
        if (newAge != null) {
            existingStudent.setAge(newAge);
        }
        if (newClassName != null && !newClassName.trim().isEmpty()) {
            existingStudent.setClassName(newClassName);
        }
        if (newPhoneNumber != null && !newPhoneNumber.trim().isEmpty()) {
            existingStudent.setPhoneNumber(newPhoneNumber);
        }
    }

    @Transactional
    @CacheEvict(value = "students", allEntries = true)
    public Student updateStudent(Long id, Student studentDetails) {
        Student existingStudent = studentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + id));

        // Use helper method to update properties
        updateStudentProperties(existingStudent, studentDetails);

        return studentRepository.save(existingStudent);
    }

    @CacheEvict(value = "students", allEntries = true)
    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    @Cacheable(value = "students")
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Cacheable(value = "students", key = "#name")
    public List<Student> searchStudentsByName(String name) {
        return studentRepository.findByNameContainingIgnoreCase(name);
    }

    @CacheEvict(value = "students", allEntries = true)
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new EntityNotFoundException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
    }
}