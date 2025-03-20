package me.beanes.acid.plugin.util.splitstate;

public enum
SplitStateBoolean {
    TRUE,
    FALSE,
    POSSIBLE;

    public boolean possible() {
        return this != FALSE;
    }

    public boolean notPossible() {
        return this != TRUE;
    }

    public static SplitStateBoolean result(boolean first, boolean second) {
        if (first == second) {
            return first ? SplitStateBoolean.TRUE : SplitStateBoolean.FALSE;
        }

        return SplitStateBoolean.POSSIBLE;
    }

    public static SplitStateBoolean result(SplitStateBoolean first, SplitStateBoolean second) {
        if (first == SplitStateBoolean.POSSIBLE || second == SplitStateBoolean.POSSIBLE) {
            return SplitStateBoolean.POSSIBLE;
        }

        if (first != second) {
            return SplitStateBoolean.POSSIBLE;
        }

        return first;
    }
}
