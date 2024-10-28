package com.activecourses.upwork.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.activecourses.upwork.dto.JobDTO;
import com.activecourses.upwork.mapper.JobMapper;
import com.activecourses.upwork.model.Job;
import com.activecourses.upwork.model.JobStatus;
import com.activecourses.upwork.model.JobType;
import com.activecourses.upwork.model.User;
import com.activecourses.upwork.repository.job.JobRepository;
import com.activecourses.upwork.repository.user.UserRepository;
import com.activecourses.upwork.service.authentication.AuthService;
import com.activecourses.upwork.service.job.JobServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private JobServiceImpl jobService;

    private JobDTO jobDTO;
    private User client;
    private Job job;

    @BeforeEach
    void setUp() {
        // Initialize test data
        jobDTO = new JobDTO();
        jobDTO.setTitle("Java Developer");
        jobDTO.setDescription("We are looking for a skilled Java developer to join our team.");
        jobDTO.setBudget(BigDecimal.valueOf(1000.00));
        jobDTO.setJobType(JobType.Hourly);
        jobDTO.setStatus(JobStatus.Open);

        client = new User();
        client.setId(1); // Set a mock user ID

        job = new Job();
        job.setTitle(jobDTO.getTitle());
        job.setDescription(jobDTO.getDescription());
        job.setBudget(jobDTO.getBudget());
        job.setJobType(JobType.Hourly); // Assuming JobType is an enum
        job.setStatus(JobStatus.Open); // Assuming JobStatus is an enum
    }

    @Test
    void createJob_Success() {
        // Arrange
        when(authService.getCurrentUserId()).thenReturn(client.getId());
        when(userRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(jobMapper.mapFrom(jobDTO)).thenReturn(job);
        when(jobRepository.save(any(Job.class))).thenReturn(job);

        // Act
        Job createdJob = jobService.createJob(jobDTO);

        // Assert
        assertNotNull(createdJob);
        assertEquals(jobDTO.getTitle(), createdJob.getTitle());
        assertEquals(jobDTO.getDescription(), createdJob.getDescription());
        assertEquals(jobDTO.getBudget(), createdJob.getBudget());
        assertEquals(jobDTO.getJobType(), createdJob.getJobType());
        assertEquals(jobDTO.getStatus(), createdJob.getStatus());
        verify(jobRepository).save(any(Job.class)); // Verify save was called
    }

    @Test
    void createJob_UserNotAuthenticated() {
        // Arrange
        when(authService.getCurrentUserId()).thenReturn(null);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            jobService.createJob(jobDTO);
        });
        assertEquals("User is not authenticated", exception.getMessage());
    }

    @Test
    void createJob_UserNotFound() {
        // Arrange
        when(authService.getCurrentUserId()).thenReturn(client.getId());
        when(userRepository.findById(client.getId())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            jobService.createJob(jobDTO);
        });
        assertEquals("User not found with ID: " + client.getId(), exception.getMessage());
    }
}