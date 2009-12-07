package org.openspotlight.web.command;

import java.util.Map;

import net.sf.json.JSONObject;

import org.openspotlight.federation.context.ExecutionContext;
import org.openspotlight.web.WebException;
import org.openspotlight.web.json.Message;

// TODO: Auto-generated Javadoc
/**
 * The Class HelloWebCommand.
 */
public class HelloWebCommand implements WebCommand {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openspotlight.web.command.WebCommand#execute(org.openspotlight.web
	 * .command.WebCommand.WebCommandContext, java.util.Map)
	 */
	public String execute(final ExecutionContext context,
			final Map<String, String> parameters) throws WebException {
		final Message message = new Message();
		message.setMessage("hello world!");
		return JSONObject.fromObject(message).toString();
	}

}
