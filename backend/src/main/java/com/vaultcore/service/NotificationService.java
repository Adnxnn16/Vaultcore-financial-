package com.vaultcore.service;

import java.util.UUID;

public interface NotificationService {

    void sendChallenge(UUID userId, String otp);

}
