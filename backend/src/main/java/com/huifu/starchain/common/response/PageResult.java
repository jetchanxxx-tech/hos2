package com.huifu.starchain.common.response;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PageResult<T> {

    private List<T> records;
    private long total;
    private int page;
    private int size;
    private int totalPages;

    public PageResult() {}

    public PageResult(List<T> records, long total, int page, int size, int totalPages) {
        this.records = records; this.total = total; this.page = page;
        this.size = size; this.totalPages = totalPages;
    }

    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public static <T> PageResult<T> of(List<T> records, long total, int page, int size) {
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        return new PageResult<>(records, total, page, size, totalPages);
    }

    public <U> PageResult<U> map(Function<? super T, ? extends U> mapper) {
        List<U> mapped = records.stream().map(mapper).collect(Collectors.toList());
        return new PageResult<>(mapped, total, page, size, totalPages);
    }
}
