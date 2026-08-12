package uz.uzinfocom.app.platform.devpanel.application.query.dto;

import java.util.List;

public record DevFileListResponse(
        DevFileRoot root,
        String path,
        List<DevFileEntryResponse> entries
) {
}
