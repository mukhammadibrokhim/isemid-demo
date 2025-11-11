package uz.uzinfocom.isemid.features.report.service.impls;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.uzinfocom.isemid.features.form058.reposiotries.Form058Repository;
import uz.uzinfocom.isemid.features.report.service.Form13Service;

@Service
@RequiredArgsConstructor
public class Form13ServiceImpl implements Form13Service {
    private final Form058Repository form058Repository;


}
