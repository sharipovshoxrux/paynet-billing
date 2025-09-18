package uz.baraka.paynetbilling.web.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JsonParamBinder {
    private final ObjectMapper om;
    public JsonParamBinder(ObjectMapper om){ this.om = om; }
    public <T> T bind(Map<String,Object> src, Class<T> type) {
        if (src == null) throw new IllegalArgumentException("params is required");
        return om.convertValue(src, type);
    }
}
