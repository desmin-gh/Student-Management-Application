package com.example.student.service;

import com.example.student.model.Student;
import com.example.student.repository.StudentRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Service
public class StudentService {
    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);
    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Cacheable(value = "students", unless = "#result == null")
    public List<Student> getAllStudents() {
        try {
            return studentRepository.findAll();
        } catch (RedisConnectionFailureException e) {
            logger.warn("Redis connection failed. Falling back to database: {}", e.getMessage());
            return studentRepository.findAll();
        }
    }

    @Cacheable(value = "students", key = "#name", unless = "#result == null")
    public List<Student> searchStudentsByName(String name) {
        try {
            return studentRepository.findByNameContainingIgnoreCase(name);
        } catch (RedisConnectionFailureException e) {
            logger.warn("Redis connection failed. Falling back to database: {}", e.getMessage());
            return studentRepository.findByNameContainingIgnoreCase(name);
        }
    }

    @CacheEvict(value = "students", allEntries = true)
    public Student createStudent(Student student) {
        try {
            return studentRepository.save(student);
        } catch (RedisConnectionFailureException e) {
            logger.warn("Redis connection failed. Continuing with database operation: {}", e.getMessage());
            return studentRepository.save(student);
        }
    }

    @Transactional
    @CacheEvict(value = "students", allEntries = true)
    public Student updateStudent(Long id, Student studentDetails) {
        try {
            Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + id));

            if (studentDetails.getName() != null) {
                existingStudent.setName(studentDetails.getName());
            }
            if (studentDetails.getAge() != null) {
                existingStudent.setAge(studentDetails.getAge());
            }
            if (studentDetails.getClassName() != null) {
                existingStudent.setClassName(studentDetails.getClassName());
            }
            if (studentDetails.getPhoneNumber() != null) {
                existingStudent.setPhoneNumber(studentDetails.getPhoneNumber());
            }

            return studentRepository.save(existingStudent);
        } catch (RedisConnectionFailureException e) {
            logger.warn("Redis connection failed. Continuing with database operation: {}", e.getMessage());
            return studentRepository.save(studentDetails);
        }
    }

    @CacheEvict(value = "students", allEntries = true)
    public void deleteStudent(Long id) {
        try {
            if (!studentRepository.existsById(id)) {
                throw new EntityNotFoundException("Student not found with id: " + id);
            }
            studentRepository.deleteById(id);
        } catch (RedisConnectionFailureException e) {
            logger.warn("Redis connection failed. Continuing with database operation: {}", e.getMessage());
            studentRepository.deleteById(id);
        }
    }
}