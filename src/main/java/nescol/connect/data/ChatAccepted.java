package nescol.connect.data;

import lombok.Data;

import java.util.Map;

@Data
public class ChatAccepted {
    private Boolean isAccepted;
    private Map<Object, Object> publicKeys;
}
