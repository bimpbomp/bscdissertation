package bham.student.txm683.heartbreaker.utils;

import org.json.JSONException;
import org.json.JSONObject;

public interface Packable <T> {
    JSONObject pack() throws JSONException;
    T build(JSONObject jsonObject) throws JSONException;
}
