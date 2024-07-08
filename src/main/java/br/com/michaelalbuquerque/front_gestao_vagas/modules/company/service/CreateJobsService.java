package br.com.michaelalbuquerque.front_gestao_vagas.modules.company.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import br.com.michaelalbuquerque.front_gestao_vagas.modules.company.dto.CreateJobsDTO;

@Service
public class CreateJobsService {

    @Value("${host.api.gestao.vagas}")
    private String hostAPIGestaoVagas;
    
    
    public String execute(CreateJobsDTO jobs, String token){
        RestTemplate rt = new RestTemplate();

        HttpHeaders headers = new HttpHeaders(); 
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<CreateJobsDTO> request = new HttpEntity<>(jobs, headers);

        String url = hostAPIGestaoVagas.concat("/company/job/");

        var result = rt.postForObject(url, request, String.class);

        return result;
    }
}
