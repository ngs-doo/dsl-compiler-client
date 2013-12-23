package com.dslplatform.compiler.client.gui.windows.login;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingWorker;

import com.dslplatform.compiler.client.api.ApiCall;
import com.dslplatform.compiler.client.api.RunningTask;
import com.dslplatform.compiler.client.api.params.Action;
import com.dslplatform.compiler.client.api.params.Credentials;
import com.dslplatform.compiler.client.api.params.DSL;
import com.dslplatform.compiler.client.api.transport.Message;
import com.dslplatform.compiler.client.api.transport.MessageType;
import com.dslplatform.compiler.client.gui.windows.login.LoginResponse.Status;
import com.dslplatform.compiler.client.io.Logger;

public class LoginWorker extends SwingWorker<Boolean, LoginResponse> {
    private final Logger logger;
    private final String username;
    private final String password;
    private final ApiCall apiCall;

    public LoginWorker(
            final Logger logger,
            final ApiCall apiCall,
            final String username,
            final String password) {
        this.logger = logger;
        this.apiCall = apiCall;
        this.username = username;
        this.password = password;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        try {
            final HashMap<String, String> mockDSL = new HashMap<String, String>();
            mockDSL.put("auth.dsl", "module Auth;");

            publish(new LoginResponse(Status.PENDING,
                    "Connecting to the server ..."));

            final RunningTask rt = apiCall.call(Action.PARSE, new Credentials(
                    username, password), new DSL(mockDSL));

            publish(new LoginResponse(Status.PENDING,
                    "Connection established, authenticating ..."));

            for (final Message message : rt.getMessages()) {
                if (message.messageType == MessageType.SUCCESS) {
                    publish(new LoginResponse(Status.SUCCESS,
                            "User authenticated!"));
                    return true;
                }
            }

            publish(new LoginResponse(Status.INVALID,
                    "Invalid username / password!"));
            return false;
        } catch (final IOException e) {
            publish(new LoginResponse(Status.SUCCESS, e.getMessage()));
            return false;
        }
    }

    @Override
    protected void process(final List<LoginResponse> chunks) {
        firePropertyChange("Auth", null, chunks.get(0));
    }
}
