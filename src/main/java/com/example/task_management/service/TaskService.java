package com.example.task_management.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.task_management.model.Task;
import com.example.task_management.repository.TaskRepository;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public List<Task> getTasksExcludingDone() {
        return taskRepository.findByStatusNot("Completed");
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElseThrow();
    }

    public void saveTask(Task task) {
        taskRepository.save(task);
    }

    public void deleteTaskById(Long id) {
        taskRepository.deleteById(id);
    }

    public void updateTaskStatus(Long taskId, String newStatus) {

        taskRepository.findById(taskId).ifPresent(task -> {

            task.setStatus(newStatus);
            taskRepository.save(task);

        });
    }

    public void updateTask(Task updatedTask) {
        taskRepository.save(updatedTask);
    }
}
