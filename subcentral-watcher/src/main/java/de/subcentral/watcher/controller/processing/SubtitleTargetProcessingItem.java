package de.subcentral.watcher.controller.processing;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import de.subcentral.core.metadata.release.Compatibility;
import de.subcentral.core.metadata.release.CompatibilityService.CompatibilityInfo;
import de.subcentral.core.metadata.release.CrossGroupCompatibility;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.SameGroupCompatibility;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.SubtitleAdjustmentNamer;

public class SubtitleTargetProcessingItem extends AbstractProcessingItem
{
    private final ListProperty<Path>	       files		  = new SimpleListProperty<>(this, "files", FXCollections.observableArrayList());
    private final Property<SubtitleAdjustment> subtitleAdjustment = new SimpleObjectProperty<>(this, "subtitleAdjustment");
    private final Property<Release>	       release		  = new SimpleObjectProperty<>(this, "release");
    private final Property<ProcessInfo>	       processInfo	  = new SimpleObjectProperty<>(this, "processInfo");
    private final StringBinding		       nameBinding;
    private final StringBinding		       infoBinding;

    public SubtitleTargetProcessingItem(NamingService namingService, Map<String, Object> namingParameters)
    {
	super(namingService, namingParameters);

	nameBinding = createNameBinding();
	infoBinding = createInfoBinding();
    }

    private StringBinding createNameBinding()
    {
	return new StringBinding()
	{
	    {
		super.bind(subtitleAdjustment, release);
	    }

	    @Override
	    protected String computeValue()
	    {
		Map<String, Object> effectiveParams = new HashMap<>();
		effectiveParams.putAll(SubtitleTargetProcessingItem.this.namingParameters);
		effectiveParams.put(SubtitleAdjustmentNamer.PARAM_RELEASE, release.getValue());
		return SubtitleTargetProcessingItem.this.namingService.name(subtitleAdjustment.getValue(), effectiveParams);
	    }
	};
    }

    private StringBinding createInfoBinding()
    {
	return new StringBinding()
	{
	    {
		super.bind(processInfo);
	    }

	    @Override
	    protected String computeValue()
	    {
		ProcessInfo info = processInfo.getValue();
		if (info == null)
		{
		    return "";
		}
		switch (info.getMethod())
		{
		    case MATCHING:
			return "Matching release";
		    case GUESSING:
		    {
			GuessingProcessInfo pInfo = (GuessingProcessInfo) info;
			StringBuilder sb = new StringBuilder();
			sb.append("Guessed release");
			if (pInfo.getStandardRelease() != null)
			{
			    sb.append(" (based on standard release ");
			    sb.append(SubtitleTargetProcessingItem.this.namingService.name(pInfo.getStandardRelease().getRelease(), SubtitleTargetProcessingItem.this.namingParameters));
			    sb.append(")");
			}
			return sb.toString();
		    }
		    case COMPATIBILITY:
		    {
			CompatibilityProcessInfo cInfo = (CompatibilityProcessInfo) info;
			StringBuilder sb = new StringBuilder();
			sb.append("Compatible to ");
			sb.append(SubtitleTargetProcessingItem.this.namingService.name(cInfo.getCompatibilityInfo().getCompatibleTo(), SubtitleTargetProcessingItem.this.namingParameters));
			sb.append(" because of ");
			Compatibility c = cInfo.getCompatibilityInfo().getCompatibility();
			if (c instanceof SameGroupCompatibility)
			{
			    sb.append("same group");
			}
			else if (c instanceof CrossGroupCompatibility)
			{
			    sb.append(((CrossGroupCompatibility) c).toShortString());
			}
			else
			{
			    sb.append(c);
			}
			return sb.toString();
		    }
		    default:
			return info.toString();
		}
	    }
	};
    }

    @Override
    public ListProperty<Path> getFiles()
    {
	return files;
    }

    @Override
    public StringBinding nameBinding()
    {
	return nameBinding;
    }

    @Override
    public StringBinding infoBinding()
    {
	return infoBinding;
    }

    public final Property<SubtitleAdjustment> subtitleAdjustmentProperty()
    {
	return this.subtitleAdjustment;
    }

    public final SubtitleAdjustment getSubtitleAdjustment()
    {
	return this.subtitleAdjustmentProperty().getValue();
    }

    public final void setSubtitleAdjustment(final SubtitleAdjustment subtitleAdjustment)
    {
	this.subtitleAdjustmentProperty().setValue(subtitleAdjustment);
    }

    public final Property<Release> releaseProperty()
    {
	return this.release;
    }

    public final Release getRelease()
    {
	return this.releaseProperty().getValue();
    }

    public final void setRelease(final Release release)
    {
	this.releaseProperty().setValue(release);
    }

    public final Property<ProcessInfo> processInfoProperty()
    {
	return this.processInfo;
    }

    public final ProcessInfo getProcessInfo()
    {
	return this.processInfoProperty().getValue();
    }

    public final void setProcessInfo(final SubtitleTargetProcessingItem.ProcessInfo processInfo)
    {
	this.processInfoProperty().setValue(processInfo);
    }

    public static class ProcessInfo
    {
	public static enum Method
	{
	    MATCHING, GUESSING, COMPATIBILITY
	};

	private final Method method;

	public ProcessInfo(Method method)
	{
	    this.method = method;
	}

	public Method getMethod()
	{
	    return method;
	}
    }

    public static class GuessingProcessInfo extends ProcessInfo
    {
	private final StandardRelease standardRelease;

	public GuessingProcessInfo(Method method, StandardRelease standardRelease)
	{
	    super(method);
	    this.standardRelease = standardRelease;
	}

	public StandardRelease getStandardRelease()
	{
	    return standardRelease;
	}
    }

    public static class CompatibilityProcessInfo extends ProcessInfo
    {
	private final CompatibilityInfo compatibilityInfo;

	public CompatibilityProcessInfo(Method method, CompatibilityInfo compatibilityInfo)
	{
	    super(method);
	    this.compatibilityInfo = compatibilityInfo;
	}

	public CompatibilityInfo getCompatibilityInfo()
	{
	    return compatibilityInfo;
	}
    }
}
