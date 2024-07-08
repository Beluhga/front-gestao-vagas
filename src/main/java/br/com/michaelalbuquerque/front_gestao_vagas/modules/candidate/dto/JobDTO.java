package br.com.michaelalbuquerque.front_gestao_vagas.modules.candidate.dto;

import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDTO {
    private UUID id;
    private UUID companyId;
    private Date createdAt;
    private String description;
    private String benefits;
    private String level;
}
