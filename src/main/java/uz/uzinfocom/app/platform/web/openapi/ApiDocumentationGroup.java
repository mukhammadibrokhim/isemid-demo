package uz.uzinfocom.app.platform.web.openapi;

public record ApiDocumentationGroup(
        String group,
        String displayName,
        String title,
        String description,
        String[] pathsToMatch
) {
}