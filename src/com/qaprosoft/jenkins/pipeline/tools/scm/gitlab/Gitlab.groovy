package com.qaprosoft.jenkins.pipeline.tools.scm.gitlab

import com.qaprosoft.jenkins.pipeline.tools.scm.Scm

class Gitlab extends Scm {

    Gitlab(context, host, org, repo, branch) {
        super(context, host, org, repo, branch)
        this.prRefSpec = "+refs/merge-requests/*:refs/remotes/merge-requests/pr/*"
    }

    Gitlab(context) {
        super(context)
    }

    enum HookArgs {
        HEADER_EVENT_NAME("eventName", "x-gitlab-event"),

        PR_ACTION("prAction", "\$.object_attributes.state"),
        PR_SHA("prSha", "\$.object_attributes.last_commit.id"),
        PR_NUMBER("prNumber", "\$.object_attributes.iid"),
        PR_REPO("prRepo", "\$.project.id"),
        PR_SOURCE_BRANCH("prSourceBranch", "\$.object_attributes.source_branch"),
        PR_TARGET_BRANCH("prTargetBranch", "\$.object_attributes.target_branch"),
        PR_FILTER_REGEX("prFilterExpression", "^(opened|reopened)\\s(Merge\\sRequest\\sHook)*?\$"),
        PR_FILTER_TEXT("prFilterText", "\$pr_action \$x_gitlab_event"),

        PUSH_FILTER_TEXT("pushFilterText", "\$ref x_gitlab_event"),
        PUSH_FILTER_REGEX("pushFilterExpression", "^(refs/heads/master\\sPush\\sHook)*?\$"),
        REF_JSON_PATH("refJsonPath", "\$.ref")

        private final String key
        private final String value

        HookArgs(String key, String value) {
            this.key = key
            this.value = value
        }

        public String getKey() { return key }

        public String getValue() { return value }
    }

    @Override
    protected String getBranchSpec(spec) {
        return branch
    }

}
