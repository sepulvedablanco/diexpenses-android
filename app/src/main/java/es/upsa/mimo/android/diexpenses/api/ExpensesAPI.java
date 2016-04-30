package es.upsa.mimo.android.diexpenses.api;

import java.util.List;
import java.util.Locale;

import es.upsa.mimo.android.diexpenses.models.BankAccount;
import es.upsa.mimo.android.diexpenses.models.Credential;
import es.upsa.mimo.android.diexpenses.models.GenericResponse;
import es.upsa.mimo.android.diexpenses.models.Kind;
import es.upsa.mimo.android.diexpenses.models.Movement;
import es.upsa.mimo.android.diexpenses.models.MovementPage;
import es.upsa.mimo.android.diexpenses.models.NewUser;
import es.upsa.mimo.android.diexpenses.models.Subkind;
import es.upsa.mimo.android.diexpenses.models.User;
import lombok.Getter;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Diego on 1/4/16.
 */
public interface ExpensesAPI {

    @POST("user/login")
    Call<User> doLogin(@Body Credential credential);

    @POST("user")
    Call<GenericResponse> createUser(@Body NewUser newUser);

    @GET("user/{userId}/financialMovements/amounts")
    Call<ResponseBody> getAmount(@Path("userId") long userId, @Query("e") boolean isExpense,
                                 @Query("m") int month, @Query("y") int year);

    @GET("user/{userId}/bankAccounts/amount")
    Call<ResponseBody> getTotalAmount(@Path("userId") long userId);

    @GET("user/{userId}/financialMovementTypes")
    Call<List<Kind>> getKinds(@Path("userId") long userId);

    @PUT("user/{userId}/financialMovementType/{kindId}")
    Call<GenericResponse> editKind(@Path("userId") long userId, @Path("kindId") long kindId, @Body Kind kind);

    @DELETE("user/{userId}/financialMovementType/{kindId}")
    Call<GenericResponse> deleteKind(@Path("userId") long userId, @Path("kindId") long kindId);

    @POST("user/{userId}/financialMovementType")
    Call<GenericResponse> createKind(@Path("userId") long userId, @Body Kind kind);

    @GET("user/financialMovementType/{kindId}/financialMovementSubtypes")
    Call<List<Subkind>> getSubkinds(@Path("kindId") long kindId);

    @PUT("user/financialMovementType/{kindId}/financialMovementSubtype/{subkindId}")
    Call<GenericResponse> editSubkind(@Path("kindId") long kindId, @Path("subkindId") long subkindId, @Body Subkind kind);

    @DELETE("user/financialMovementType/{kindId}/financialMovementSubtype/{subkindId}")
    Call<GenericResponse> deleteSubkind(@Path("kindId") long kindId, @Path("subkindId") long subkindId);

    @POST("user/financialMovementType/{kindId}/financialMovementSubtype")
    Call<GenericResponse> createSubkind(@Path("kindId") long kindId, @Body Subkind subkind);

    @GET("user/{userId}/bankAccounts")
    Call<List<BankAccount>> getBankAccounts(@Path("userId") long userId);

    @PUT("user/{userId}/bankAccount/{bankAccountId}")
    Call<GenericResponse> editBankAccount(@Path("userId") long userId, @Path("bankAccountId") long bankAccountId, @Body BankAccount bankAccount);

    @DELETE("user/{userId}/bankAccount/{bankAccountId}")
    Call<GenericResponse> deleteBankAccount(@Path("userId") long userId, @Path("bankAccountId") long bankAccountId);

    @POST("user/{userId}/bankAccount")
    Call<GenericResponse> createBankAccount(@Path("userId") long userId, @Body BankAccount bankAccount);

    @GET("user/{userId}/financialMovements")
    Call<MovementPage> getMovements(@Path("userId") long userId, @Query("y") int year, @Query("m") int month);

    @POST("user/{userId}/financialMovement")
    Call<GenericResponse> createMovement(@Path("userId") long userId, @Body Movement newMovement);

    @DELETE("user/{userId}/financialMovement/{movementId}")
    Call<GenericResponse> deleteMovement(@Path("userId") long userId, @Path("movementId") long movementId);

}
