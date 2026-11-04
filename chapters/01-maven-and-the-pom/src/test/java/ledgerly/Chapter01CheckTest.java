package ledgerly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Checks for chapter 1. They read YOUR file: lab/pom.xml.
 * Read the failure message. It says what was expected and what was found.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class Chapter01CheckTest {

  private static final Path BASE = Path.of(System.getProperty("labs.basedir", "."));

  // ---------- Task 1: coordinates ----------

  @Test
  @DisplayName("Task 1: project groupId is com.ledgerly")
  void task1a_groupId() {
    assertThat(text(project(), "groupId"))
        .withFailMessage("[task 1: the project's own groupId] expected com.ledgerly")
        .isEqualTo("com.ledgerly");
  }

  @Test
  @DisplayName("Task 1: project artifactId is ledgerly-core")
  void task1b_artifactId() {
    assertThat(text(project(), "artifactId"))
        .withFailMessage("[task 1: the project's own artifactId] expected ledgerly-core")
        .isEqualTo("ledgerly-core");
  }

  @Test
  @DisplayName("Task 1: project version is 0.1.0-SNAPSHOT")
  void task1c_version() {
    assertThat(text(project(), "version"))
        .withFailMessage("[task 1: the project's own version] expected 0.1.0-SNAPSHOT")
        .isEqualTo("0.1.0-SNAPSHOT");
  }

  // ---------- Task 2: parent ----------

  @Test
  @DisplayName("Task 2: a parent element exists directly inside project")
  void task2a_parentExists() {
    assertThat(child(project(), "parent"))
        .withFailMessage("[task 2: parent] there is no <parent> element directly inside <project>")
        .isNotNull();
  }

  @Test
  @DisplayName("Task 2: the parent is spring-boot-starter-parent")
  void task2b_parentCoordinates() {
    Element parent = requireParent("task 2: parent");
    assertThat(text(parent, "groupId"))
        .withFailMessage("[task 2: parent groupId] expected org.springframework.boot")
        .isEqualTo("org.springframework.boot");
    assertThat(text(parent, "artifactId"))
        .withFailMessage("[task 2: parent artifactId] expected spring-boot-starter-parent")
        .isEqualTo("spring-boot-starter-parent");
  }

  @Test
  @DisplayName("Task 2: the parent version is exactly the one this repo uses")
  void task2c_parentVersion() {
    String expected = repoBootVersion();
    Element parent = requireParent("task 2: parent version");
    assertThat(text(parent, "version"))
        .withFailMessage("[task 2: the parent version] expected " + expected
            + " (three numbers, copy it from the parent element of the root pom.xml)")
        .isEqualTo(expected);
  }

  @Test
  @DisplayName("Task 2: the parent has an empty relativePath")
  void task2d_relativePath() {
    Element parent = requireParent("task 2: relativePath");
    Element rel = child(parent, "relativePath");
    assertThat(rel)
        .withFailMessage("[task 2: relativePath] add an empty <relativePath/> inside <parent>")
        .isNotNull();
    assertThat(rel.getTextContent().trim())
        .withFailMessage("[task 2: relativePath] it must be EMPTY, so Maven looks in the repository")
        .isEmpty();
  }

  @Test
  @DisplayName("Task 2: the parent comes before the project's own coordinates")
  void task2e_parentFirst() {
    List<String> order = elementNames(project());
    int parent = order.indexOf("parent");
    int groupId = order.indexOf("groupId");
    assertThat(parent)
        .withFailMessage("[task 2: position] <parent> must be before the project's own <groupId>."
            + " Order found: " + order)
        .isGreaterThanOrEqualTo(0)
        .isLessThan(groupId);
  }

  // ---------- Task 3: java.version ----------

  @Test
  @DisplayName("Task 3: java.version property is 21")
  void task3_javaVersion() {
    Element props = child(project(), "properties");
    assertThat(props)
        .withFailMessage("[task 3: properties] the <properties> element directly inside <project> is missing")
        .isNotNull();
    assertThat(text(props, "java.version"))
        .withFailMessage("[task 3: java.version] expected 21 (the parent's default is 17)")
        .isEqualTo("21");
  }

  // ---------- Task 4 (Interview): web starter ----------

  @Test
  @DisplayName("Task 4: spring-boot-starter-webmvc dependency is present")
  void task4a_webStarterPresent() {
    assertThat(dependency("org.springframework.boot", "spring-boot-starter-webmvc"))
        .withFailMessage("[task 4: web starter] no <dependency> for org.springframework.boot:spring-boot-starter-webmvc"
            + " inside <dependencies>")
        .isNotNull();
  }

  @Test
  @DisplayName("Task 4: the web starter has NO version element")
  void task4b_webStarterNoVersion() {
    Element dep = requireDependency("task 4: web starter", "org.springframework.boot", "spring-boot-starter-webmvc");
    assertThat(child(dep, "version"))
        .withFailMessage("[task 4: web starter version] remove <version>. The parent's dependency management picks it")
        .isNull();
  }

  @Test
  @DisplayName("Task 4: the web starter keeps the default compile scope")
  void task4c_webStarterScope() {
    Element dep = requireDependency("task 4: web starter", "org.springframework.boot", "spring-boot-starter-webmvc");
    Element scope = child(dep, "scope");
    String found = scope == null ? "(none)" : scope.getTextContent().trim();
    assertThat(scope == null || "compile".equals(found))
        .withFailMessage("[task 4: web starter scope] expected no <scope> (default compile) but found " + found)
        .isTrue();
  }

  @Test
  @DisplayName("Task 4: a version-less starter only works with the parent from task 2")
  void task4d_needsParent() {
    assertThat(child(project(), "parent"))
        .withFailMessage("[task 4: why it needs the parent] without a <parent> nobody knows the starter's version."
            + " Do task 2 first")
        .isNotNull();
  }

  // ---------- Task 5: scopes ----------

  @Test
  @DisplayName("Task 5: test starter has scope test and no version")
  void task5a_testStarter() {
    Element dep = requireDependency("task 5: test starter", "org.springframework.boot", "spring-boot-starter-webmvc-test");
    assertThat(scopeOf(dep))
        .withFailMessage("[task 5: test starter scope] expected test, so it never ships in the jar")
        .isEqualTo("test");
    assertThat(child(dep, "version"))
        .withFailMessage("[task 5: test starter version] remove <version>, the parent picks it")
        .isNull();
  }

  @Test
  @DisplayName("Task 5: h2 has scope runtime and no version")
  void task5b_h2() {
    Element dep = requireDependency("task 5: h2", "com.h2database", "h2");
    assertThat(scopeOf(dep))
        .withFailMessage("[task 5: h2 scope] expected runtime: needed to run, never imported by your code")
        .isEqualTo("runtime");
    assertThat(child(dep, "version"))
        .withFailMessage("[task 5: h2 version] remove <version>, Spring Boot's dependency management knows h2")
        .isNull();
  }

  // ---------- Task 6: uat profile ----------

  @Test
  @DisplayName("Task 6: profiles is the last element inside project")
  void task6a_profilesLast() {
    List<String> order = elementNames(project());
    assertThat(order)
        .withFailMessage("[task 6: profiles] add a <profiles> element directly inside <project>")
        .contains("profiles");
    assertThat(order.get(order.size() - 1))
        .withFailMessage("[task 6: profiles position] <profiles> must be the LAST element inside <project>."
            + " Order found: " + order)
        .isEqualTo("profiles");
  }

  @Test
  @DisplayName("Task 6: a profile with id uat exists")
  void task6b_profileId() {
    assertThat(uatProfile())
        .withFailMessage("[task 6: profile id] no <profile> with <id>uat</id> inside <profiles>")
        .isNotNull();
  }

  @Test
  @DisplayName("Task 6: the uat profile sets spring-boot.run.profiles to uat")
  void task6c_profileProperty() {
    Element profile = uatProfile();
    assertThat(profile).withFailMessage("[task 6: profile id] no profile with id uat yet").isNotNull();
    Element props = child(profile, "properties");
    assertThat(props)
        .withFailMessage("[task 6: profile properties] the uat profile needs its own <properties> element")
        .isNotNull();
    assertThat(text(props, "spring-boot.run.profiles"))
        .withFailMessage("[task 6: spring-boot.run.profiles] expected uat inside the profile's properties")
        .isEqualTo("uat");
  }

  @Test
  @DisplayName("Task 6: the property is NOT in the project's main properties")
  void task6d_notInMainProperties() {
    Element props = child(project(), "properties");
    assertThat(props == null ? null : child(props, "spring-boot.run.profiles"))
        .withFailMessage("[task 6: wrong place] spring-boot.run.profiles must be inside the profile only,"
            + " not in the main <properties>")
        .isNull();
  }

  // ---------- helpers ----------

  private static Element project() {
    Path file = BASE.resolve("lab/pom.xml");
    try {
      var factory = DocumentBuilderFactory.newInstance();
      Document doc = factory.newDocumentBuilder().parse(Files.newInputStream(file));
      return doc.getDocumentElement();
    } catch (Exception e) {
      return fail("[lab/pom.xml] could not be read as XML: " + e.getMessage()
          + ". Usually a tag that was opened and not closed. File: " + file.toAbsolutePath());
    }
  }

  private static String repoBootVersion() {
    try {
      var factory = DocumentBuilderFactory.newInstance();
      Document doc = factory.newDocumentBuilder()
          .parse(Files.newInputStream(BASE.resolve("../../pom.xml")));
      return text(child(doc.getDocumentElement(), "parent"), "version");
    } catch (Exception e) {
      return fail("[root pom.xml] could not be read: " + e.getMessage());
    }
  }

  private static Element requireParent(String label) {
    Element parent = child(project(), "parent");
    if (parent == null) {
      fail("[" + label + "] there is no <parent> element directly inside <project> yet");
    }
    return parent;
  }

  private static Element dependency(String groupId, String artifactId) {
    Element deps = child(project(), "dependencies");
    if (deps == null) {
      return null;
    }
    for (Element dep : children(deps, "dependency")) {
      if (groupId.equals(text(dep, "groupId")) && artifactId.equals(text(dep, "artifactId"))) {
        return dep;
      }
    }
    return null;
  }

  private static Element requireDependency(String label, String groupId, String artifactId) {
    Element dep = dependency(groupId, artifactId);
    if (dep == null) {
      fail("[" + label + "] no <dependency> for " + groupId + ":" + artifactId
          + " inside <dependencies>");
    }
    return dep;
  }

  private static String scopeOf(Element dep) {
    Element scope = child(dep, "scope");
    return scope == null ? "(no scope element)" : scope.getTextContent().trim();
  }

  private static Element uatProfile() {
    Element profiles = child(project(), "profiles");
    if (profiles == null) {
      return null;
    }
    for (Element profile : children(profiles, "profile")) {
      if ("uat".equals(text(profile, "id"))) {
        return profile;
      }
    }
    return null;
  }

  private static String text(Element parent, String name) {
    Element e = child(parent, name);
    return e == null ? null : e.getTextContent().trim();
  }

  private static Element child(Element parent, String name) {
    for (Element e : children(parent, null)) {
      if (e.getTagName().equals(name)) {
        return e;
      }
    }
    return null;
  }

  private static List<Element> children(Element parent, String name) {
    List<Element> out = new ArrayList<>();
    NodeList nodes = parent.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node n = nodes.item(i);
      if (n instanceof Element e && (name == null || e.getTagName().equals(name))) {
        out.add(e);
      }
    }
    return out;
  }

  private static List<String> elementNames(Element parent) {
    return children(parent, null).stream().map(Element::getTagName).toList();
  }
}
