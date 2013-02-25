package gov.nysenate.sage.model.result;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class ApiErrorResult
{
    private String message;

    public ApiErrorResult(String message)
    {
        this.message = message;
    }

    public String getMessage()
    {
        return this.message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public LinkedHashMap<String,Object> toMap()
    {
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put("message", this.message);
        LinkedHashMap<String,Object> root = new LinkedHashMap<>();
        root.put("apierror", map);
        return root;
    }
}
