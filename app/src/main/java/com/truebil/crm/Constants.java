package com.truebil.crm;

public class Constants {

    public static final class Config {
        protected static final String CURRENT_FRAG_TAG = "CURRENT_FRAG_TAG";
        public static final String S3_PATH = "https://s3-ap-southeast-1.amazonaws.com/truebil-test/";
        public static final int IMAGE_WIDTH = 250;
        public static final int IMAGE_HEIGHT = 250;
        public static final String API_PATH = BuildConfig.API_PATH;
        public static final int TEXT_MAXLEN = 100;
        public static final long MIN_DATE_DURATION = 1*1000;
        public static final long MAX_DATE_DURATION = 3*24*60*60*1000;
    }

    public static final class Message {
    }

    public static final class SharedPref {
        public static final String JWT_TOKEN = "JWT_TOKEN";
        public static final String USER_ID = "USER_ID";
        public static final String USER_NAME = "USER_NAME";
        public static final String HAS_LOGGED_IN_BEFORE = "HAS_LOGGED_IN_BEFORE";
        public static final String USER_MOBILE = "USER_MOBILE";
        public static final String DELIVERY_DATE = "DELIVERY_DATE";
    }

    public static final class Keys {
        public static final String VISITS = "VISITS";
        public static final String ALL_CARS = "ALL_CARS";
        public static final String FOLLOW_UPS = "FOLLOW_UPS";
        public static final String MORE = "MORE";
        public static final String FRAGMENT = "FRAGMENT";
        public static final String BUYER_ID = "BUYER_ID";
        public static final String MOBILE = "MOBILE";

        public static final String TOKEN_GIVEN = "TOKEN GIVEN";
        public static final String TOKEN_PENDING = "TOKEN PENDING";
        public static final String NEGOTIATION_PENDING = "NEGOTIATION PENDING";
        public static final String DECISION_PENDING = "DECISION PENDING";
        public static final String WAITING_FOR_OTHER_CARS = "WAITING FOR OTHER CARS";
        public static final String NEGOTIATION_UNSUCUCESSFUL = "NEGOTIATION UNSUCUCESSFUL";
        public static final String CAR_QUALITY_ISSUE = "CAR QUALITY ISSUE";
        public static final String OTHER_REASON = "OTHER REASON";

        public static final String SALES_PERSON_ID = "SALES_PERSON_ID";
        public static final String SALES_PERSON_MOBILE = "SALES_PERSON_MOBILE";
        public static final String SALES_PERSON_CITY_ID = "SALES_PERSON_CITY_ID";
        public static final String SALES_PERSON_NAME = "SALES_PERSON_NAME";
        public static final String LOCALITIES_LIST = "LOCALITIES_LIST";
        public static final String RTO_LIST = "RTO_LIST";
        public static final String BANKS = "BANKS";
        public static final String SALES_REPRESENTATIVE_LIST = "SALES_REPRESENTATIVE_LIST";
        public static final String FORM_INFO = "FORM_INFO";
        public static final String MAKES_LIST = "MAKES_LIST";
    }
}
