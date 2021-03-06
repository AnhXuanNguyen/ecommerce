package com.example.ecommerce.service.notification;

import com.example.ecommerce.model.notification.Notification;
import com.example.ecommerce.model.user.User;
import com.example.ecommerce.repository.INotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class NotificationService implements INotificationService{
    @Autowired
    private INotificationRepository notificationRepository;
    @Override
    public Iterable<Notification> findAll() {
        return notificationRepository.findAll();
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return notificationRepository.findById(id);
    }

    @Override
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public void deleteById(Long id) {
        notificationRepository.deleteById(id);
    }

    @Override
    public Iterable<Notification> findAllByUser(User user) {
        return notificationRepository.findAllByUser(user);
    }
}
