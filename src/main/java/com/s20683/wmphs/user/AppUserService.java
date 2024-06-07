package com.s20683.wmphs.user;

import com.s20683.wmphs.destination.Destination;
import com.s20683.wmphs.gui2wmphs.request.AppUserDTO;
import com.s20683.wmphs.tools.QueryTimer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AppUserService {
    protected final Logger logger = LoggerFactory.getLogger(AppUserService.class);
    @Autowired
    private final AppUserRepository appUserRepository;
    private final Map<Integer, AppUser> users = new HashMap<>();
    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }
    @PostConstruct
    public void init(){
        QueryTimer timer = new QueryTimer();
        appUserRepository
                .findAll()
                .forEach(user -> {
                    logger.info("Received from database user {}", user);
                    users.put(user.getId(), user);
                });
        logger.info("Find All operation for Users executed on {}", timer);
    }
    public String addAppUser(AppUserDTO appUserDTO) {
        AppUser user = users.get(appUserDTO.getId());
        if (user == null) {
            QueryTimer timer = new QueryTimer();
            user = appUserRepository.save(
                    new AppUser(
                            appUserDTO.getName()
                    )
            );
            if (user.getId() != null) {
                logger.info("AppUser {} saved to database, executed in {}", user, timer);
                users.put(user.getId(), user);
                return "OK";
            } else {
                logger.warn("Error while saving user {} to database", appUserDTO);
                return "Użytkownik nie istnieje ale powstał problem podczas zapisu do bazy.";
            }
        } else {
            QueryTimer timer = new QueryTimer();
            user.setName(appUserDTO.getName());
            appUserRepository.save(user);
            logger.info("User {} updated on database, executed in {}", user, timer);
            users.put(user.getId(), user);
            return "OK";
        }
    }
    public String removeUser(int id) {
        AppUser userToRemove = users.get(id);
        if (userToRemove == null) {
            logger.info("Cannot remove user with id {}, does not exist", id);
            return "Użytkownik z id " + id + " nie istnieje";
        }
        QueryTimer timer = new QueryTimer();
        try {
            appUserRepository.delete(userToRemove);
            users.remove(userToRemove.getId());
            logger.info("User {} removed from database, executed {}", userToRemove, timer);
            return "OK";
        } catch (Exception exception) {
            logger.warn("Exception while remove user {}", userToRemove, exception);
            return exception.getMessage();
        }
    }
    public List<AppUserDTO> getAppUsers() {
        return users.values().stream().map(AppUser::toDTO).collect(Collectors.toList());
    }
    public AppUser getAppUser(int appUserId){
        return users.get(appUserId);
    }
}
