package uz.uzinfocom.app.platform.persistence.mapper;

public final class TableStatusMapper {

    private TableStatusMapper() {
    }

    /**
     * Shared INCOMING+SENT -> NEW display-status rule used by every module's
     * table projection mapping; falls back to a same-named value on the
     * target enum for every other status.
     */
    public static <S extends Enum<S>, D extends Enum<D>> D deriveTableStatus(
            S status,
            boolean incomingSent,
            D newStatusValue,
            Class<D> targetType
    ) {
        if (status == null) {
            return null;
        }
        if (incomingSent) {
            return newStatusValue;
        }
        return Enum.valueOf(targetType, status.name());
    }
}
