package de.subcentral.core.metadata;

public abstract class NamedMetadataBase extends MetadataBase implements NamedMetadata {
    private static final long serialVersionUID = 4769019113412042360L;

    protected String          name;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
