package org.spongepowered.mctester.old.appclass;

public class ErrorSlot {

    public ErrorSlot() {}

    private Throwable error;

    public void clear() {
        this.error = null;
    }

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
