package uz.uzinfocom.isemid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {"uz.uzinfocom.isemid.models.entities"})
public class IsemidApplication {

    public static void main(String[] args) {
        SpringApplication.run(IsemidApplication.class, args);
    }

}
