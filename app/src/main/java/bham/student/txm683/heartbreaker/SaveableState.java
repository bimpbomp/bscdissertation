package bham.student.txm683.heartbreaker;

import org.json.JSONException;
import org.json.JSONObject;

public interface SaveableState {

    JSONObject getStateObject() throws JSONException;

}
