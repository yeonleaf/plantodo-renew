package yeonleaf.plantodo.unit.interceptor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import yeonleaf.plantodo.interceptor.LoginCheckInterceptor;

/**
 * description : {@link LoginCheckInterceptorTest}에서 {@link LoginCheckInterceptor}를 테스트 할 때 사용하는 임시 컨트롤러
 */
@RestController
public class DummyController {

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.status(HttpStatus.OK).body("pong");
    }

}
