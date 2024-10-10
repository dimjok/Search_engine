package response;

import lombok.Data;

@Data
public class FalseResultResponse {

    private String error;

    private final boolean result = false;
}
