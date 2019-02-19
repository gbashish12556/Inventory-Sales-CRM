package com.truebil.crm.Network;

import com.android.volley.VolleyError;

import org.json.JSONObject;

public interface VolleyInterface {
    void onResult(JSONObject response);
    void onError(VolleyError error);
}
