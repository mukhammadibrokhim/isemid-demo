package uz.uzinfocom.app.shared.pagination;

public interface PageableRequest {

    Integer page();

    Integer size();

    String sortBy();

    String sortDir();
}