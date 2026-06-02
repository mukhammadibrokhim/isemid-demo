package uz.uzinfocom.app.platform.scope;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Component;

@Component
public class CriteriaPathResolver {

    public Path<?> resolve(Root<?> root, String dottedPath) {
        Path<?> current = root;
        for (String segment : dottedPath.split("\\.")) {
            current = current.get(segment);
        }
        return current;
    }
}
