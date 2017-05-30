package de.subcentral.watcher.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import com.google.common.collect.ImmutableSet;

import de.subcentral.core.metadata.service.MetadataService;
import de.subcentral.core.util.Service;
import de.subcentral.core.util.ServiceUtil;
import de.subcentral.fx.settings.ConfigurationPropertyHandler;
import de.subcentral.fx.settings.SimpleDeactivatableSettingsItem;
import de.subcentral.support.orlydbcom.OrlyDbCom;
import de.subcentral.support.predbme.PreDbMe;
import de.subcentral.support.predborg.PreDbOrg;
import de.subcentral.support.xrelto.XRelTo;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class MetadataServiceSettingsItem extends SimpleDeactivatableSettingsItem<MetadataService> {
    private static final ConfigurationPropertyHandler<ObservableList<MetadataServiceSettingsItem>> HANDLER = new ListConfigurationPropertyHandler();

    public static class Availability {
        public enum Code {
            UNKNOWN, CHECKING, AVAILABLE, LIMITED, NOT_AVAILABLE;
        }

        private final Code           code;
        private final Service.Status status;

        public static Availability unknown() {
            return new Availability(Code.UNKNOWN, null);
        }

        public static Availability checking() {
            return new Availability(Code.CHECKING, null);
        }

        public static Availability ofServiceStatus(Service.Status status) {
            switch (status.getCode()) {
                case AVAILABLE:
                    return new Availability(Code.AVAILABLE, status);
                case LIMITED:
                    return new Availability(Code.LIMITED, status);
                case NOT_AVAILABLE:
                    return new Availability(Code.NOT_AVAILABLE, status);
                default:
                    throw new IllegalArgumentException("Illegal service status: " + status);
            }
        }

        private Availability(Code code, Service.Status status) {
            this.code = Objects.requireNonNull(code, "code");
            this.status = status;
        }

        public Code getCode() {
            return code;
        }

        public Service.Status getStatus() {
            return status;
        }
    }

    private final Property<Availability> availability = new SimpleObjectProperty<>(this, "availability", Availability.unknown());

    public MetadataServiceSettingsItem(MetadataService database, boolean enabled) {
        super(database, enabled);
    }

    public Property<Availability> availabilityProperty() {
        return availability;
    }

    public Availability getAvailability() {
        return availability.getValue();
    }

    public void updateAvailability(ExecutorService executor) {
        availability.setValue(Availability.checking());
        Task<Availability> updateAvailibilityTask = new Task<Availability>() {
            {
                updateTitle("Checking availability of " + item.getSite().getDisplayNameOrName());
            }

            @Override
            protected Availability call() throws Exception {
                return Availability.ofServiceStatus(item.checkStatus());
            }

            @Override
            protected void succeeded() {
                availability.setValue(getValue());
            }
        };
        executor.submit(updateAvailibilityTask);
    }

    public static ObservableList<MetadataServiceSettingsItem> createObservableList() {
        return createObservableList(new ArrayList<>());
    }

    public static ObservableList<MetadataServiceSettingsItem> createObservableList(List<MetadataServiceSettingsItem> list) {
        return FXCollections.observableList(list, (MetadataServiceSettingsItem item) -> new Observable[] { item.enabledProperty() });
    }

    public static ConfigurationPropertyHandler<ObservableList<MetadataServiceSettingsItem>> getListConfigurationPropertyHandler() {
        return HANDLER;
    }

    private static class ListConfigurationPropertyHandler implements ConfigurationPropertyHandler<ObservableList<MetadataServiceSettingsItem>> {
        @SuppressWarnings("unchecked")
        @Override
        public ObservableList<MetadataServiceSettingsItem> get(ImmutableConfiguration cfg, String key) {
            if (cfg instanceof HierarchicalConfiguration<?>) {
                return get((HierarchicalConfiguration<ImmutableNode>) cfg, key);
            }
            throw new IllegalArgumentException("Configuration type not supported: " + cfg);
        }

        private static ObservableList<MetadataServiceSettingsItem> get(HierarchicalConfiguration<ImmutableNode> cfg, String key) {
            List<HierarchicalConfiguration<ImmutableNode>> rlsDbCfgs = cfg.configurationsAt(key + ".db");
            List<MetadataServiceSettingsItem> services = new ArrayList<>(rlsDbCfgs.size());
            for (HierarchicalConfiguration<ImmutableNode> serviceCfg : rlsDbCfgs) {
                String name = serviceCfg.getString("");
                MetadataService service = ServiceUtil.getService(getAvailableMetadataServices(), name);
                if (service == null) {
                    throw new IllegalArgumentException("Unknown metadata service: " + name);
                }
                boolean enabled = serviceCfg.getBoolean("[@enabled]", true);
                services.add(new MetadataServiceSettingsItem(service, enabled));
            }
            return createObservableList(services);
        }

        private static Set<MetadataService> getAvailableMetadataServices() {
            ImmutableSet.Builder<MetadataService> services = ImmutableSet.builder();
            services.add(PreDbMe.getMetadataService());
            services.add(PreDbOrg.getMetadataService());
            services.add(XRelTo.getMetadataService());
            services.add(OrlyDbCom.getMetadataService());
            return services.build();
        }

        @Override
        public void add(Configuration cfg, String key, ObservableList<MetadataServiceSettingsItem> value) {
            for (int i = 0; i < value.size(); i++) {
                MetadataServiceSettingsItem service = value.get(i);
                cfg.addProperty(key + ".db(" + i + ")", service.getItem().getName());
                cfg.addProperty(key + ".db(" + i + ")[@enabled]", service.isEnabled());
            }
        }
    }
}