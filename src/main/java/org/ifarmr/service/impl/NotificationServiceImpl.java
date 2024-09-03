package org.ifarmr.service.impl;

import lombok.RequiredArgsConstructor;
import org.ifarmr.entity.*;
import org.ifarmr.enums.NotificationType;
import org.ifarmr.exceptions.NotFoundException;
import org.ifarmr.payload.request.NotificationRequest;
import org.ifarmr.payload.request.RecentActivityDto;
import org.ifarmr.repository.*;
import org.ifarmr.service.NotificationService;
import org.ifarmr.service.NotificationTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private NotificationTokenRepository tokenRepository;
    @Autowired
    private FCMService fcmService;
    @Autowired
    NotificationTokenService notificationTokenService;

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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

        for (Post post : posts) {
            activities.add(RecentActivityDto.builder()
                    .icon("post-icon") // Replace with appropriate icon
                    .title("New Post")
                    .description(post.getTitle())
                    .timeAgo(calculateTimeAgo(post.getDateCreated()))
                    .date(post.getDateCreated().format(formatter))
                    .build());
        }

        return activities;
    }

    private List<RecentActivityDto> getCommentActivities(User user) {
        List<RecentActivityDto> activities = new ArrayList<>();
        List<Comment> comments = commentRepository.findCommentsOnUserPosts(user.getId());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

        for (Comment comment : comments) {
            activities.add(RecentActivityDto.builder()
                    .icon("comment-icon") // Replace with appropriate icon
                    .title("New Comment on Your Post")
                    .description(comment.getUser().getUsername() + " commented: \"" + comment.getContent() + "\"")
                    .timeAgo(calculateTimeAgo(comment.getDateCreated()))
                    .date(comment.getDateCreated().format(formatter))
                    .build());
        }

        return activities;
    }

    private List<RecentActivityDto> getLikeActivities(User user) {
        List<RecentActivityDto> activities = new ArrayList<>();
        List<Like> likes = likeRepository.findLikesOnUserContent(user.getId());

        if (likes.isEmpty()) {
            logger.warn("No likes found for user: {}", user.getUsername());
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

        for (Like like : likes) {
            String title;
            String description;

            if (like.getPost() != null) {
                title = "Your Post Received a Like";
                description = like.getUser().getUsername() + " liked your post: \"" + like.getPost().getTitle() + "\"";
            } else if (like.getComment() != null) {
                title = "Your Comment Received a Like";
                description = like.getUser().getUsername() + " liked your comment: \"" + like.getComment().getContent() + "\"";
            } else {
                continue; // Skip if neither post nor comment is associated
            }

            activities.add(RecentActivityDto.builder()
                    .icon("like-icon") // Replace with appropriate icon
                    .title(title)
                    .description(description)
                    .timeAgo(calculateTimeAgo(like.getDateCreated()))
                    .date(like.getDateCreated().format(formatter))
                    .build());
        }

        return activities;
    }

    private List<RecentActivityDto> getTaskActivities(User user) {
        List<RecentActivityDto> activities = new ArrayList<>();
        List<Task> tasks = taskRepository.findByUserIdOrderByDueDateDesc(user.getId());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

        for (Task task : tasks) {
            activities.add(RecentActivityDto.builder()
                    .icon("task-icon") // Replace with appropriate icon
                    .title("New Task")
                    .description(task.getTitle())
                    .timeAgo(calculateTimeAgo(task.getDueDate().atStartOfDay()))
                    .date(task.getDueDate().atStartOfDay().format(formatter))
                    .build());
        }

        return activities;
    }

    private List<RecentActivityDto> getCropActivities(User user) {
        List<RecentActivityDto> activities = new ArrayList<>();
        List<Crop> crops = cropRepository.findByUserIdOrderBySowDateDesc(user.getId());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

        for (Crop crop : crops) {
            activities.add(RecentActivityDto.builder()
                    .icon("crop-icon") // Replace with appropriate icon
                    .title("New Crop")
                    .description(crop.getCropName())
                    .timeAgo(calculateTimeAgo(crop.getSowDate()))
                    .date(crop.getSowDate().format(formatter))
                    .build());
        }

        return activities;
    }

    private List<RecentActivityDto> getInventoryActivities(User user) {
        List<RecentActivityDto> activities = new ArrayList<>();
        List<Inventory> inventories = inventoryRepository.findByUserIdOrderByDateAcquiredDesc(user.getId());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

        for (Inventory inventory : inventories) {
            activities.add(RecentActivityDto.builder()
                    .icon("inventory-icon") // Replace with appropriate icon
                    .title("New Inventory")
                    .description(inventory.getName())
                    .timeAgo(calculateTimeAgo(inventory.getDateAcquired().atStartOfDay()))
                    .date(inventory.getDateAcquired().atStartOfDay().format(formatter))
                    .build());
        }

        return activities;
    }

    private List<RecentActivityDto> getLiveStockActivities(User user) {
        List<RecentActivityDto> activities = new ArrayList<>();
        List<LiveStock> liveStocks = liveStockRepository.findByUserIdOrderByDateCreatedDesc(user.getId());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

        for (LiveStock liveStock : liveStocks) {
            activities.add(RecentActivityDto.builder()
                    .icon("livestock-icon") // Replace with appropriate icon
                    .title("New Livestock")
                    .description(liveStock.getAnimalName())
                    .timeAgo(calculateTimeAgo(liveStock.getDateCreated()))
                    .date(liveStock.getDateCreated().format(formatter))
                    .build());
        }

        return activities;
    }

    private List<RecentActivityDto> getTicketActivities(User user) {
        List<RecentActivityDto> activities = new ArrayList<>();
        List<Ticket> tickets = ticketRepository.findByUserIdOrderByDateCreatedDesc(user.getId());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

        for (Ticket ticket : tickets) {
            activities.add(RecentActivityDto.builder()
                    .icon("ticket-icon") // Replace with appropriate icon
                    .title("New Ticket")
                    .description(ticket.getTitle())
                    .timeAgo(calculateTimeAgo(ticket.getDateCreated()))
                    .date(ticket.getDateCreated().format(formatter))
                    .build());
        }

        return activities;
    }

    private String calculateTimeAgo(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, now);

        long seconds = duration.getSeconds();
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        Map<String, Long> timeUnits = new HashMap<>();
        timeUnits.put("year", days / 365);
        timeUnits.put("month", (days % 365) / 30);
        timeUnits.put("day", days % 30);
        timeUnits.put("hour", hours % 24);
        timeUnits.put("minute", minutes % 60);
        timeUnits.put("second", seconds % 60);

        if (timeUnits.get("year") > 0) {
            return timeUnits.get("year") + " year" + (timeUnits.get("year") > 1 ? "s" : "") + " ago";
        } else if (timeUnits.get("month") > 0) {
            return timeUnits.get("month") + " month" + (timeUnits.get("month") > 1 ? "s" : "") + " ago";
        } else if (timeUnits.get("day") > 0) {
            return timeUnits.get("day") + " day" + (timeUnits.get("day") > 1 ? "s" : "") + " ago";
        } else if (timeUnits.get("hour") > 0) {
            return timeUnits.get("hour") + " hour" + (timeUnits.get("hour") > 1 ? "s" : "") + " ago";
        } else if (timeUnits.get("minute") > 0) {
            return timeUnits.get("minute") + " minute" + (timeUnits.get("minute") > 1 ? "s" : "") + " ago";
        } else {
            return timeUnits.get("second") + " second" + (timeUnits.get("second") > 1 ? "s" : "") + " ago";
        }
    }



    // FIREBASE NOTIFICATION METHODS BELOW;

    @Override
    public void sendNotificationToUser(String username, NotificationRequest request) throws ExecutionException, InterruptedException {
        User user = findUserByUsername(username);
        List<NotificationToken> tokens = notificationTokenService.getTokensByUserId(user.getId());

        if (tokens.isEmpty()) {
            throw new RuntimeException("No tokens found for user with ID: " + user.getId());
        }

        for (NotificationToken token : tokens) {
            request.setToken(token.getToken());
            fcmService.sendMessageToToken(request);
        }
    }

    @Override
    public void sendNotificationToAll(String username, NotificationRequest request) throws ExecutionException, InterruptedException {
        // No need to validate the username if this is for all users,
        // but you might want to log or audit who initiated the action.
        List<NotificationToken> tokens = tokenRepository.findAll();

        for (NotificationToken token : tokens) {
            request.setToken(token.getToken());
            fcmService.sendMessageToToken(request);
        }
    }

}
