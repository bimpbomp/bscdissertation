package bham.student.txm683.heartbreaker;

import org.json.JSONException;

public interface SaveableState {

    String getStateString() throws JSONException;

}
