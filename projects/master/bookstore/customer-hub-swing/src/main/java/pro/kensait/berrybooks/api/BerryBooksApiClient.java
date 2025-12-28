package pro.kensait.berrybooks.api;

import pro.kensait.berrybooks.model.CustomerStats;
import pro.kensait.berrybooks.model.CustomerTO;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Berry Books REST APIクライアント
public class BerryBooksApiClient {
    private final String baseUrl;

    public BerryBooksApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    // 全顧客の統計情報を取得
    public List<CustomerStats> fetchCustomerStats() throws IOException, InterruptedException {
        URL url = new URL(baseUrl + "/customers/");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Failed to fetch customers: HTTP " + responseCode);
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        JSONArray jsonArray = new JSONArray(response.toString());
        List<CustomerStats> customers = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.getJSONObject(i);
            CustomerStats customer = new CustomerStats();
            customer.setCustomerId(json.getLong("customerId"));
            customer.setCustomerName(json.getString("customerName"));
            customer.setEmail(json.getString("email"));
            
            // JSONフィールド名は "birthday" (REST APIの仕様)
            String birthDateStr = json.optString("birthday", null);
            if (birthDateStr != null && !birthDateStr.isEmpty()) {
                customer.setBirthDate(LocalDate.parse(birthDateStr));
            }
            
            customer.setAddress(json.getString("address"));
            customer.setOrderCount(json.getLong("orderCount"));
            customer.setBookCount(json.getLong("totalBooks"));
            customers.add(customer);
        }

        return customers;
    }

    // 顧客情報を更新
    public void updateCustomer(Long customerId, CustomerTO customerTO) throws IOException, InterruptedException {
        URL url = new URL(baseUrl + "/customers/" + customerId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        JSONObject json = new JSONObject();
        json.put("customerName", customerTO.getCustomerName());
        json.put("email", customerTO.getEmail());
        json.put("birthday", customerTO.getBirthday()); // REST APIの仕様に合わせる
        json.put("address", customerTO.getAddress());

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            // エラーレスポンスを読み取る
            BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            
            // JSONエラーレスポンスをパース
            try {
                JSONObject errorJson = new JSONObject(response.toString());
                String message = errorJson.optString("message", "更新に失敗しました");
                throw new RuntimeException(message);
            } catch (Exception e) {
                throw new RuntimeException("Failed to update customer: HTTP " + responseCode + " - " + response.toString());
            }
        }
    }
}
