package de.subcentral.fx;

import javafx.concurrent.Task;

public abstract class ExecutingController extends Controller
{
	public abstract TaskExecutor getExecutor();

	public void execute(Task<?> task)
	{
		getExecutor().execute(task);
	}
}
