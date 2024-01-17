package nescol.connect.data;

import lombok.Data;

@Data
public class GetMeResponse {
    private boolean success;
    private String userId;
}
