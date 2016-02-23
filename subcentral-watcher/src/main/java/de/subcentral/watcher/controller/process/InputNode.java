package de.subcentral.watcher.controller.process;

import java.nio.file.Path;
import java.util.Objects;

import de.subcentral.watcher.controller.processing.ProcessingController;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class InputNode implements ProcessNode
{
	private final ProcessingController		controller;
	private final Path						inputFile;
	private final Property<Task<Void>>		currentTask	= new SimpleObjectProperty<>();
	private final ListProperty<OutputNode>	outputNodes	= new SimpleListProperty<>();

	public InputNode(ProcessingController controller, Path inputFile)
	{
		this.controller = Objects.requireNonNull(controller, "controller");
		this.inputFile = Objects.requireNonNull(inputFile, "inputFile");
	}

	public Path getInputFile()
	{
		return inputFile;
	}

	public final ReadOnlyProperty<Task<Void>> currentTaskProperty()
	{
		return this.currentTask;
	}

	public final Task<Void> getCurrentTask()
	{
		return this.currentTask.getValue();
	}

	public final ReadOnlyListProperty<OutputNode> outputNodesProperty()
	{
		return this.outputNodes;
	}

	public final ObservableList<OutputNode> getOutputNodes()
	{
		return this.outputNodes.get();
	}

	public void process()
	{
		cancelCurrentTask();

		outputNodes.clear();

		currentTask.setValue(new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				// TODO Auto-generated method stub
				return null;
			}
		});
	}

	public void cancelCurrentTask()
	{
		if (currentTask.getValue() != null)
		{
			currentTask.getValue().cancel(true);
		}
	}

}
