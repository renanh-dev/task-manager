package com.app.taskmanager.controller;

import com.app.taskmanager.AuthTestSupport;
import com.app.taskmanager.BaseIntegrationTest;
import com.app.taskmanager.dto.request.TaskRequest;
import com.app.taskmanager.dto.request.TaskUpdateRequest;
import com.app.taskmanager.dto.response.AuthResponse;
import com.app.taskmanager.dto.response.TaskResponse;
import com.app.taskmanager.enums.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskIntegrationTest extends BaseIntegrationTest {

    private AuthTestSupport auth;
    private AuthResponse owner;
    private AuthResponse other;

    @BeforeEach
    void setUp() throws Exception {
        auth = new AuthTestSupport(mockMvc, objectMapper);
        owner = auth.register("taskowner");
        other = auth.register("taskother");
    }

    // - Create -

    @Test
    void createTask_withValidRequest_returns201AndTask() throws Exception {
        TaskRequest req = new TaskRequest("Buy groceries", "Milk, eggs, bread");

        String body = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", auth.bearer(owner.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Buy groceries"))
                .andExpect(jsonPath("$.description").value("Milk, eggs, bread"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        assertThat(objectMapper.readValue(body, TaskResponse.class).id()).isPositive();
    }

    @Test
    void createTask_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskRequest("Unauthorised task", null))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createTask_withTitleTooShort_returns400() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", auth.bearer(owner.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskRequest("ab", null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_withBlankTitle_returns400() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", auth.bearer(owner.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskRequest("   ", null))))
                .andExpect(status().isBadRequest());
    }

    // - Get -

    @Test
    void getTasks_returnsOnlyOwnTasks() throws Exception {
        createTaskFor(owner, "Owner task 1");
        createTaskFor(owner, "Owner task 2");
        createTaskFor(other, "Other user task");

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", auth.bearer(owner.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Owner task 1"))
                .andExpect(jsonPath("$.content[1].title").value("Owner task 2"));
    }

    @Test
    void getTasks_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTasks_withPagination_respectsPageSize() throws Exception {
        for (int i = 1; i <= 5; i++) {
            createTaskFor(owner, "Task " + i);
        }

        mockMvc.perform(get("/api/tasks")
                        .param("size", "2")
                        .param("page", "0")
                        .header("Authorization", auth.bearer(owner.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    // - Get (Single) -

    @Test
    void getTask_ownTask_returnsTask() throws Exception {
        long taskId = createTaskFor(owner, "My task");

        mockMvc.perform(get("/api/tasks/{id}", taskId)
                        .header("Authorization", auth.bearer(owner.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("My task"));
    }

    @Test
    void getTask_anotherUsersTask_returns403() throws Exception {
        long taskId = createTaskFor(other, "Other's private task");

        mockMvc.perform(get("/api/tasks/{id}", taskId)
                        .header("Authorization", auth.bearer(owner.accessToken())))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTask_nonExistentTask_returns404() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", 999_999L)
                        .header("Authorization", auth.bearer(owner.accessToken())))
                .andExpect(status().isNotFound());
    }

    // - Update -

    @Test
    void updateTask_withValidRequest_updatesAndReturnsTask() throws Exception {
        long taskId = createTaskFor(owner, "Old title");
        TaskUpdateRequest req = new TaskUpdateRequest("New title", "New description", TaskStatus.IN_PROGRESS);

        mockMvc.perform(patch("/api/tasks/{id}", taskId)
                        .header("Authorization", auth.bearer(owner.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.description").value("New description"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void updateTask_partialUpdate_onlyChangesProvidedFields() throws Exception {
        long taskId = createTaskFor(owner, "Original title");

        mockMvc.perform(patch("/api/tasks/{id}", taskId)
                        .header("Authorization", auth.bearer(owner.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskUpdateRequest(null, null, TaskStatus.DONE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Original title"))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void updateTask_anotherUsersTask_returns403() throws Exception {
        long taskId = createTaskFor(other, "Other's task");

        mockMvc.perform(patch("/api/tasks/{id}", taskId)
                        .header("Authorization", auth.bearer(owner.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskUpdateRequest("Hijacked", null, null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateTask_withNoFields_returns400() throws Exception {
        long taskId = createTaskFor(owner, "Some task");

        mockMvc.perform(patch("/api/tasks/{id}", taskId)
                        .header("Authorization", auth.bearer(owner.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // - Delete -

    @Test
    void deleteTask_ownTask_returns204AndTaskIsGone() throws Exception {
        long taskId = createTaskFor(owner, "Task to delete");

        mockMvc.perform(delete("/api/tasks/{id}", taskId)
                        .header("Authorization", auth.bearer(owner.accessToken())))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/{id}", taskId)
                        .header("Authorization", auth.bearer(owner.accessToken())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTask_anotherUsersTask_returns403() throws Exception {
        long taskId = createTaskFor(other, "Other's task to protect");

        mockMvc.perform(delete("/api/tasks/{id}", taskId)
                        .header("Authorization", auth.bearer(owner.accessToken())))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/tasks/{id}", taskId)
                        .header("Authorization", auth.bearer(other.accessToken())))
                .andExpect(status().isOk());
    }

    @Test
    void deleteTask_softDeletedTaskDoesNotAppearInList() throws Exception {
        createTaskFor(owner, "Visible task");
        long deletedId = createTaskFor(owner, "Deleted task");

        mockMvc.perform(delete("/api/tasks/{id}", deletedId)
                        .header("Authorization", auth.bearer(owner.accessToken())))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", auth.bearer(owner.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Visible task"));
    }

    // - Helper -

    private long createTaskFor(AuthResponse user, String title) throws Exception {
        String body = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", auth.bearer(user.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskRequest(title, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(body, TaskResponse.class).id();
    }
}