package bham.student.txm683.heartbreaker.intentbundleholders;

import android.os.Bundle;

import static bham.student.txm683.heartbreaker.intentbundleholders.LevelEndStatus.ERROR;

public class LevelEnder {

    private LevelEndStatus status;

    public LevelEnder(){
        status = ERROR;
    }

    public LevelEnder(Bundle bundle){

        if (bundle.containsKey("status")){
            status = LevelEndStatus.valueOf(bundle.getString("status"));
        } else {
            status = ERROR;
        }
    }

    public Bundle createBundle(){
        Bundle bundle = new Bundle();

        bundle.putString("status", status.name());

        return bundle;
    }

    public LevelEndStatus getStatus() {
        return status;
    }

    public void setStatus(LevelEndStatus status) {
        this.status = status;
    }
}