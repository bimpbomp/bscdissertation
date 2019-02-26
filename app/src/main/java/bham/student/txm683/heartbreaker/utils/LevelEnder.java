package bham.student.txm683.heartbreaker.utils;

import android.os.Bundle;

public class LevelEnder {

    private boolean success;

    public LevelEnder(){

    }

    public LevelEnder(Bundle bundle){
        if (bundle.containsKey("success"))
            success = bundle.getBoolean("success");
        else
            throw new IllegalArgumentException("Bundle does not contain success boolean");
    }

    public Bundle createBundle(){
        Bundle bundle = new Bundle();

        bundle.putBoolean("success", success);

        return bundle;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
