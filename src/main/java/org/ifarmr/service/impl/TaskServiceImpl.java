package org.ifarmr.service.impl;

import org.ifarmr.entity.Task;
import org.ifarmr.entity.User;
import org.ifarmr.exceptions.ConflictException;
import org.ifarmr.exceptions.IFarmServiceException;
import org.ifarmr.exceptions.NotFoundException;
import org.ifarmr.payload.request.NewTaskRequest;
import org.ifarmr.payload.request.NotificationRequest;
import org.ifarmr.payload.response.TaskInfo;
import org.ifarmr.payload.response.TaskResponseDto;
import org.ifarmr.payload.response.UpcomingTaskResponse;
import org.ifarmr.repository.TaskRepository;
import org.ifarmr.repository.UserRepository;
import org.ifarmr.service.NotificationService;
import org.ifarmr.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;


    @Override
    public TaskResponseDto createTask(NewTaskRequest taskRequest, String currentUsername) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("User not found"));


        boolean taskExists = taskRepository.existsByTitleAndDueDateAndUser(taskRequest.getTitle(), taskRequest.getDueDate(), user);
        if (taskExists) {
            throw new ConflictException("Task with the same title and due date already exists.");
        }

        Task task = Task.builder()
                .title(taskRequest.getTitle())
                .location(taskRequest.getLocation())
                .category(taskRequest.getCategory())
                .dueDate(taskRequest.getDueDate())
                .description(taskRequest.getDescription())
                .user(user)
                .build();

        try {
            taskRepository.save(task);

            // SEND NOTIFICATION TO USER
            NotificationRequest notificationRequest = new NotificationRequest();
            notificationRequest.setTitle("New Task Added");
            notificationRequest.setBody("A new task has been added with title: " + task.getTitle());
            notificationRequest.setTopic("Task Notifications");
            notificationService.sendNotificationToUser(user.getId(), notificationRequest);

        } catch (Exception e) {
            throw new IFarmServiceException("An error occurred while saving the task", e);
        }


        return TaskResponseDto.builder()
                .responseMessage("Task successfully created! ")
                .taskInfo(TaskInfo.builder()
                        .title(task.getTitle())
                        .location(task.getLocation())
                        .category(task.getCategory().name())
                        .dueDate(task.getDueDate())
                        .description(task.getDescription())
                        .build())
                .build();

    }

    @Override
    public List<UpcomingTaskResponse> getUpcomingTasks(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("user not found"));

        List<Task> tasks = taskRepository.findTasksByUserOrderByDueDateAsc(user);


        return tasks.stream().map(task ->
                UpcomingTaskResponse.builder()
                        .title(task.getTitle())
                        .location(task.getLocation())
                        .dueDate(task.getDueDate())
                        .description(getDescription(task))
                        .category(task.getCategory())
                        .build()).collect(Collectors.toList()


        );
    }
    private String getDescription(Task task){
        LocalDate now = LocalDate.now();
        LocalDate dueDate = task.getDueDate();
        long daysUntilDue = ChronoUnit.DAYS.between(now, task.getDueDate());
        String whenScheduled;
        String whenDue;

        // Determine the "Scheduled" part
        if (daysUntilDue == 0) {
            whenScheduled = "today";
        } else if (daysUntilDue == 1) {
            whenScheduled = "tomorrow";
        } else if (daysUntilDue < 7) {
            if (dueDate.getDayOfWeek().getValue() > now.getDayOfWeek().getValue()) {
                whenScheduled = "next " + dueDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            } else {
                whenScheduled = "in " + daysUntilDue + " days";
            }
        } else if (dueDate.getMonth() != now.getMonth()) {
            whenScheduled = "next month";
        } else if (daysUntilDue >= 7 && daysUntilDue < 14) {
            whenScheduled = "next " + dueDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        } else {
            whenScheduled = "in " + daysUntilDue + " days";
        }

        // Determine the "Due" part
        if (daysUntilDue == 0) {
            whenDue = "today";
        } else if (daysUntilDue == 1) {
            whenDue = "tomorrow";
        } else {
            whenDue = "in " + daysUntilDue + " days";
        }

        return "Scheduled " + whenScheduled + " in " + task.getLocation() + ". Due " + whenDue + ".";
    }

}