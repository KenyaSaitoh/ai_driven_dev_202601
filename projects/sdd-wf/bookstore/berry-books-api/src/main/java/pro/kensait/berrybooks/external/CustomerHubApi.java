package pro.kensait.berrybooks.external;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import pro.kensait.berrybooks.external.dto.CustomerTO;

/**
 * customer-hub-api連携用REST Clientインターフェース
 */
@RegisterRestClient(configKey = "customer-hub-api")
@Path("/api/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface CustomerHubApi {
    
    /**
     * メールアドレスで顧客を検索する
     * 
     * @param email メールアドレス
     * @return 顧客情報
     */
    @GET
    @Path("/email/{email}")
    CustomerTO findByEmail(@PathParam("email") String email);
    
    /**
     * 顧客を登録する
     * 
     * @param customer 顧客情報
     * @return 登録された顧客情報
     */
    @POST
    CustomerTO createCustomer(CustomerTO customer);
}