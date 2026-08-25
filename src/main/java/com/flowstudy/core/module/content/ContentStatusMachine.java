package com.flowstudy.core.module.content;

import com.flowstudy.core.common.exception.BusinessException;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;

public final class ContentStatusMachine {
    private static final Set<String> STATES = Set.of("DRAFT", "PUBLISHED", "DELETED");

    private ContentStatusMachine() {
    }

    public static String normalize(String status, String defaultStatus) {
        String value = status == null || status.isBlank() ? defaultStatus : status.trim();
        String normalized = value.toUpperCase(Locale.ROOT);
        if (!STATES.contains(normalized)) {
            throw new BusinessException(40012, "unsupported content status", HttpStatus.BAD_REQUEST);
        }
        return normalized;
    }

    public static String transition(String current, String requested) {
        String from = normalize(current, "DRAFT");
        String to = normalize(requested, from);
        if ("DELETED".equals(from) && !"DELETED".equals(to)) {
            throw new BusinessException(40013, "deleted content cannot be restored directly", HttpStatus.CONFLICT);
        }
        return to;
    }
}
