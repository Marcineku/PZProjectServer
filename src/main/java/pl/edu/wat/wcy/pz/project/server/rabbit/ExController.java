package pl.edu.wat.wcy.pz.project.server.rabbit;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.wat.wcy.pz.project.server.form.EmailDTO;


//todo - class only for test
@RestController
@AllArgsConstructor
public class ExController {

    RabbitProducer rabbitProducer;

    @GetMapping("/test")
    public void send() {
        //todo
        System.out.println("sending");
        rabbitProducer.sendToQueue(new EmailDTO("", null, null, EmailDTO.EmailType.VERIFICATION_EMAIL));
    }
}
