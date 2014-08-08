package de.subcentral.core.model;

/**
 * The following statements apply to all model classes in this package and the sub packages:
 * <ul>
 * <li>All model classes implement {@link Object#equals(Object)}, {@link Object#hashCode()} and {@link Object#toString()} properly.
 * <li>All non-standard classes implement {@link Comparable}
 * <li>All collection properties are final, never null and always mutable. With this knowledge, fewer helper collections have to be created. setter() methods exists but they invoke <code>coll.clear(); coll.addAll()</code>
 * </li>
 * </ul>
 * 
 * Dependency hierarchy:<br/>
 * model -> -<br/>
 * model.media -> model<br/>
 * model.release -> model, model.media<br/>
 * model.subtitle -> model, model.media, model.release<br/>
 * model.project -> model, model.media, model.release, model.subtitle
 */
