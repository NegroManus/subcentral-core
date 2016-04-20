package de.subcentral.fx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.stage.Window;

public abstract class DefaultTask<V> extends Task<V>
{
	private static final Logger	log	= LogManager.getLogger(DefaultTask.class);

	private final Window		owner;

	public DefaultTask(Window owner)
	{
		this.owner = owner;
	}

	@Override
	protected void failed()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Execution of ");
		if (!getTitle().isEmpty())
		{
			sb.append(" the background task \"");
			sb.append(getTitle());
			sb.append('"');
		}
		else
		{
			sb.append("a background task");
		}
		sb.append(" failed");
		String msg = sb.toString();

		log.error(msg, getException());
		Alert alert = FxUtil.createExceptionAlert(owner, "Task execution failed", msg, getException());
		alert.show();
	}
}
