package de.subcentral.fx;

import javafx.concurrent.Task;

public abstract class ExecutingController extends Controller
{
	public abstract TaskExecutor getExecutor();

	public void execute(Task<?> task)
	{
		if (getExecutor() != null)
		{
			getExecutor().execute(task);
		}
		else
		{
			new Thread(task).start();
		}
	}
}
