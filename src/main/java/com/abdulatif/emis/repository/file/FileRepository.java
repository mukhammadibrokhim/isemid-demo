package com.abdulatif.emis.repository.file;

import com.abdulatif.emis.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;


public interface FileRepository extends PagingAndSortingRepository<File, Long>, JpaRepository<File, Long>, QueryByExampleExecutor<File> {

}
