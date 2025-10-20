package com.pixeldoctrine.smhi_assignment.dto;

public class PaginationDTO {

    private static int DEFAULT_PAGE_SIZE = 25;

    private int page;
    private int pageSize;

    public PaginationDTO(Integer page, Integer pageSize) {
        this.page = page != null? page : 0;
        this.pageSize = pageSize != null? pageSize : DEFAULT_PAGE_SIZE;
    }

    public int getPage() {
        return page;
    }

    public int getOffset() {
        return page * pageSize;
    }

    public int getSize() {
        return pageSize;
    }
}
