package yeonleaf.plantodo.unit.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import yeonleaf.plantodo.TestConfig;
import yeonleaf.plantodo.controller.CheckboxController;

@Import({TestConfig.class})
@WebMvcTest(CheckboxController.class)
public class CheckboxControllerUnitTest {
}
