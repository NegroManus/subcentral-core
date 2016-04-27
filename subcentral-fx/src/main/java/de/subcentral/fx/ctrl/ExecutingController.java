package de.subcentral.fx.ctrl;

import javafx.concurrent.Task;

public abstract class ExecutingController extends Controller
{
	public abstract TaskExecutor getExecutor();

	public void execute(Task<?> task)
	{
		TaskExecutor executor = getExecutor();
		if (executor != null)
		{
			executor.execute(task);
		}
		else
		{
			new Thread(task).start();
		}
	}
}
