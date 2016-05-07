package es.upsa.mimo.android.diexpenses.utils;

/**
 * Created by Diego on 27/3/16.
 */
public class Constants {

    public class SharedPreferences {

        public static final String NAME = "diexpenses";

        public class Parameters {

            public class User {

                public static final String IS_LOGGED = "isLogged";
                public static final String ID = "id";
                public static final String USER = "user";
                public static final String NAME = "name";
                public static final String AUTH_TOKEN  ="authToken";
            }

            public class Rate {

                public static final String COUNTER_OF_USES = "counterOfUses";
                public static final String HAS_RATE = "hasRated";
            }
        }
    }

    public class API {
        //public static final String BASE_URL = "https://diexpenses.herokuapp.com/";
        public static final String BASE_URL = "https://diexpenses-herokuapp-com-u8gcrab3473z.runscope.net/";

        public class HEADERS {
            public static final String USER_AGENT = "User-Agent";
            public static final String ACCEPT_LANGUAGE = "Accept-Language";
            public static final String AUTH_TOKEN = "X-AUTH-TOKEN";
        }
    }

    public class Parcelables {

        public static final String USER = "user";
        public static final String BANK_ACCOUNT = "bankAccount";
    }

    public class Bundles {

        public static final String USER_ID = "userId";
        public static final String KIND_ID = "kindId";
    }

    public class Arguments {

        public static final String YEAR = "year";
        public static final String MONTH = "month";
    }

    public class Preferences {

        public static final String PROFILE_IMAGE_NAME = "ProfileImage";
    }

    public class RequestCodes {

        public static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 1;
        public static final int PICK_PROFILE_IMAGE = 2;
        public static final int REQUEST_CAMERA_PERMISSION_CODE = 3;
        public static final int OPEN_CAMERA = 4;

    }

}
