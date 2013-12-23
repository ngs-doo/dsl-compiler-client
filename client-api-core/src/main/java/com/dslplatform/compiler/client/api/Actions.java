package com.dslplatform.compiler.client.api;

import java.io.IOException;

import com.dslplatform.compiler.client.api.params.Action;
import com.dslplatform.compiler.client.api.params.Auth;
import com.dslplatform.compiler.client.api.params.DSL;
import com.dslplatform.compiler.client.api.params.Language;
import com.dslplatform.compiler.client.api.params.PackageName;
import com.dslplatform.compiler.client.api.params.Param;
import com.dslplatform.compiler.client.api.params.ProjectID;
import com.dslplatform.compiler.client.api.params.ProjectName;
import com.dslplatform.compiler.client.api.processors.CleanProcessor;
import com.dslplatform.compiler.client.api.processors.CreateProcessor;
import com.dslplatform.compiler.client.api.processors.DeleteProcessor;
import com.dslplatform.compiler.client.api.processors.DiffProcessor;
import com.dslplatform.compiler.client.api.processors.ParseAndDiffProcessor;
import com.dslplatform.compiler.client.api.processors.ParseProcessor;
import com.dslplatform.compiler.client.api.processors.UpdateProcessor;
import com.dslplatform.compiler.client.api.processors.UpdateUnsafeProcessor;
import com.dslplatform.compiler.client.io.Logger;

public class Actions {
    private final Logger logger;
    private final ApiCall apiCall;

    public Actions(
            final Logger logger,
            final ApiCall apiCall) {
        this.logger = logger;
        this.apiCall = apiCall;
    }

    public ParseProcessor parse(final Auth auth, final DSL dsl)
            throws IOException {
        final ParseProcessor pr = new ParseProcessor(logger);
        apiCall.call(Action.PARSE, auth, dsl).processMessages(pr);
        return pr;
    }

    public DiffProcessor diff(
            final Auth auth,
            final DSL dsl,
            final ProjectID projectID) throws IOException {
        final DiffProcessor dp = new DiffProcessor(logger);
        apiCall.call(Action.DIFF, auth, dsl, projectID).processMessages(dp);
        return dp;
    }

    public ParseAndDiffProcessor parseAndDiff(
            final Auth auth,
            final DSL dsl,
            final ProjectID projectID) throws IOException {
        final ParseAndDiffProcessor pdp = new ParseAndDiffProcessor(logger);
        apiCall.call(Action.PARSE_AND_DIFF, auth, dsl, projectID)
                .processMessages(pdp);
        return pdp;
    }

    public UpdateProcessor update(
            final Auth auth,
            final DSL dsl,
            final ProjectID projectID,
            final PackageName packageName,
            final Language... languages) throws IOException {
        final UpdateProcessor up = new UpdateProcessor(logger);

        final Param[] params = new Param[5 + languages.length];
        params[0] = Action.UPDATE;
        params[1] = auth;
        params[2] = dsl;
        params[3] = projectID;
        params[4] = packageName;
        System.arraycopy(languages, 0, params, 5, languages.length);

        apiCall.call(params).processMessages(up);
        return up;
    }

    public UpdateUnsafeProcessor updateUnsafe(
            final Auth auth,
            final DSL dsl,
            final ProjectID projectID,
            final PackageName packageName,
            final Language... languages) throws IOException {
        final UpdateUnsafeProcessor uup = new UpdateUnsafeProcessor(logger);

        final Param[] params = new Param[5 + languages.length];
        params[0] = Action.UPDATE_UNSAFE;
        params[1] = auth;
        params[2] = dsl;
        params[3] = projectID;
        params[4] = packageName;
        System.arraycopy(languages, 0, params, 5, languages.length);

        apiCall.call(params).processMessages(uup);
        return uup;
    }

    public CleanProcessor clean(final Auth auth, final ProjectID projectID)
            throws IOException {
        final CleanProcessor cp = new CleanProcessor(logger);
        apiCall.call(Action.CLEAN, auth, projectID);
        return cp;
    }

    public CreateProcessor create(final Auth auth, final ProjectName projectName)
            throws IOException {
        final CreateProcessor cp = new CreateProcessor(logger);
        apiCall.call(Action.CREATE, auth, projectName).processMessages(cp);
        return cp;
    }

    public DeleteProcessor delete(final Auth auth, final ProjectID projectID)
            throws IOException {
        final DeleteProcessor dp = new DeleteProcessor(logger);
        apiCall.call(Action.DELETE, auth, projectID).processMessages(dp);
        return dp;
    }

//  public RunningTask Clone(
//          final Auth auth,
//          final DSL dsl,
//          final ProjectID projectID) throws IOException {
//      return ApiCall.call(Action.CLONE, auth, dsl, projectID);
//  }
}
