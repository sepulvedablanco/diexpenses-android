package es.upsa.mimo.android.diexpenses.api;

import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Locale;

import es.upsa.mimo.android.diexpenses.R;
import es.upsa.mimo.android.diexpenses.models.GenericResponse;
import es.upsa.mimo.android.diexpenses.utils.Constants;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Diego on 1/4/16.
 */
public class Requester {

    private static final String TAG = Requester.class.getSimpleName();

    private Retrofit retrofit;
    private ExpensesAPI expensesApi;
    private Converter<ResponseBody, GenericResponse> genericErrorConverter;

    private static Requester instance;

    private static String token;
    private static boolean setAuthToken;

    private Requester() {
        retrofit = getRetrofitInstance();
        expensesApi = getExpensesApi();
    }

    public static synchronized Requester getInstance() {
        if (instance == null) {
            instance = new Requester();
        }
        return instance;
    }

    private OkHttpClient.Builder getClient() {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();

                Log.d(TAG, "Locale=" + Locale.getDefault().getLanguage());

                Request.Builder builder = original.newBuilder()
                        .header(Constants.API.HEADERS.USER_AGENT, "Diexpenses Android")
                        .header(Constants.API.HEADERS.ACCEPT_LANGUAGE, Locale.getDefault().getLanguage())
                        .method(original.method(), original.body());

                if(setAuthToken) {
                    builder.header(Constants.API.HEADERS.AUTH_TOKEN, token);
                }

                Request request = builder.build();

                return chain.proceed(request);
            }
        });

        return httpClient;
    }

    public Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            OkHttpClient client = getClient().build();
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("dd/MM/yyyy hh:mm:ss");
            Gson gson = gsonBuilder.create();
            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.API.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public ExpensesAPI getExpensesApi() {
        if (expensesApi == null) {
            expensesApi = getRetrofitInstance().create(ExpensesAPI.class);
        }
        return expensesApi;
    }

    public static void setAuthToken(String token) {
        Requester.setAuthToken = true;
        Requester.token = token;
    }

    public GenericResponse getGenericResponse(ResponseBody responseBody) {
        if(genericErrorConverter == null) {
            genericErrorConverter = getRetrofitInstance().responseBodyConverter(GenericResponse.class, new Annotation[0]);
        }

        try {
            GenericResponse genericResponse = genericErrorConverter.convert(responseBody);
            return genericResponse;
        } catch (Exception e) {
            Log.e(TAG, "Error getting generic response:", e);
            return null;
        }
    }

    public static boolean processResponse(retrofit2.Response<GenericResponse> response, Fragment fragment) {
        if (response != null && !response.isSuccessful() && response.errorBody() != null) {
            GenericResponse genericResponse = Requester.getInstance().getGenericResponse(response.errorBody());
            String message;

            if(genericResponse == null) {
                message = fragment.getString(R.string.common_unknown_error);
            } else {
                message = genericResponse.getMessage();
            }

            Snackbar.make(fragment.getView(), message, Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}
