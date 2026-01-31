package pro.kensait.customerhub.resource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pro.kensait.customerhub.dto.CustomerTO;
import pro.kensait.customerhub.entity.Customer;
import pro.kensait.customerhub.service.CustomerService;

/**
 * 顧客マスタ管理REST APIリソースクラス
 * 顧客情報（CUSTOMER テーブル）のCRUD操作を提供します。
 * 注文情報は管理しません（アプリケーション層の責務）。
 */
@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {
    private static final Logger logger = LoggerFactory.getLogger(
            CustomerResource.class);

    @Inject
    private CustomerService customerService;

    // APIメソッド：全顧客を取得する
    @GET
    @Path("/")
    public Response getAll() {
        logger.info("[ CustomerResource#getAll ]");

        // 全顧客を取得
        List<Customer> customers = customerService.getAllCustomers();

        // 顧客エンティティリストから、HTTPレスポンス返却用の顧客TOリストを生成する
        List<CustomerTO> responseCustomers = new ArrayList<>();
        for (Customer customer : customers) {
            responseCustomers.add(toCustomerTO(customer));
        }

        // 顧客リスト（ボディ）とHTTPステータスOKを持つResponseを返す
        return Response.ok(responseCustomers).build();
    }

    // APIメソッド：顧客を取得する（主キー検索）
    @GET
    @Path("/{customerId}")
    public Response getById(@PathParam("customerId") Integer customerId) {
        logger.info("[ CustomerResource#getById ]");

        // メールアドレスから顧客エンティティを検索する
        Customer customer = customerService.getCustomerById(customerId);

        // 顧客エンティティから、HTTPレスポンス返却用の顧客TOを生成する
        CustomerTO responseCustomer = toCustomerTO(customer);
 
        // 顧客TO（ボディ）とHTTPステータスOKを持つResponseを返す
        return Response.ok(responseCustomer).build();
    }

    // APIメソッド：顧客を取得する（一意キーからの条件検索）
    @GET
    @Path("/query_email")
    public Response queryByEmail(@QueryParam("email") String email) {
        logger.info("[ CustomerResource#queryByEmail ]");

        // メールアドレスから顧客エンティティを検索する
        Customer customer = customerService.getCustomerByEmail(email);

        // 顧客エンティティから、HTTPレスポンス返却用の顧客TOを生成する
        CustomerTO responseCustomer = toCustomerTO(customer);

        // 顧客エンティティ（ボディ）とHTTPステータスOKを持つResponseを返す
        return Response.ok(responseCustomer).build();
    }

    // APIメソッド：顧客リストを取得する（誕生日からの条件検索）
    @GET
    @Path("/query_birthday")
    public Response queryFromBirthday(@QueryParam("birthday") String birthdayStr) {
        logger.info("[ CustomerResource#queryFromBirthday ]");

        // 文字列をLocalDateに変換
        LocalDate birthday = LocalDate.parse(birthdayStr);

        // 誕生日開始日から顧客エンティティのリストを取得する
        List<Customer> customers = customerService.searchCustomersFromBirthday(birthday);

        // 顧客エンティティのリストから、HTTPレスポンス返却用の顧客TOリストを生成する
        List<CustomerTO> responseCustomerList = new ArrayList<>();
        for (Customer customer : customers) {
            responseCustomerList.add(toCustomerTO(customer));
        }

        // 顧客リスト（ボディ）とHTTPステータスOKを持つResponseを返す
        return Response.ok(responseCustomerList).build();
    }
    
    // APIメソッド：顧客を新規登録する
    @POST
    @Path("/")
    public Response create(CustomerTO requestCustomer) {
        logger.info("[ CustomerResource#create ]");

        // 受け取った顧客TOから、顧客エンティティを生成する
        Customer customer = toCustomer(requestCustomer);

        // 受け取った顧客エンティティを保存する
        customerService.registerCustomer(customer);

        // 顧客エンティティから、HTTPレスポンス返却用の顧客TOを生成する
        CustomerTO responseCustomerTO = toCustomerTO(customer);

        // ボディが空で、HTTPステータスOKを持つResponseを返す
        return Response.ok(responseCustomerTO).build();
    }

    // APIメソッド：顧客を置換する
    @PUT
    @Path("/{customerId}")
    public Response replace(
            @PathParam("customerId") Integer customerId,
            CustomerTO requestCustomer) {
        logger.info("[ CustomerResource#replace ]");

        // 受け取った顧客TOから、顧客エンティティを生成する
        Customer customer = toCustomer(requestCustomer);

        // 受け取った顧客エンティティを置換する
        customer.setCustomerId(customerId);
        customerService.replaceCustomer(customer);

        // ボディが空で、HTTPステータスOKを持つResponseを返す
        return Response.ok().build();
    }

    // APIメソッド：顧客を削除する
    @DELETE
    @Path("/{customerId}")
    public Response delete(@PathParam("customerId") Integer customerId) {
        logger.info("[ CustomerResource#delete ]");

        // 受け取った顧客IDを持つエンティティを削除する
        customerService.deleteCustomer(customerId);

        // ボディが空で、HTTPステータスOKを持つResponseを返す
        return Response.ok().build();
    }

    // 詰め替え処理（Customer→CustomerTO）
    private CustomerTO toCustomerTO(Customer customer) {
        return new CustomerTO(customer.getCustomerId(),
                customer.getCustomerName(),
                customer.getPassword(),
                customer.getEmail(),
                customer.getBirthday(),
                customer.getAddress());
    }

    // 詰め替え処理（CustomerTO→Customer）
    private Customer toCustomer(CustomerTO customerTO) {
        return new Customer(customerTO.customerName(),
                customerTO.password() != null ? customerTO.password() : "",
                customerTO.email(),
                customerTO.birthday(),
                customerTO.address());
    }
}

