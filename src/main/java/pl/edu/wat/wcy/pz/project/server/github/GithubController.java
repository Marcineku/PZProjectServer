package pl.edu.wat.wcy.pz.project.server.github;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@CrossOrigin
public class GithubController {
    @Value("${github.auth}")
    private String auth;

    @Value("${github.user}")
    private String user;

    @Value("${github.repo.client}")
    private String clientRepo;

    @Value("${github.repo.server}")
    private String serverRepo;

    private static final Logger LOGGER = LoggerFactory.getLogger(GithubController.class);

    @GetMapping("/github/traffic/{repoName}")
    public String getTraffic(@PathVariable String repoName) {
        LOGGER.info("Request to Github Controller. Repo: " + repoName);
        String name;
        if (repoName.equals("server"))
            name = serverRepo;
        else
            name = clientRepo;
        String url = createUrl(name);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", auth);
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return responseEntity.getBody();
    }

    private String createUrl(String repoName) {
        StringBuilder url = new StringBuilder();
        url.append("https://api.github.com/repos/");
        url.append(user);
        url.append("/");
        url.append(repoName);
        url.append("/traffic/views");
        return url.toString();
    }
}
