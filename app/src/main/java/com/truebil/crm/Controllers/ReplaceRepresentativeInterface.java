package com.truebil.crm.Controllers;

import com.android.volley.VolleyError;

import org.json.JSONObject;

public interface ReplaceRepresentativeInterface {
    void onSuccess(JSONObject response);
    void onError(VolleyError error);
}
