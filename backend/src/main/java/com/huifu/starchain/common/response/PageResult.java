package com.huifu.starchain.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> records;
    private long total;
    private int page;
    private int size;
    private int totalPages;

    public static <T> PageResult<T> of(List<T> records, long total, int page, int size) {
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        return PageResult.<T>builder()
                .records(records)
                .total(total)
                .page(page)
                .size(size)
                .totalPages(totalPages)
                .build();
    }

    public <U> PageResult<U> map(Function<? super T, ? extends U> mapper) {
        List<U> mapped = records.stream().map(mapper).collect(Collectors.toList());
        return PageResult.<U>builder()
                .records(mapped)
                .total(total)
                .page(page)
                .size(size)
                .totalPages(totalPages)
                .build();
    }
}
