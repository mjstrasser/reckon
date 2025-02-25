package org.ajoberstar.reckon.gradle;

import java.util.List;

import javax.inject.Inject;

import org.ajoberstar.grgit.gradle.GrgitService;
import org.ajoberstar.reckon.core.Version;
import org.ajoberstar.reckon.core.VersionTagWriter;
import org.gradle.api.DefaultTask;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.UntrackedTask;

@UntrackedTask(because = "Git tracks the state")
public class ReckonPushTagTask extends DefaultTask {
  private Property<GrgitService> grgitService;
  private Property<String> remote;
  private Property<Version> version;
  private Property<VersionTagWriter> tagWriter;

  @Inject
  public ReckonPushTagTask(ObjectFactory objectFactory) {
    this.grgitService = objectFactory.property(GrgitService.class);
    this.remote = objectFactory.property(String.class);
    this.version = objectFactory.property(Version.class);
    this.tagWriter = objectFactory.property(VersionTagWriter.class);
  }

  @TaskAction
  public void create() {
    var git = grgitService.get().getGrgit();
    var tagName = tagWriter.get().write(version.get());

    var tagExists = git.getTag().list().stream()
        .anyMatch(tag -> tag.getName().equals(tagName));

    if (tagExists) {
      git.push(op -> {
        if (remote.isPresent()) {
          op.setRemote(remote.get());
        }
        op.setRefsOrSpecs(List.of("refs/tags/" + tagName));
      });
      setDidWork(true);
    } else {
      setDidWork(false);
    }

  }

  @Internal
  public Property<GrgitService> getGrgitService() {
    return grgitService;
  }

  @Input
  @Optional
  public Property<String> getRemote() {
    return remote;
  }

  @Input
  public Property<Version> getVersion() {
    return version;
  }

  @Input
  public Property<VersionTagWriter> getTagWriter() {
    return tagWriter;
  }
}
