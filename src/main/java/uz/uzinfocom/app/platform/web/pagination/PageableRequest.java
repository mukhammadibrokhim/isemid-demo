package uz.uzinfocom.app.platform.web.pagination;

public interface PageableRequest {

    Integer page();

    Integer size();

    String sortBy();

    String sortDir();
}