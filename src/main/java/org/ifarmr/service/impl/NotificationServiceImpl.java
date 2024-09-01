package org.ifarmr.service.impl;

import lombok.RequiredArgsConstructor;
import org.ifarmr.entity.*;
import org.ifarmr.enums.NotificationType;
import org.ifarmr.exceptions.NotFoundException;
import org.ifarmr.payload.request.RecentActivityDto;
import org.ifarmr.repository.*;
import org.ifarmr.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final TaskRepository taskRepository;
    private final CropRepository cropRepository;
    private final InventoryRepository inventoryRepository;
    private final LiveStockRepository liveStockRepository;
    private final TicketRepository ticketRepository;

    @Override
    public void createNotification(Long userId, String content, NotificationType type) {
        User user = findUserById(userId);

        Notification notification = Notification.builder()
                .content(content)
                .type(type)
                .user(user)
                .build();

        notificationRepository.save(notification);
    }

    @Override
    public List<RecentActivityDto> getRecentActivities(String username) {
        User user = findUserByUsername(username);
        List<RecentActivityDto> activities = new ArrayList<>();

        // Fetch recent activities from various tables
        activities.addAll(getPostActivities(user));
        activities.addAll(getCommentActivities(user));
        activities.addAll(getLikeActivities(user));
        activities.addAll(getTaskActivities(user));
        activities.addAll(getCropActivities(user));
        activities.addAll(getInventoryActivities(user));
        activities.addAll(getLiveStockActivities(user));
        activities.addAll(getTicketActivities(user));

        // Sort activities by date
        activities.sort((a, b) -> b.getDate().compareTo(a.getDate()));

        return activities;
    }

    @Override
    public List<Notification> getAllNotifications(String username) {
        User user = findUserByUsername(username);
        return notificationRepository.findByUserIdOrderByDateCreatedDesc(user.getId());
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private List<RecentActivityDto> getPostActivities(User user) {
        List<RecentActivityDto> activities = new ArrayList<>();
        List<Post> posts = postRepository.findByUserIdOrderByDateCreatedDesc(user.getId());

        for (Post post : posts) {
            activities.add(RecentActivityDto.builder()
                    .icon("post-icon") // Replace with appropriate icon
                    .title("New Post")
                    .description(post.getTitle())
                    .timeAgo(calculateTimeAgo(post.getDateCreated()))
                    .date(post.getDateCreated())
                    .build());
        }

        return activities;
    }

    private List<RecentActivityDto> getCommentActivities(User user) {
        List<RecentActivityDto> activities = new ArrayList<>();
        List<Comment> comments = commentRepository.findByUserIdOrderByDateCreatedDesc(user.getId());

        for (Comment comment : comments) {
            activities.add(RecentActivityDto.builder()
                    .icon("comment-icon") // Replace with appropriate icon
                    .title("New Comment")
                    .description(comment.getContent())
                    .timeAgo(calculateTimeAgo(comment.getDateCreated()))
                    .date(comment.getDateCreated())
                    .build());
        }

        return activities;
    }

    private List<RecentActivityDto> getLikeActivities(User user) {
        List<RecentActivityDto> activities = new ArrayList<>();
        List<Like> likes = likeRepository.findByUserIdOrderByDateCreatedDesc(user.getId());

        for (Like like : likes) {
            activities.add(RecentActivityDto.builder()
                    .icon("like-icon") // Replace with appropriate icon
                    .title("New Like")
                    .description("Liked a post/comment")
                    .timeAgo(calculateTimeAgo(like.getDateCreated()))
                    .date(like.getDateCreated())
                    .build());
        }

        return activities;
    }

    private List<RecentActivityDto> getTaskActivities(User user) {
        List<RecentActivityDto> activities = new ArrayList<>();
        List<Task> tasks = taskRepository.findByUserIdOrderByDueDateDesc(user.getId());

        for (Task task : tasks) {
            activities.add(RecentActivityDto.builder()
                    .icon("task-icon") // Replace with appropriate icon
                    .title("New Task")
                    .description(task.getTitle())
                    .timeAgo(calculateTimeAgo(task.getDueDate().atStartOfDay()))
                    .date(task.getDueDate().atStartOfDay())
                    .build());
        }

        return activities;
    }

    private List<RecentActivityDto> getCropActivities(User user) {
        List<RecentActivityDto> activities = new ArrayList<>();
        List<Crop> crops = cropRepository.findByUserIdOrderBySowDateDesc(user.getId());

        for (Crop crop : crops) {
            activities.add(RecentActivityDto.builder()
                    .icon("crop-icon") // Replace with appropriate icon
                    .title("New Crop")
                    .description(crop.getCropName())
                    .timeAgo(calculateTimeAgo(crop.getSowDate()))
                    .date(crop.getSowDate())
                    .build());
        }

        return activities;
    }

    private List<RecentActivityDto> getInventoryActivities(User user) {
        List<RecentActivityDto> activities = new ArrayList<>();
        List<Inventory> inventories = inventoryRepository.findByUserIdOrderByDateAcquiredDesc(user.getId());

        for (Inventory inventory : inventories) {
            activities.add(RecentActivityDto.builder()
                    .icon("inventory-icon") // Replace with appropriate icon
                    .title("New Inventory")
                    .description(inventory.getName())
                    .timeAgo(calculateTimeAgo(inventory.getDateAcquired().atStartOfDay()))
                    .date(inventory.getDateAcquired().atStartOfDay())
                    .build());
        }

        return activities;
    }

    private List<RecentActivityDto> getLiveStockActivities(User user) {
        List<RecentActivityDto> activities = new ArrayList<>();
        List<LiveStock> liveStocks = liveStockRepository.findByUserIdOrderByDateCreatedDesc(user.getId());

        for (LiveStock liveStock : liveStocks) {
            activities.add(RecentActivityDto.builder()
                    .icon("livestock-icon") // Replace with appropriate icon
                    .title("New Livestock")
                    .description(liveStock.getAnimalName())
                    .timeAgo(calculateTimeAgo(liveStock.getDateCreated()))
                    .date(liveStock.getDateCreated())
                    .build());
        }

        return activities;
    }

    private List<RecentActivityDto> getTicketActivities(User user) {
        List<RecentActivityDto> activities = new ArrayList<>();
        List<Ticket> tickets = ticketRepository.findByUserIdOrderByDateCreatedDesc(user.getId());

        for (Ticket ticket : tickets) {
            activities.add(RecentActivityDto.builder()
                    .icon("ticket-icon") // Replace with appropriate icon
                    .title("New Ticket")
                    .description(ticket.getTitle())
                    .timeAgo(calculateTimeAgo(ticket.getDateCreated()))
                    .date(ticket.getDateCreated())
                    .build());
        }

        return activities;
    }

    private LocalDateTime calculateTimeAgo(LocalDateTime dateTime) {
        // Implement your timeAgo calculation logic here
        return dateTime; // Placeholder
    }
}
