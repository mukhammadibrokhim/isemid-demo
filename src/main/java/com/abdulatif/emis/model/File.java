package com.abdulatif.emis.model;


import com.abdulatif.emis.model.util.Auditable;
import com.abdulatif.emis.model.enums.Status;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Getter
@Setter
@Slf4j
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "sys_files")
public class File extends Auditable<String> {
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "FILE_ENTITY_SEQUENCE")
    @SequenceGenerator(sequenceName = "FILE_ENTITY_SEQUENCE", allocationSize = 1, name = "FILE_ENTITY_SEQUENCE")
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "absolute_path")
    private String absolutePath;

    @Column(name = "file_size")
    private Long size;

    @Column(name = "file_url")
    private String url;

    @Enumerated(STRING)
    @Column(name = "status")
    private Status status;
}

