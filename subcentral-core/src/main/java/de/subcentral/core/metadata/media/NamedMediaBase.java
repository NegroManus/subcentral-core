package de.subcentral.core.metadata.media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

public abstract class NamedMediaBase extends MediaBase implements NamedMedia {
    private static final long serialVersionUID = 3863044665770766455L;

    protected String          name;
    protected List<String>    aliasNames       = new ArrayList<>(0);

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<String> getAliasNames() {
        return aliasNames;
    }

    public void setAliasNames(Collection<? extends String> aliasNames) {
        this.aliasNames.clear();
        this.aliasNames.addAll(aliasNames);
    }

    @Override
    public List<String> getAllNames() {
        ImmutableList.Builder<String> allNames = ImmutableList.builder();
        if (name != null) {
            allNames.add(name);
        }
        allNames.addAll(aliasNames);
        return allNames.build();
    }
}
