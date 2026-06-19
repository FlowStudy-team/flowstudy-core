package com.flowstudy.core.common.result;

import java.util.List;

public record PageResponse<T>(List<T> records, long total, int page, int size) {
}
