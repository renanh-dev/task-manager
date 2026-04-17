package com.example.taskmanager;

import com.example.taskmanager.dto.request.TaskRequest;
import com.example.taskmanager.dto.response.TaskResponse;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.enums.Role;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.exception.UnauthorizedException;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.security.AuthUtils;
import com.example.taskmanager.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private AuthUtils authUtils;

    @InjectMocks
    private TaskService taskService;

    private User owner;
    private User otherUser;
    private Task task;

    @BeforeEach
    void setUp() {
        owner = buildUser(1L, "john@email.com", "john");
        otherUser = buildUser(2L, "audrey@email.com", "audrey");

        task = new Task("Write tests", "learn it", owner);
        setField(task, "id", 10L);
    }

    // - getTask -

    @Test
    void getTask_returnsTask_whenOwnerMatches() {
        // arrange - what the mock returns when each method is called
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(authUtils.getCurrentUser()).thenReturn(owner);

        // act - call the real method
        TaskResponse response = taskService.getTask(10L);

        // assert - verify result
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("Write tests");
        assertThat(response.completed()).isFalse();
    }

    @Test
    void getTask_throwResourceNotFound_whenTaskDoesNotExist() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTask(99L))
                .isInstanceOf(ResourceNotFoundException.class) // correct way of asserting exceptions happen
                .hasMessage("Task not found.");
    }

    @Test
    void getTask_throwsUnauthorized_whenDifferentUserOwnsTask() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task)); // task is owned by john
        when(authUtils.getCurrentUser()).thenReturn(otherUser);           // current user logged in is audrey

        assertThatThrownBy(() -> taskService.getTask(10L))
                .isInstanceOf(UnauthorizedException.class);
    }

    // - CreateTask -

    @Test
    void createTask_savesTaskAndReturnsResponse() {
        TaskRequest request = new TaskRequest("something", "nothing");
        when(authUtils.getCurrentUser()).thenReturn(owner);

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            setField(saved, "id", 20L);
            return saved;
        });

        TaskResponse response = taskService.createTask(request);

        assertThat(response.title()).isEqualTo("something");
        assertThat(response.description()).isEqualTo("nothing");
        assertThat(response.completed()).isFalse();

        verify(taskRepository, times(1)).save(any(Task.class));
        // verifies save() was called exactly once.
    }

    // - deleteTask -

    @Test
    void deleteTask_deletesSuccessfully_whenUserMatches() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(authUtils.getCurrentUser()).thenReturn(owner);

        taskService.deleteTask(10L);

        verify(taskRepository).deleteById(10L);
    }

    @Test
    void deleteTask_throwsUnauthorized_andNeverDeletes_whenWrongUser() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(authUtils.getCurrentUser()).thenReturn(otherUser);

        taskService.deleteTask(10L);

        assertThatThrownBy(() -> taskService.deleteTask(10L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void deleteTask_throwsNotFound_whenTaskDoesNotExist() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        taskService.deleteTask(99L);

        assertThatThrownBy(() -> taskService.deleteTask(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // - updateCompletionStatus -

    @Test
    void updateCompletionStatus_marksTaskComplete() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(authUtils.getCurrentUser()).thenReturn(owner);
        when(taskRepository.save(task)).thenReturn(task);

        TaskResponse response = taskService.updateCompletionStatus(10L, true);

        assertThat(response.completed()).isTrue();
        verify(taskRepository).save(task);
    }

    @Test
    void updateCompletionStatus_throwsUnauthorized_whenWrongUser() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(authUtils.getCurrentUser()).thenReturn(otherUser);

        assertThatThrownBy(() -> taskService.updateCompletionStatus(10L, true))
                .isInstanceOf(UnauthorizedException.class);

        verify(taskRepository, never()).save(any());
    }

    // - Helpers -

    private User buildUser(Long id, String email, String username) {
        User u = User.builder()
                .username(username)
                .email(email)
                .password("password")
                .role(Role.USER)
                .build();

        setField(u, "id", id);
        return u;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Could not set field " + fieldName, e);
        }
    } // try catch is fine for helpers
}
