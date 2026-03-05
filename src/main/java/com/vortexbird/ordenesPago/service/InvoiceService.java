package com.vortexbird.ordenesPago.service;

import com.vortexbird.ordenesPago.dto.response.InvoiceResponse;
import org.springframework.web.multipart.MultipartFile;

public interface InvoiceService {
    InvoiceResponse uploadInvoice(Long orderId, MultipartFile file, String userEmail);
    InvoiceResponse getInvoiceByOrderId(Long orderId, String userEmail);
}
