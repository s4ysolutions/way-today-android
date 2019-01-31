package solutions.s4y.waytoday;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class Utils {
    public static final String TEST_HOST="192.168.1.132";
    public static final String FIRST_RAND_ID = "126";
    public static void cleanDb() throws Exception {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder()
                .url("http://" + TEST_HOST + ":8001/1/admin/clear")
                .build();
        Response response = client.newCall(request).execute();
        response.body().close();
        if (response.code() != 200) {
            throw new Exception("Failed to clean up, http status=" + response.code());
        }
    }

    private static void randSeed(int seed) throws Exception {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder()
                .url("http://" + TEST_HOST + ":8001/1/admin/rand?seed=" + seed)
                .build();
        Response response = client.newCall(request).execute();
        response.body().close();
        if (response.code() != 200) {
            throw new Exception("Failed to seed, http status=" + response.code());
        }
    }

    public static void randReset() throws Exception {
        randSeed(0);
    }

    public static int getPreOccupatedSize() throws Exception {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder()
                .url("http://" + TEST_HOST + ":8001/1/admin/preoccupated")
                .build();
        Response response = client.newCall(request).execute();
        if (response.code() != 200) {
            throw new Exception("Failed to seed, http status=" + response.code());
        }
        return Integer.parseInt(response.body().string());
    }
}
