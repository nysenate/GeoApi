package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.model.result.ResultStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ping")
public class PingController {
    @GetMapping("")
    public BaseResponse ping() {
        return new BaseResponse(ResultStatus.SUCCESS);
    }
}
