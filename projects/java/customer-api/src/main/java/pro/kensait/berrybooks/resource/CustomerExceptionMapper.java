package pro.kensait.berrybooks.resource;

import pro.kensait.berrybooks.dto.ErrorResponse;
import pro.kensait.berrybooks.exception.CustomerExistsException;
import pro.kensait.berrybooks.exception.CustomerNotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

// 顧客関連の例外をHTTPレスポンスに変換するマッパークラス
@Provider
public class CustomerExceptionMapper implements ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(RuntimeException exception) {
        if (exception instanceof CustomerNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse("customer.not-found", exception.getMessage()))
                    .build();
        } else if (exception instanceof CustomerExistsException) {
            return Response.status(Response.Status.CONFLICT)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse("customer.exists", exception.getMessage()))
                    .build();
        }
        
        // その他の例外は500エラーとして返す
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse("internal.error", exception.getMessage()))
                .build();
    }
}

