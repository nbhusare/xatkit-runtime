package fr.zelus.jarvis.plugins.github.module.io;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.zelus.jarvis.core.EventDefinitionRegistry;
import fr.zelus.jarvis.core.JarvisException;
import fr.zelus.jarvis.intent.EventInstance;
import fr.zelus.jarvis.io.EventInstanceBuilder;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import static fr.zelus.jarvis.io.JsonWebhookEventProvider.JsonHelper.getJsonElementFromJsonObject;
import static java.util.Objects.nonNull;

public class GithubIssueEventBuilder {

    private final static String GITHUB_ISSUES_ACTION_OPENED = "opened";

    private final static String GITHUB_ISSUES_ACTION_EDITED = "edited";

    private final static String GITHUB_ISSUES_ACTION_CLOSED = "closed";

    private final static String GITHUB_ISSUES_ACTION_REOPENED = "reopened";

    private final static String GITHUB_ISSUES_ACTION_ASSIGNED = "assigned";

    private final static String GITHUB_ISSUES_ACTION_UNASSIGNED = "unassigned";

    private final static String GITHUB_ISSUES_ACTION_LABELED = "labeled";

    private final static String GITHUB_ISSUES_ACTION_UNLABELED = "unlabeled";

    private final static String GITHUB_ISSUES_ACTION_MILESTONED = "milestoned";

    private final static String GITHUB_ISSUES_ACTION_DEMILESTONED = "demilestoned";

    public static List<EventInstance> handleGithubIssuesEvent(JsonElement parsedContent, EventDefinitionRegistry
            eventRegistry) {
        JsonObject json = parsedContent.getAsJsonObject();
        String action = getJsonElementFromJsonObject(json, "action").getAsString();
        JsonObject issueObject = getJsonElementFromJsonObject(json, "issue").getAsJsonObject();
        String title = getJsonElementFromJsonObject(issueObject, "title").getAsString();
        String number = getJsonElementFromJsonObject(issueObject, "number").getAsString();
        String body = getJsonElementFromJsonObject(issueObject, "body").getAsString();
        String htmlURL = getJsonElementFromJsonObject(issueObject, "html_url").getAsString();
        JsonObject senderObject = getJsonElementFromJsonObject(json, "sender").getAsJsonObject();
        String senderLogin = getJsonElementFromJsonObject(senderObject, "login").getAsString();
        EventInstanceBuilder builder = EventInstanceBuilder.newBuilder(eventRegistry);
        switch(action) {
            case GITHUB_ISSUES_ACTION_OPENED:
                builder.setEventDefinitionName("Issue_Opened");
                break;
            case GITHUB_ISSUES_ACTION_EDITED:
                builder.setEventDefinitionName("Issue_Edited");
                JsonObject changesObject = getJsonElementFromJsonObject(json, "changes").getAsJsonObject();
                JsonObject changesTitleObject = changesObject.getAsJsonObject("title");
                if(nonNull(changesTitleObject)) {
                    String previousTitle = getJsonElementFromJsonObject(changesTitleObject, "from").getAsString();
                    builder.setOutContextValue("old_title", previousTitle);
                } else {
                    /*
                     * The title has not been updated, setting the old title with the value of the current one.
                     */
                    builder.setOutContextValue("old_title", title);
                }
                JsonObject changesBodyObject = changesObject.getAsJsonObject("body");
                if(nonNull(changesBodyObject)) {
                    String previousBody = getJsonElementFromJsonObject(changesBodyObject, "from").getAsString();
                    builder.setOutContextValue("old_body", previousBody);
                } else  {
                    /*
                     * The body has not been updated, setting the old body with the value of the current one.
                     */
                    builder.setOutContextValue("old_body", body);
                }
                break;
            case GITHUB_ISSUES_ACTION_CLOSED:
                builder.setEventDefinitionName("Issue_Closed");
                break;
            case GITHUB_ISSUES_ACTION_REOPENED:
                builder.setEventDefinitionName("Issue_Reopened");
                break;
            case GITHUB_ISSUES_ACTION_ASSIGNED:
                builder.setEventDefinitionName("Issue_Assigned");
                JsonObject assigneeObject = getJsonElementFromJsonObject(json, "assignee").getAsJsonObject();
                String assigneeLogin = getJsonElementFromJsonObject(assigneeObject, "login").getAsString();
                builder.setOutContextValue("assignee", assigneeLogin);
                break;
            case GITHUB_ISSUES_ACTION_UNASSIGNED:
                builder.setEventDefinitionName("Issue_Unassigned");
                JsonObject removedAssigneeObject = getJsonElementFromJsonObject(json, "assignee").getAsJsonObject();
                String removedAssigneeLogin = getJsonElementFromJsonObject(removedAssigneeObject, "login")
                        .getAsString();
                builder.setOutContextValue("old_assignee", removedAssigneeLogin);
                break;
            case GITHUB_ISSUES_ACTION_LABELED:
                /*
                 * Only one new label per request.
                 */
                builder.setEventDefinitionName("Issue_Labeled");
                JsonObject labelObject = getJsonElementFromJsonObject(json, "label").getAsJsonObject();
                String label = getJsonElementFromJsonObject(labelObject, "name").getAsString();
                builder.setOutContextValue("label", label);
                break;
            case GITHUB_ISSUES_ACTION_UNLABELED:
                /*
                 * Only one removed label per request.
                 */
                builder.setEventDefinitionName("Issue_Unlabeled");
                JsonObject removedLabelObject = getJsonElementFromJsonObject(json, "label").getAsJsonObject();
                String removedLabel = getJsonElementFromJsonObject(removedLabelObject, "name").getAsString();
                builder.setOutContextValue("old_label", removedLabel);
                break;
            case GITHUB_ISSUES_ACTION_MILESTONED:
                builder.setEventDefinitionName("Issue_Milestoned");
                JsonObject milestoneObject = getJsonElementFromJsonObject(issueObject, "milestone").getAsJsonObject();
                String milestoneTitle = getJsonElementFromJsonObject(milestoneObject, "title").getAsString();
                String milestoneUrl = getJsonElementFromJsonObject(milestoneObject, "html_url").getAsString();
                builder.setOutContextValue("milestone_title", milestoneTitle);
                builder.setOutContextValue("milestone_url", milestoneUrl);
                break;
            case GITHUB_ISSUES_ACTION_DEMILESTONED:
                builder.setEventDefinitionName("Issue_Demilestoned");
                break;
            default:
                throw new JarvisException(MessageFormat.format("Unknown Issue action {0}", action));

        }
        builder.setOutContextValue("title", title)
                .setOutContextValue("number", number)
                .setOutContextValue("body", body)
                .setOutContextValue("user", senderLogin)
                .setOutContextValue("url", htmlURL);
        return Arrays.asList(builder.build());
    }
}