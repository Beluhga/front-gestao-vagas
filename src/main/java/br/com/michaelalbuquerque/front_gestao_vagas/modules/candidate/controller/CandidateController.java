package br.com.michaelalbuquerque.front_gestao_vagas.modules.candidate.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.michaelalbuquerque.front_gestao_vagas.modules.candidate.dto.CreateCandidateDTO;
import br.com.michaelalbuquerque.front_gestao_vagas.modules.candidate.service.ApplyJobService;
import br.com.michaelalbuquerque.front_gestao_vagas.modules.candidate.service.CandidateService;
import br.com.michaelalbuquerque.front_gestao_vagas.modules.candidate.service.CreateCandidateService;
import br.com.michaelalbuquerque.front_gestao_vagas.modules.candidate.service.FindJobsService;
import br.com.michaelalbuquerque.front_gestao_vagas.modules.candidate.service.ProfileCandidateService;
import br.com.michaelalbuquerque.front_gestao_vagas.modules.candidate.utils.FormatErrorMessage;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
@RequestMapping("/candidate")
public class CandidateController {

    @Autowired
    private CreateCandidateService createCandidateService;

    @Autowired
    private ApplyJobService applyJobService;

    @Autowired
    private FindJobsService findJobsService;

    @Autowired
    private ProfileCandidateService profileCandidateService;

    @Autowired
    private CandidateService candidateService;

    @GetMapping("/login")
    public String login() {
        return "candidate/login";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("candidate", new CreateCandidateDTO());
        return "candidate/create";
    }

    @PostMapping("/create")
    public String save(CreateCandidateDTO candidate, Model model) {

        try{
        this.createCandidateService.execute(candidate);

        } catch(HttpClientErrorException errorException) {
            model.addAttribute("error_message", FormatErrorMessage.formatErrorMessage(errorException.getResponseBodyAsString()));
        }
        model.addAttribute("candidate", new CreateCandidateDTO());
        return "candidate/create";
    }
    
    @PostMapping("/signIn")
    public String signIn(RedirectAttributes redirectAttributes, HttpSession session, String username, String password) {

    try {
        var token = this.candidateService.login(username, password);
        var grants = token.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase())).toList();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(null, null, grants);
        auth.setDetails(token.getAccess_token());

        SecurityContextHolder.getContext().setAuthentication(auth);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
        session.setAttribute("token", token);

            return "redirect:/candidate/profile";

    } catch(HttpClientErrorException exception) {
        redirectAttributes.addFlashAttribute("error_message", "Usuário ou Senha incorretos");
        return "redirect:/candidate/login";
    }
}

    @GetMapping("/profile")
    @PreAuthorize("hasRole('CANDIDATE')")
    public String profile(Model model) {
       try {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var result = this.profileCandidateService.execute((authentication.getDetails().toString()));

        model.addAttribute("user", result);

        return "candidate/profile";

       } catch (HttpClientErrorException exception) {
         return "redirect:/candidate/login";
       }
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasRole('CANDIDATE')")
    public String jobs(Model model, String filter) {

        try{
        if(filter != null) {
            var jobs = this.findJobsService.execute(getToken(), filter);

            model.addAttribute("jobs", jobs);
        }
    } catch(HttpClientErrorException exception) {
        return "redirect:/candidate/login";
    }
    return "candidate/jobs";
    }

    @PostMapping("/jobs/apply")
    @PreAuthorize("hasRole('CANDIDATE')")
    public String applyJob(@RequestParam("jobId") UUID jobId){
        this.applyJobService.execute(getToken(), jobId);
        return "redirect:/candidate/jobs";

    }
    private String getToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getDetails().toString();
    }
    

}
