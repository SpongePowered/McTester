package org.spongepowered.mctester.main.appclass;

public class ErrorSlot {

    private Throwable error;

    public void throwIfSet() throws Throwable {
        if (this.error != null) {
            throw this.error;
        }
    }

    public void setErrorIfUnset(Throwable throwable) {
        if (this.error == null) {
            this.error = throwable;
        }
    }

}
