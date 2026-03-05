package com.vortexbird.ordenesPago.service;

import com.vortexbird.ordenesPago.entity.Order;

public interface ExternalNotificationService {
    void notifyOrderApproved(Order order);
}
